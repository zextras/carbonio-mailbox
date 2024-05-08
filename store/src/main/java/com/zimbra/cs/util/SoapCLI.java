// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.common.soap.SoapTransport;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Provisioning;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * For command line interface utilities that are SOAP clients and need to authenticate with
 * the admin service using credentials from local configuration.
 * <p>
 * This class takes -h,--help for displaying usage, and -s,--server for target server hostname.
 * Subclass can provide additional options. The expected use is similar to the following:
 * <pre>
 *   MyUtil util = new MyUtil();
 *   try {
 *     util.setupCommandLineOptions();
 *     CommandLine cl = util.getCommandLine(args);
 *     if (cl != null) {
 *       if (cl.hasOption(...)) {
 *         util.auth();
 *         util.doMyThing();
 *       } else if (cl.hasOption(...)) {
 *         ...
 *       }
 *     }
 *   } catch (ParseException e) {
 *     util.usage(e);
 *   }
 *
 * </pre>
 *
 * @author kchen
 *
 */
public abstract class SoapCLI {

    // common options

    public static final String O_AUTHTOKEN = "y";
    public static final String O_AUTHTOKENFILE = "Y";
    public static final String O_H = "h";
    public static final String O_HIDDEN = "hidden";
    public static final String O_S = "s";

    public static final Option OPT_AUTHTOKEN = new Option(O_AUTHTOKEN, "authtoken", true, "use auth token string (has to be in JSON format) from command line");
    public static final Option OPT_AUTHTOKENFILE = new Option(O_AUTHTOKENFILE, "authtokenfile", true, "read auth token (has to be in JSON format) from a file");


    private final String mUser;
    private final String mPassword;
    private String mHost;
    private final int mPort;
    private boolean mAuth;
    private final Options mOptions;
    private final Options mHiddenOptions;
    private final boolean mDisableTargetServerOption;

    private SoapTransport mTrans = null;
    private String mServerUrl;

    protected SoapCLI() throws ServiceException {
        this(false);
    }

    protected SoapCLI(boolean disableTargetServerOption) throws ServiceException {
        // get admin username from local config
        mUser = LC.zimbra_ldap_user.value();
        // get password from localconfig
        mPassword = LC.zimbra_ldap_password.value();
        // host can be specified
        mHost = "localhost";
        // get admin port number from provisioning
        com.zimbra.cs.account.Config conf;
        try {
	        conf = Provisioning.getInstance().getConfig();
        } catch (ServiceException e) {
        	throw ServiceException.FAILURE("Unable to connect to LDAP directory", e);
        }
        mPort = conf.getIntAttr(ZAttrProvisioning.A_zimbraAdminPort, 0);
        if (mPort == 0)
            throw ServiceException.FAILURE("Unable to get admin port number from provisioning", null);
        mOptions = new Options();
        mHiddenOptions = new Options();
        mDisableTargetServerOption = disableTargetServerOption;
    }

    protected void setServer(String hostname) {
        mHost = hostname;
    }

    /**
     * Parses the command line arguments. If -h,--help is specified, displays usage and returns null.
     * @param args the command line arguments
     * @return {@link CommandLine}
     * @throws ParseException if parsing fails
     */
    protected CommandLine getCommandLine(String[] args) throws ParseException {
        CommandLineParser clParser = new GnuParser();
        CommandLine cl;

        Options opts = getAllOptions();
        try {
            cl = clParser.parse(opts, args);
        } catch (ParseException e) {
            if (helpOptionSpecified(args)) {
                usage();
                return null;
            } else
                throw e;
        }
        if (cl.hasOption(O_H)) {
            boolean showHiddenOptions = cl.hasOption(O_HIDDEN);
            usage(null, showHiddenOptions);
            return null;
        }
        if (!mDisableTargetServerOption && cl.hasOption(O_S))
            setServer(cl.getOptionValue(O_S));
        return cl;
    }

    /**
     * Returns an <tt>Options</tt> object that combines the standard options
     * and the hidden ones.
     */
    @SuppressWarnings("unchecked")
    private Options getAllOptions() {
        Options newOptions = new Options();
        Set<OptionGroup> groups = new HashSet<>();
        Options[] optionses =
            new Options[] { mOptions, mHiddenOptions };
        for (Options options : optionses) {
            for (Option opt : (Collection<Option>) options.getOptions()) {
                OptionGroup group = options.getOptionGroup(opt);
                if (group != null) {
                    groups.add(group);
                } else {
                    newOptions.addOption(opt);
                }
            }
        }

        for (OptionGroup group : groups) {
            newOptions.addOptionGroup(group);
        }
        return newOptions;
    }

