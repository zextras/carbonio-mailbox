// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpException;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.util.CliUtil;
import com.zimbra.cs.util.SoapCLI;

public class FixCalendarEndTimeUtil extends SoapCLI {

    protected static final String O_ACCOUNT = "a";
    protected static final String O_SYNC = "sync";

    protected void setupCommandLineOptions() {
        super.setupCommandLineOptions();
        Options options = getOptions();
        Option accountOpt = new Option(O_ACCOUNT, "account", true,
                "account email addresses seperated by white space or \"all\" for all accounts");
        accountOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(accountOpt);
        options.addOption(new Option(null, O_SYNC, false,
                "run synchronously; default is asynchronous"));
        options.addOption(SoapCLI.OPT_AUTHTOKEN);
        options.addOption(SoapCLI.OPT_AUTHTOKENFILE);
    }

    protected String getCommandUsage() {
        return "zmfixcalendtime -a <account(s)> [options]";
    }

    public FixCalendarEndTimeUtil() throws ServiceException {
        super();
        setupCommandLineOptions();
    }

    public static void main(String[] args) {
        CliUtil.toolSetup();
        FixCalendarEndTimeUtil util = null;
        try {
            util = new FixCalendarEndTimeUtil();
        } catch (ServiceException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        try {
            CommandLine cl = util.getCommandLine(args);
            if (cl == null)
                return;
            util.doit(getZAuthToken(cl), cl.getOptionValues(O_ACCOUNT), cl.hasOption(O_SYNC));
            System.exit(0);
        } catch (ParseException e) {
            util.usage(e);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            util.usage(null);
        }
        System.exit(1);
    }

    private void doit(ZAuthToken zat, String[] accts, boolean sync)
    throws SoapFaultException, IOException, ServiceException, HttpException {
        Element req = new Element.XMLElement(AdminConstants.FIX_CALENDAR_END_TIME_REQUEST);
        if (accts == null || accts.length == 0)
            throw ServiceException.INVALID_REQUEST("Missing -" + O_ACCOUNT + " option", null);
        for (String acct : accts) {
            Element acctElem = req.addElement(AdminConstants.E_ACCOUNT);
            acctElem.addAttribute(AdminConstants.A_NAME, acct);
        }
        if (sync)
            req.addAttribute(AdminConstants.A_TZFIXUP_SYNC, true);

        auth(zat);
        getTransport().invokeWithoutSession(req);
    }
}
