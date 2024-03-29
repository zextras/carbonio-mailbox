// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog.util;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.redolog.RedoPlayer;
import com.zimbra.cs.redolog.RolloverManager;
import com.zimbra.cs.redolog.logger.FileHeader;
import com.zimbra.cs.redolog.logger.FileLogReader;
import com.zimbra.cs.util.Config;
import com.zimbra.cs.util.SoapCLI;
import com.zimbra.cs.util.Zimbra;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * zmplayredo: Program for playing back redologs.
 *
 * This program is intended to be run with mailboxd process stopped.
 * Some diagnostic information is logged to stdout/stderr.  Most of
 * the logging goes to /opt/zextras/log/mailbox.log, with log level
 * controlled by /opt/zextras/conf/log4j.properties file.
 *
 * When it is run without options it replays all redologs found under
 * /opt/zextras/redolog/archive directory, in sequence order, followed
 * by /opt/zextras/redolog/redo.log.  For each redolog all committed
 * operations are replayed.
 *
 * Specify --mailboxId <mailbox id> option to replay committed operations
 * for that mailbox only.
 *
 * Specify --logfiles <list of redolog files> option to replay only
 * those redologs, in the order specified.
 *
 * Specify --fromSeq <sequence> to replay logs from that sequence only.
 * Specify --toSeq <sequence> to replay logs up to that sequence only.
 * Specify --fromTime <timestamp> to replay operations from that time only.
 * Specify --toTime <timestamp> to replay operations up to that time only.
 *
 * Specify --stopOnError option to make replay stop when it encounters any
 * error.  By default errors are logged and ignored.
 *
 * Specify --threads <number of threads> to set the degree of parallelism.
 * By default 50 threads are used.  Operations for a given mailbox is always
 * executed by the same thread to guarantee execution order within a mailbox.
 * Thread assignment is done by taking the module of mailbox id divided by
 * number of threads.  Thus in the default setting mailboxes 1 and 51 are
 * handled by the same thread.  This means replay is not fully parallelized.
 * This is a consequence of the need to guarantee order of execution for each
 * mailbox.
 *
 * When only 1 thread is used or --mailboxId option is used all replay is done
 * by the main thread.  Even when multiple replay threads are used operations
 * that span multiple mailboxes (StoreIncomingBlob of a multi-recipient email
 * delivery) or system operations (volume ops) are executed by the main thread.
 * This is necessary to guarantee that latermailbox-specific operations that
 * depend on the non-mailbox-specific operation (e.g. CreateMessage that links
 * to blob stored by StoreIncomingBlob) are not started out of order.
 *
 * Specify --queueCapacity <number of ops> to set the capacity of the operation
 * queue used by each replay thread.  Default capacity is 100.  This parameter
 * is related to the degree of parallelism.  When a replay thread's queue is full
 * and the main thread must submit another operation to the thread, it will block
 * and will be prevented from submitting operations that might have been run in
 * parallel by other replay threads that have available room in the queue.
 */
public class PlaybackUtil {

    private static final String OPT_FROM_TIME = "fromTime";
    private static final String OPT_FROM_SEQ = "fromSeq";
    private static final String OPT_TO_TIME = "toTime";
    private static final String OPT_TO_SEQ = "toSeq";
    private static final String OPT_MAILBOX_ID = "mailboxId";
    private static final String OPT_LOGFILES = "logfiles";
    private static final String OPT_STOP_ON_ERROR = "stopOnError";
    private static final String OPT_THREADS = "threads";
    private static final String OPT_QUEUE_CAPACITY = "queueCapacity";
    private static final String OPT_HELP = "h";

    private static Options sOptions = new Options();

    static {
        sOptions.addOption(null, OPT_FROM_TIME, true, "Replay from this time (inclusive)");
        sOptions.addOption(null, OPT_FROM_SEQ, true, "Replay from this redolog sequence (inclusive)");
        sOptions.addOption(null, OPT_TO_TIME, true, "Replay to this time (inclusive)");
        sOptions.addOption(null, OPT_TO_SEQ, true, "Replay to this redolog sequence (inclusive)");
        sOptions.addOption(null, OPT_MAILBOX_ID, true, "Replay for this mailbox only");
        sOptions.addOption(null, OPT_THREADS, true, "Number of parallel redo threads; default=50");
        sOptions.addOption(null, OPT_QUEUE_CAPACITY, true, "Queue capacity per player thread; default=100");

        Option logfilesOpt = new Option(null, OPT_LOGFILES, true, "Replay these logfiles, in order");
        logfilesOpt.setArgs(Option.UNLIMITED_VALUES);
        sOptions.addOption(logfilesOpt);

        sOptions.addOption(null, OPT_STOP_ON_ERROR, false, "Stop replay on any error");
        sOptions.addOption(OPT_HELP, "help", false, "Show help (this output)");
    }

    private static void usage(String errmsg) {
        if (errmsg != null) {
            System.err.println(errmsg);
        }

        String usage = "zmplayredo <options>";
        Options opts = sOptions;
        PrintWriter pw = new PrintWriter(System.err, true);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, formatter.getWidth(), usage,
                null, opts, formatter.getLeftPadding(), formatter.getDescPadding(),
                null);
        pw.flush();

