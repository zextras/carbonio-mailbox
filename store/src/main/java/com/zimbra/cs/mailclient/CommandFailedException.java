// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient;

import java.net.SocketTimeoutException;

/**
 * Indicates that a mail protocol command has failed.
 */
public class CommandFailedException extends MailException {
    private final String cmd;
    private final String error;
    private String request; // Optional request that caused the error

    /**
     * Creates a new <tt>CommandFailedException</tt> for the specified
     * command and detail message.
     * 
     * @param cmd the name of the failed command
     * @param error the error message, or <tt>null</tt> if none
     */
    public CommandFailedException(String cmd, String error) {
        this.cmd = cmd;
        this.error = error;
    }

    /**
     * Returns the name of the command that failed.
     * 
     * @return the name of the failed command
     */
    public String getCommand() {
        return cmd;
    }

    /**
     * Returns the error that caused the failure
     *
     * @return the error message, or <tt>null</tt> if unknown
     */
    public String getError() {
        return error;
    }

    public void setRequest(String req) {
        request = req;
    }

    public String getRequest() {
        return request;
    }

    /**
     * Returns true if use of mail connection can continue even after this
     * command failure.
     * 
     * @return true if the mail connection is still usable
     */
    public boolean canContinue() {
        Throwable e = getCause();
        return e == null || e instanceof MailException && !(e instanceof ParseException);
    }

    public boolean isTimeout() {
        return getCause() instanceof SocketTimeoutException;
    }
    
    /**
     * Returns the exception detail message.
     * 
     * @return the exception detail message
     */
    @Override
    public String getMessage() {
        return error != null ? cmd + " failed: " + error : cmd + " failed";
    }
}
