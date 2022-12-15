// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.zimbra.common.lmtp.LmtpClient;
import com.zimbra.common.lmtp.LmtpClient.Protocol;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.EmailUtil;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;

@SuppressWarnings("static-access")
public class LmtpInject {

    private static Log mLog = LogFactory.getLog(LmtpInject.class);

    private static Options mOptions = new Options();

    private String mSender;
    private String[] mRecipients;
    private List<File> mFiles;
    private String mHost;
    private int mPort;
    private Protocol mProto;
    private int mCurrentFileIndex = 0;

    private int mSucceeded;
    private int mFailed;
    private int mIgnored;

    private long mStartTime;
    private long mLastProgressTime;
    private int mLastProgressCount;
    private boolean mQuietMode = false;
    private boolean mVerbose = false;

    private volatile long mFileSizeTotal = 0;
    private int mNumThreads;
    private boolean skipTLSCertValidation;


	private LmtpInject(int numThreads,
                       String sender,
                       String[] recipients,
                       List<File> files,
                       String host,
                       int port,
                       Protocol proto,
                       boolean quietMode,
                       boolean tracingEnabled,
                       boolean verbose, boolean skipTLSCertValidation)
    throws Exception {
        mNumThreads = numThreads;
        mSender = sender;
        mRecipients = recipients;
        mFiles = files;
        mHost = host;
        mPort = port;
        mProto = proto;
        mSucceeded = mFailed = mIgnored = 0;
        mStartTime = mLastProgressTime = 0;
        mLastProgressCount = 0;
        mQuietMode = quietMode;
        mVerbose = verbose;
        this.skipTLSCertValidation = skipTLSCertValidation;
    }

    public synchronized void markStartTime() {
        mStartTime = mLastProgressTime = System.currentTimeMillis();
    }

    private int mReportEvery = 100;
    public synchronized void setReportEvery(int num) { mReportEvery = num; }
    
    public void incSuccess() {
        int count;
        int lastCount = 0;
        long lastTime = 0;
        long startTime = 0;
        long now = 0;
        boolean report = false;

        synchronized (this) {
            count = ++mSucceeded;
            if (count % mReportEvery == 0) {
                report = true;
                startTime = mStartTime;
                lastCount = mLastProgressCount;
                lastTime = mLastProgressTime;
                mLastProgressCount = count;
                now = System.currentTimeMillis();
                mLastProgressTime = now;
            }
        }
        if (report && !mQuietMode) {
            long elapsed = now - lastTime;
            long howmany = count - lastCount;
            double rate = 0.0;
            if (elapsed > 0)
                rate = howmany * 1000.0 / elapsed;

            long elapsedTotal = now - startTime;
            double rateAvg = 0.0;
            if (elapsedTotal > 0)
                rateAvg = count * 1000.0 / elapsedTotal;

            System.out.printf(
                    "[progress] " +
                    "%d msgs in %dms @ %.2fmps; " +
                    "last %d msgs in %dms @ %.2fmps\n",
                    count, elapsedTotal, rateAvg,
                    howmany, elapsed, rate);
        }
    }
    public synchronized void incFailure() { mFailed++; }
    public synchronized void incIgnored() { mIgnored++; }
    public synchronized int getSuccessCount() { return mSucceeded; }
    public synchronized int getFailureCount() { return mFailed; }
    public String getSender() { return mSender; }

    String getHost() { return mHost; }
    int getPort() { return mPort; }
    Protocol getProtocol() { return mProto; }
    boolean isVerbose() { return mVerbose; }
    public boolean isSkipTLSCertValidation() {
        return skipTLSCertValidation;
    }
    boolean isQuiet() { return mQuietMode; }
    
    /**
     * Returns the next file and increments the current file
     * index.
     */
    synchronized File getNextFile() {
        if (mCurrentFileIndex >= mFiles.size()) {
            return null;
        }
        return mFiles.get(mCurrentFileIndex++);
    }
    
    public String[] getRecipients() {
        return mRecipients;
    }

    public void addToFileSizeTotal(long size) {
        mFileSizeTotal += size;
    }
    
    private void run()
    throws IOException {
        Thread[] threads = new Thread[mNumThreads];

        // Start threads.
        for (int i = 0; i < mNumThreads; i++) {
            threads[i] = new Thread(new LmtpInjectTask(this));
            threads[i].start();
        }
        
        // Wait for them to finish.
        for (int i = 0; i < mNumThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }
    }

