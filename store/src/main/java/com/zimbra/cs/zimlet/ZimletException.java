// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

/**
 * 
 * @author jylee
 *
 */
@SuppressWarnings("serial")
public class ZimletException extends Exception {

    private ZimletException(String msg) {
        super(msg);
    }

    private ZimletException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public static ZimletException ZIMLET_HANDLER_ERROR(String msg) {
        return new ZimletException(msg);
    }

    public static ZimletException INVALID_ZIMLET_DESCRIPTION(String msg) {
        return new ZimletException(msg);
    }

    public static ZimletException INVALID_ZIMLET_CONFIG(String msg) {
        return new ZimletException(msg);
    }

    public static ZimletException INVALID_ZIMLET_NAME() {
        return new ZimletException("Zimlet name may contain only letters, numbers and the following symbols: '.', '-' and '_'");
    }

    public static ZimletException INVALID_ZIMLET_NAME(String msg) {
        return new ZimletException(msg);
    }

    public static ZimletException INVALID_ZIMLET_ENTRY(String entry) {
        return new ZimletException(String.format("Invalid entry in Zimlet archive: %s", entry));
    }

    public static ZimletException INVALID_ABSOLUTE_PATH(String entry) {
        return new ZimletException(String.format("Invalid entry in Zimlet archive: %s. Zimlet entries with absolute paths are not allowed.", entry));
    }

    public static ZimletException CANNOT_DEPLOY(String zimlet, Throwable cause) {
        return new ZimletException("Cannot deploy Zimlet " + zimlet, cause);
    }

    public static ZimletException CANNOT_DEPLOY(String zimlet, String msg, Throwable cause) {
        return new ZimletException(String.format("Cannot deploy Zimlet %s. Error message: %s", zimlet, msg), cause);
    }

    public static ZimletException CANNOT_CREATE(String zimlet, Throwable cause) {
        return new ZimletException("Cannot create Zimlet " + zimlet, cause);
    }

    public static ZimletException CANNOT_DELETE(String zimlet, Throwable cause) {
        return new ZimletException("Cannot delete Zimlet " + zimlet, cause);
    }

    public static ZimletException CANNOT_ACTIVATE(String zimlet, Throwable cause) {
        return new ZimletException("Cannot activate Zimlet " + zimlet, cause);
    }

    public static ZimletException CANNOT_DEACTIVATE(String zimlet, Throwable cause) {
        return new ZimletException("Cannot deactivate Zimlet " + zimlet, cause);
    }

    public static ZimletException CANNOT_ENABLE(String zimlet, Throwable cause) {
        return new ZimletException("Cannot enable Zimlet " + zimlet, cause);
    }

    public static ZimletException CANNOT_DISABLE(String zimlet, Throwable cause) {
        return new ZimletException("Cannot disable Zimlet " + zimlet, cause);
    }

    public static ZimletException CANNOT_FLUSH_CACHE(Throwable cause) {
        return new ZimletException("Cannot flush zimlet cache", cause);
    }
}