    private boolean helpOptionSpecified(String[] args) {
        return
            args != null && args.length == 1 &&
            ("-h".equals(args[0]) || "--help".equals(args[0]));
    }


    /**
     *
     * Authenticates using the username and password from the local config.
     *
     * @return {@link Session}
     * @throws IOException if an error occurs
     * @throws ServiceException if an error occurs
     */
    protected Session auth() throws IOException, ServiceException {
        URL url = new URL("https", mHost, mPort, AdminConstants.ADMIN_SERVICE_URI);
        mServerUrl = url.toExternalForm();
        SoapTransport trans = getTransport();
        mAuth = false;

        Element authReq = new Element.XMLElement(AdminConstants.AUTH_REQUEST);
        authReq.addAttribute(AdminConstants.E_NAME, mUser, Element.Disposition.CONTENT);
        authReq.addAttribute(AdminConstants.E_PASSWORD, mPassword, Element.Disposition.CONTENT);
        try {
            Element authResp = trans.invokeWithoutSession(authReq);
            String authToken = authResp.getAttribute(AdminConstants.E_AUTH_TOKEN);
            ZAuthToken zat = new ZAuthToken(null, authToken, null);
            trans.setAuthToken(authToken);
            mAuth = true;
            return new Session(zat, null);
        } catch (UnknownHostException e) {
            // UnknownHostException's error message is not clear; rethrow with a more descriptive message
            throw new IOException("Unknown host: " + mHost);
        }
    }

    /**
     * Encapsulate the notion of a session, including auth token, session ID, and
     * whatever else is desired...
     */
    public static class Session {

        private ZAuthToken mAuthToken;

        private String mSessionID;

        public ZAuthToken getAuthToken() {
            return mAuthToken;
        }

        public String getSessionID() {
            return mSessionID;
        }

        public void setAuthToken(ZAuthToken a) {
            mAuthToken = a;
        }

        public void setSessionID(String s) {
            mSessionID = s;
        }

        public Session(ZAuthToken authToken, String sessionID) {
            mAuthToken = authToken;
            mSessionID = sessionID;
        }
    }

    /**
     * Authenticates using the provided ZAuthToken
     * @throws IOException if an error occurs
     * @throws ServiceException if an error occurs
     * @return {@link Session}
     */
    protected Session auth(ZAuthToken zAuthToken) throws IOException, ServiceException {
        if (zAuthToken == null)
            return auth();

        URL url = new URL("https", mHost, mPort, AdminConstants.ADMIN_SERVICE_URI);
        mServerUrl = url.toExternalForm();
        SoapTransport trans = getTransport();
        mAuth = false;

        Element authReq = new Element.XMLElement(AdminConstants.AUTH_REQUEST);
        zAuthToken.encodeAuthReq(authReq, true);
        try {
            Element authResp = trans.invokeWithoutSession(authReq);
            ZAuthToken zat = new ZAuthToken(authResp.getElement(AdminConstants.E_AUTH_TOKEN), true);
            trans.setAuthToken(zat);
            mAuth = true;
            return new Session(zat, null);
        } catch (UnknownHostException e) {
            // UnknownHostException's error message is not clear; rethrow with a more descriptive message
            throw new IOException("Unknown host: " + mHost);
        }
    }

    /**
     * Sets up expected command line options. This class adds -h for help and -s for server.
     *
     */
    protected void setupCommandLineOptions() {
        if (!mDisableTargetServerOption) {
            Option s = new Option(O_S, "server", true, "Mail server hostname. Default is localhost.");
            mOptions.addOption(s);
        }
        mOptions.addOption(O_H, "help", false, "Displays this help message.");
        mHiddenOptions.addOption(null, O_HIDDEN, false, "Include hidden options in help output");
    }

    /**
     * Displays usage to stdout.
     *
     */
    protected void usage() {
        usage(null);
    }

    /**
     * Displays usage to stdout.
     * @param e parse error
     */
    protected void usage(ParseException e) {
        usage(e, false);
    }

    protected void usage(ParseException e, boolean showHiddenOptions) {
        if (e != null) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
        }