    private static class LmtpInjectTask implements Runnable {
        private LmtpInject mDriver;
        private LmtpClient mClient;

        public LmtpInjectTask(LmtpInject driver)
        throws IOException {
            mDriver = driver;
            mClient = new LmtpClient(driver.getHost(), driver.getPort(), mDriver.getProtocol(),mDriver.isSkipTLSCertValidation());
            if (mDriver.isVerbose() && !mDriver.isQuiet()) {
                mClient.quiet(false);
            } else {
                mClient.quiet(true);
            }
        }

        public void run() {
            File file = mDriver.getNextFile();
            while (file != null) {
                InputStream in = null;
                try {
                    boolean ok = false;
                    long dataLength;

                    if (FileUtil.isGzipped(file)) {
                        dataLength = ByteUtil.getDataLength(new GZIPInputStream(new FileInputStream(file)));
                        in = new GZIPInputStream(new FileInputStream(file));
                    } else {
                        dataLength = file.length();
                        in = new FileInputStream(file);
                    }

                    ok = mClient.sendMessage(in, mDriver.getRecipients(), mDriver.getSender(), file.getName(), dataLength);
                    if (ok) {
                        mDriver.incSuccess();
                        mDriver.addToFileSizeTotal(file.length());
                    } else {
                        mDriver.incFailure();
                    }
                } catch (Exception e) {
                    mDriver.incFailure();
                    mLog.warn("Delivery failed for " + file.getPath() + ": ", e);
                } finally {
                    ByteUtil.closeStream(in);
                }
                file = mDriver.getNextFile();
            }
            mClient.close();
        }
    }

    static {
        mOptions.addOption("d", "directory", true,  "message file directory");
        mOptions.addOption("a", "address",   true,  "lmtp server (default localhost)");
        mOptions.addOption("p", "port",      true,  "lmtp server port (default 7025)");
        mOptions.addOption(
            OptionBuilder.withLongOpt("sender").hasArg(true).withDescription("envelope sender (mail from)").create("s"));
        Option ropt = new Option("r", "recipient", true,
            "envelope recipients (rcpt to).  This option accepts multiple arguments, so it can't be last " +
            "if a list of input files is used.");
        ropt.setArgs(Option.UNLIMITED_VALUES);
        mOptions.addOption(
            OptionBuilder.withLongOpt("recipient").hasArgs(Option.UNLIMITED_VALUES).withDescription(
                "envelope recipients (rcpt to).  This option accepts multiple arguments, so it can't be last " +
                "if a list of input files is used.").create("r"));
        mOptions.addOption("t", "threads",   true,  "number of worker threads (default 1)");
        mOptions.addOption("q", "quiet",     false, "don't print status");
        mOptions.addOption("T", "trace",     false, "trace server/client traffic");
        mOptions.addOption("N", "every",     true,  "report progress after every N messages (default 100)");
        mOptions.addOption(null, "smtp",     false, "use SMTP protocol instead of LMTP");
        mOptions.addOption("h", "help",      false, "display usage information");
        mOptions.addOption("v", "verbose",   false, "print detailed delivery status");
        mOptions.addOption(null, "noValidation", false, "don't validate file content");
        mOptions.addOption(null, "skipTLSCertValidation", false, "don't validate server certifcate during TLS handshake");
    }

