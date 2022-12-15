// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ExtensionUtil;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * IOException handler
 * Default implementation does nothing; may override to perform special handling of IOExceptions
 * 
 */
public class IOExceptionHandler {

    private static IOExceptionHandler instance;
    
    public static synchronized IOExceptionHandler getInstance() {
        if (instance == null) {
            String className = LC.data_source_ioexception_handler_class.value();
            if (className != null && !className.equals("")) {
                try {
                    try {
                        instance = (IOExceptionHandler) Class.forName(className).newInstance();
                    } catch (ClassNotFoundException cnfe) {
                        // ignore and look in extensions
                        instance = (IOExceptionHandler) ExtensionUtil.findClass(className).newInstance();
                    }
                } catch (Exception e) {
                    ZimbraLog.account.error("could not instantiate IOExceptionHandler child class '" + className + "'; defaulting to IOExceptionHandler", e);
                }
            }
            if (instance == null)
                instance = new IOExceptionHandler();
        }
        return instance;
    }
    
    public boolean isRecoverable(Mailbox mbox, long itemId, String message, Exception exception) throws ServiceException {
        return false;
    }
    
    public void trackSyncItem(Mailbox mbox, long itemId) {
    }

    public void checkpointIOExceptionRate(Mailbox mbox) throws ServiceException {
    }

    public void resetSyncCounter(Mailbox mbox) {
    }
}