        Options opts = showHiddenOptions ? getAllOptions() : mOptions;
        PrintWriter pw = new PrintWriter(System.err, true);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, formatter.getWidth(), getCommandUsage(),
                null, opts, formatter.getLeftPadding(), formatter.getDescPadding(),
                null);
        pw.flush();

        String trailer = getTrailer();
        if (trailer != null && trailer.length() > 0) {
            System.err.println();
            System.err.println(trailer);
        }
    }

    /**
     * Returns the command usage. Since most CLI utilities are wrapped into shell script, the name of
     * the script should be returned.
     * @return the command usage string
     */
    protected abstract String getCommandUsage();

    /**
     * Returns the trailer in the usage message. Subclass can add additional notes on the usage.
     * @return the trailer in the usage message
     */
    protected String getTrailer() {
        return "";
    }

    /**
     * Returns whether this command line SOAP client has been authenticated.
     * @return whether this command line SOAP client has been authenticated
     */
    protected boolean isAuthenticated() {
        return mAuth;
    }

    /**
     * @return Returns the username.
     */
    protected String getUser() {
        return mUser;
    }

    /**
     * @return Returns the target server hostname.
     */
    protected String getServer() {
        return mHost;
    }


    protected int getPort() {
        return mPort;
    }

    /**
     * Gets the SOAP transport.
     * @return null if the SOAP client has not been authenticated.
     */
    protected SoapTransport getTransport() {
        if (mTrans == null)
            initTransport();
        return mTrans;
    }

    private void initTransport() {
        SoapHttpTransport trans = new SoapHttpTransport(mServerUrl);
        trans.setRetryCount(1);
        mTrans = trans;
    }

    /**
     * Set the SOAP transport read timeout
     */
    public void setTransportTimeout(int newTimeout) {
        getTransport().setTimeout(newTimeout);
    }

    protected String getServerUrl() {
        return mServerUrl;
    }

    /**
     * @return Gets the options that has been set up so far.
     */
    protected Options getOptions() {
        return mOptions;
    }

    protected Options getHiddenOptions() {
        return mHiddenOptions;
    }

    // helper for options that specify date/time

    private static final String[] DATETIME_FORMATS = {
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd HH:mm:ss SSS",
        "yyyy/MM/dd HH:mm:ss.SSS",
        "yyyy/MM/dd-HH:mm:ss-SSS",
        "yyyy/MM/dd-HH:mm:ss",
        "yyyyMMdd.HHmmss.SSS",
        "yyyyMMdd.HHmmss",
        "yyyyMMddHHmmssSSS",
        "yyyyMMddHHmmss"
    };
    public static final String CANONICAL_DATETIME_FORMAT = DATETIME_FORMATS[0];

    public static Date parseDatetime(String str) {
        for (String formatStr: DATETIME_FORMATS) {
            SimpleDateFormat fmt = new SimpleDateFormat(formatStr);
            fmt.setLenient(false);
            ParsePosition pp = new ParsePosition(0);
            Date d = fmt.parse(str, pp);
            if (d != null && pp.getIndex() == str.length())
                return d;
        }
        return null;
    }

    public static String getAllowedDatetimeFormatsHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Specify date/time in one of these formats:\n\n");
        Date d = new Date();
        for (String formatStr: DATETIME_FORMATS) {
            SimpleDateFormat fmt = new SimpleDateFormat(formatStr);
            String s = fmt.format(d);
            sb.append("    ").append(s).append("\n");
        }
        sb.append("\n");

        sb.append(
            "Specify year, month, date, hour, minute, second, and optionally millisecond.\n");
        sb.append(
            "Month/date/hour/minute/second are 0-padded to 2 digits, millisecond to 3 digits.\n");
        sb.append(
            "Hour must be specified in 24-hour format, and time is in local time zone.\n");
        return sb.toString();
    }

    public static ZAuthToken getZAuthToken(CommandLine cl) throws ServiceException, ParseException, IOException {
        if (cl.hasOption(O_AUTHTOKEN) && cl.hasOption(O_AUTHTOKENFILE)) {
            String msg = String.format("cannot specify both %s and %s options",
                    O_AUTHTOKEN, O_AUTHTOKENFILE);
            throw new ParseException(msg);
        }

        if (cl.hasOption(O_AUTHTOKEN)) {
            return ZAuthToken.fromJSONString(cl.getOptionValue(O_AUTHTOKEN));
        }

        if (cl.hasOption(O_AUTHTOKENFILE)) {
            String authToken = StringUtil.readSingleLineFromFile(cl.getOptionValue(O_AUTHTOKENFILE));
            return ZAuthToken.fromJSONString(authToken);
        }

        return null;
    }
}