        String trailer = SoapCLI.getAllowedDatetimeFormatsHelp();
        if (trailer != null && trailer.length() > 0) {
            System.err.println();
            System.err.println(trailer);
        }
    }

    private static CommandLine parseArgs(String[] args) {
        CommandLineParser parser = new GnuParser();
        CommandLine cl = null;
        try {
            cl = parser.parse(sOptions, args);
        } catch (ParseException pe) {
            usage(pe.getMessage());
            System.exit(1);
        }
        return cl;
    }

    private static class Params {
        private static final int MBOX_ID_UNSET = -1;
        private static final int PLAYER_THREADS = 50;
        private static final int QUEUE_CAPACITY = 100;

        public long fromTime = Long.MIN_VALUE;
        public long fromSeq = Long.MIN_VALUE;
        public long toTime = Long.MAX_VALUE;
        public long toSeq = Long.MAX_VALUE;
        public int mboxId = MBOX_ID_UNSET;
        public int threads = PLAYER_THREADS;
        public int queueCapacity = QUEUE_CAPACITY;
        public File[] logfiles;
        public boolean stopOnError = false;
        public boolean help = false;
    }

    private static Params initParams(CommandLine cl) throws ServiceException, IOException {
        Params params = new Params();
        params.help = cl.hasOption(OPT_HELP);
        if (params.help)
            return params;

        params.stopOnError = cl.hasOption(OPT_STOP_ON_ERROR);

        if (cl.hasOption(OPT_FROM_TIME)) {
            String timeStr = cl.getOptionValue(OPT_FROM_TIME);
            Date time = SoapCLI.parseDatetime(timeStr);
            if (time != null) {
                params.fromTime = time.getTime();
                SimpleDateFormat f = new SimpleDateFormat(SoapCLI.CANONICAL_DATETIME_FORMAT);
                String tstamp = f.format(time);
                System.out.printf("Using from-time of %s\n", tstamp);
            } else {
                System.err.printf("Invalid timestamp \"%s\" specified for --%s option\n",
                                  timeStr, OPT_FROM_TIME);
                System.err.println();
                System.err.print(SoapCLI.getAllowedDatetimeFormatsHelp());
                System.exit(1);
            }
        }
        if (cl.hasOption(OPT_FROM_SEQ)) {
            params.fromSeq = Long.parseLong(cl.getOptionValue(OPT_FROM_SEQ));
            System.out.printf("Using from-sequence of %d\n", params.fromSeq);
        }
        if (cl.hasOption(OPT_TO_TIME)) {
            String timeStr = cl.getOptionValue(OPT_TO_TIME);
            Date time = SoapCLI.parseDatetime(timeStr);
            if (time != null) {
                params.toTime = time.getTime();
                SimpleDateFormat f = new SimpleDateFormat(SoapCLI.CANONICAL_DATETIME_FORMAT);
                String tstamp = f.format(time);
                System.out.printf("Using to-time of %s\n", tstamp);
            } else {
                System.err.printf("Invalid timestamp \"%s\" specified for --%s option\n",
                                  timeStr, OPT_TO_TIME);
                System.err.println();
                System.err.print(SoapCLI.getAllowedDatetimeFormatsHelp());
                System.exit(1);
            }
        }
        if (cl.hasOption(OPT_TO_SEQ)) {
            params.toSeq = Long.parseLong(cl.getOptionValue(OPT_TO_SEQ));
            System.out.printf("Using to-sequence of %d\n", params.toSeq);
        }
        if (params.fromSeq > params.toSeq) {
            System.err.println("Error: fromSeq greater than toSeq");
            System.exit(1);
        }
        if (params.fromTime > params.toTime) {
            System.err.println("Error: fromTime later than toTime");
            System.exit(1);
        }

        if (cl.hasOption(OPT_MAILBOX_ID)) {
            params.mboxId = Integer.parseInt(cl.getOptionValue(OPT_MAILBOX_ID));
            System.out.printf("Replaying operations for mailbox %d only\n", params.mboxId);
        } else {
            System.out.println("Replaying operations for all mailboxes");
        }

        if (cl.hasOption(OPT_THREADS))
            params.threads = Integer.parseInt(cl.getOptionValue(OPT_THREADS));
        System.out.printf("Using %d redo player threads\n", params.threads);
        if (cl.hasOption(OPT_QUEUE_CAPACITY))
            params.queueCapacity = Integer.parseInt(cl.getOptionValue(OPT_QUEUE_CAPACITY));
        System.out.printf("Using %d as queue capacity for each redo player thread\n", params.queueCapacity);

        List<File> logList = new ArrayList<>();
        if (cl.hasOption(OPT_LOGFILES)) {
            String[] fnames = cl.getOptionValues(OPT_LOGFILES);
            params.logfiles = new File[fnames.length];
          for (String fname : fnames) {
            File f = new File(fname);
            if (f.exists())
              logList.add(f);
            else
              throw new FileNotFoundException("No such file: " + f.getAbsolutePath());
          }
        } else {
            // By default, use /opt/zextras/redolog/archive/*, then /opt/zextras/redolog/redo.log,
            // ordered by log sequence.
            Provisioning prov = Provisioning.getInstance();
            Server server = prov.getLocalServer();
            String archiveDirPath =
                Config.getPathRelativeToZimbraHome(
                        server.getAttr(Provisioning.A_zimbraRedoLogArchiveDir, "redolog/archive")).getAbsolutePath();
            String redoLogPath =
                Config.getPathRelativeToZimbraHome(
                        server.getAttr(Provisioning.A_zimbraRedoLogLogPath, "redolog/redo.log")).getAbsolutePath();

            File archiveDir = new File(archiveDirPath);
            if (archiveDir.exists()) {
                File[] archiveLogs = RolloverManager.getArchiveLogs(archiveDir, params.fromSeq, params.toSeq);
              logList.addAll(Arrays.asList(archiveLogs));
            }
            File redoLog = new File(redoLogPath);
            if (redoLog.exists()) {
                FileLogReader logReader = new FileLogReader(redoLog);
                long seq = logReader.getHeader().getSequence();
                if (params.fromSeq <= seq && seq <= params.toSeq)
                    logList.add(redoLog);
            }
        }
        // Filter out logs based on from/to times.
        for (Iterator<File> iter = logList.iterator(); iter.hasNext(); ) {
            File f = iter.next();
            FileHeader hdr = (new FileLogReader(f)).getHeader();
            if (hdr.getFirstOpTstamp() > params.toTime || (hdr.getLastOpTstamp() < params.fromTime && !hdr.getOpen())) {
                // log is outside the time range
                iter.remove();
                System.out.printf("Redolog %s has no operation in the requested time range\n", f.getName());
            }
        }
        params.logfiles = new File[logList.size()];
        params.logfiles = logList.toArray(params.logfiles);
        System.out.printf("%d redolog files to play back\n", params.logfiles.length);

        return params;
    }


    private Params mParams;
    private RedoPlayer mPlayer;

    public PlaybackUtil(Params params) {
        mParams = params;
        if (mParams.mboxId != Params.MBOX_ID_UNSET || mParams.threads == 1)
            mPlayer = new RedoPlayer(false, true, !mParams.stopOnError, false, true);
        else
            mPlayer = new ParallelRedoPlayer(false, true, !mParams.stopOnError, false,
                                             mParams.threads, mParams.queueCapacity, true);
    }

    public void playback() throws Throwable {
        try {
            for (File redolog : mParams.logfiles) {
                System.out.println("Processing log file: " + redolog.getAbsolutePath());
                long until = mParams.toTime;
                if (until < Long.MAX_VALUE)
                    until++;
                try {
                    Map<Integer, Integer> mboxIdMap = null;
                    if (mParams.mboxId != Params.MBOX_ID_UNSET) {
                        mboxIdMap = new HashMap<>(1);
                        mboxIdMap.put(mParams.mboxId, mParams.mboxId);
                    }
                    mPlayer.scanLog(redolog, true, mboxIdMap, mParams.fromTime, until);
                } catch (OutOfMemoryError oome) {
                    Zimbra.halt("OutOfMemoryError while replaying redolog: " + oome.getMessage(), oome);
                } catch (Throwable t) {
                    if (mParams.stopOnError)
                        throw t;
                    ZimbraLog.redolog.warn("Ignoring error and moving on: " + t.getMessage(), t);
                }
            }
        } finally {
            mPlayer.shutdown();
        }
    }

    public static void main(String[] cmdlineargs) throws Throwable {
        // Bug: 47051
        // for the CLI utilities we need to set the default soap http transport timeout to 0 (no timeout).
        CliUtil.setCliSoapHttpTransportTimeout();
        try {
            CommandLine cl = parseArgs(cmdlineargs);
            Params params = initParams(cl);
            if (params.help) {
                usage(null);
                System.exit(0);
            }
            setup();
            PlaybackUtil player = new PlaybackUtil(params);
            player.playback();
        } finally {
            teardown();
        }
    }

    private static void setup() throws ServiceException {
        // set up log4j
        ZimbraLog.toolSetupLog4j("INFO", LC.zimbra_log4j_properties.value());
        // remove the console appender if any
        Logger rootLogger = LogManager.getRootLogger();
        LoggerContext context = LoggerContext.getContext(false);
        Configuration configuration = context.getConfiguration();
        LoggerConfig loggerConfig = configuration.getLoggerConfig(rootLogger.getName());
        Map<String, Appender> appenders = loggerConfig.getAppenders();
        AtomicReference<Appender> consoleAppender = new AtomicReference<>();
        appenders.values().forEach(
            appender -> {
                if (appender instanceof ConsoleAppender) {
                    consoleAppender.set(appender);
                }
            });
        if (consoleAppender.get() != null) {
            loggerConfig.removeAppender(consoleAppender.get().getName());
            }

        DbPool.startup();
        Zimbra.startupCLI();
    }

    private static void teardown() throws ServiceException {
        Zimbra.shutdown();
    }
}