    private static void usage(String errmsg) {
        if (errmsg != null) {
            mLog.error(errmsg);
        }
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
            "zmlmtpinject -r <recip1> [recip2 ...] -s <sender> [options]",
            "  <file1 [file2 ...] | -d <dir>>",
            mOptions,
            "Specified paths contain rfc822 messages.  Files may be gzipped.");
        System.exit((errmsg == null) ? 0 : 1);
    }

    private static CommandLine parseArgs(String args[]) {
        StringBuffer gotCL = new StringBuffer("cmdline: ");
        for (int i = 0; i < args.length; i++) {
            gotCL.append("'").append(args[i]).append("' ");
        }
        //mLog.info(gotCL);

        CommandLineParser parser = new GnuParser();
        CommandLine cl = null;
        try {
            cl = parser.parse(mOptions, args);
        } catch (ParseException pe) {
            usage(pe.getMessage());
        }
        return cl;
    }

    public static void main(String[] args) {
        CliUtil.toolSetup();
        CommandLine cl = parseArgs(args);

        if (cl.hasOption("h")) {
            usage(null);
        }
        boolean quietMode = cl.hasOption("q");
        int threads = 1;
        if (cl.hasOption("t")) {
            threads = Integer.valueOf(cl.getOptionValue("t")).intValue();
        }

        String host = null;
        if (cl.hasOption("a")) {
            host = cl.getOptionValue("a");
        } else {
            host = "localhost";
        }

        int port;
        Protocol proto = null;
        if (cl.hasOption("smtp")) {
            proto = Protocol.SMTP;
            port = 25;
        } else
            port = 7025;
        if (cl.hasOption("p"))
            port = Integer.valueOf(cl.getOptionValue("p")).intValue();

        String[] recipients = cl.getOptionValues("r");
        String sender = cl.getOptionValue("s");
        boolean tracingEnabled = cl.hasOption("T");

        int everyN;
        if (cl.hasOption("N")) {
            everyN = Integer.valueOf(cl.getOptionValue("N")).intValue();
        } else {
            everyN = 100;
        }

        // Process files from the -d option.
        List<File> files = new ArrayList<File>();
        if (cl.hasOption("d")) {
            File dir = new File(cl.getOptionValue("d"));
            if (!dir.isDirectory()) {
                System.err.format("%s is not a directory.\n", dir.getPath());
                System.exit(1);
            }
            File[] fileArray = dir.listFiles();
            if (fileArray == null || fileArray.length == 0) {
                System.err.format("No files found in directory %s.\n", dir.getPath());
            }
            Collections.addAll(files, fileArray);
        }
        
        // Process files specified as arguments.
        for (String arg : cl.getArgs()) {
            files.add(new File(arg));
        }
        
        // Validate file content.
        if (!cl.hasOption("noValidation")) {
            Iterator<File> i = files.iterator();
            while (i.hasNext()) {
                InputStream in = null;
                File file = i.next();
                boolean valid = false;
                try {
                    in = new FileInputStream(file);
                    if (FileUtil.isGzipped(file)) {
                        in = new GZIPInputStream(in);
                    }
                    in = new BufferedInputStream(in); // Required for RFC 822 check
                    if (!EmailUtil.isRfc822Message(in)) {
                        System.err.format("%s does not contain a valid RFC 822 message.\n", file.getPath());
                    } else {
                        valid = true;
                    }
                } catch (IOException e) {
                    System.err.format("Unable to validate %s: %s.\n", file.getPath(), e.toString());
                } finally {
                    ByteUtil.closeStream(in);
                }
                if (!valid) {
                    i.remove();
                }
            }
        }

        if (files.size() == 0) {
            System.err.println("No files to inject.");
            System.exit(1);
        }

        if (!quietMode) {
            System.out.format("Injecting %d message(s) to %d recipient(s).  Server %s, port %d, using %d thread(s).\n",
                files.size(), recipients.length, host, port, threads);
        }

        int totalFailed = 0;
        int totalSucceeded = 0;
        long startTime = System.currentTimeMillis();
        boolean verbose = cl.hasOption("v");
        boolean skipTLSCertValidation = cl.hasOption("skipTLSCertValidation");

        LmtpInject injector = null;
        try {
            injector = new LmtpInject(threads,
                    sender, recipients, files,
                    host, port, proto,
                    quietMode, tracingEnabled, verbose, skipTLSCertValidation);
        } catch (Exception e) {
            mLog.error("Unable to initialize LmtpInject", e);
            System.exit(1);
        }

        injector.setReportEvery(everyN);
        injector.markStartTime();
        try {
            injector.run();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int succeeded = injector.getSuccessCount();
        int failedThisTime = injector.getFailureCount();
        long elapsedMS = System.currentTimeMillis() - startTime;
        double elapsed = elapsedMS / 1000.0;
        double msPerMsg = 0.0;
        double msgSizeKB = 0.0;
        if (succeeded > 0) {
            msPerMsg = elapsedMS;
            msPerMsg /= succeeded;
            msgSizeKB = injector.mFileSizeTotal / 1024.0;
            msgSizeKB /= succeeded;
        }
        double msgPerSec = ((double) succeeded / (double) elapsedMS) * 1000;
        if (!quietMode) {
            System.out.println();
            System.out.printf(
                "LmtpInject Finished\n" +
                "submitted=%d failed=%d\n" +
                "%.2fs, %.2fms/msg, %.2fmsg/s\n" +
                "average message size = %.2fKB\n",
                succeeded, failedThisTime,
                elapsed, msPerMsg, msgPerSec,
                msgSizeKB);
        }

        totalFailed+=failedThisTime;
        totalSucceeded+=succeeded;

        if (totalFailed!= 0)
            System.exit(1);
    }
}
