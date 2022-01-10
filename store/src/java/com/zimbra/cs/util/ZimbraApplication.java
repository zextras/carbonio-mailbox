// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import java.util.List;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;

/**
 * Zimbra Servers enable/disable settings overridable by LC.
 */

public class ZimbraApplication {

    private static ZimbraApplication sServices;

    public static ZimbraApplication getInstance() {
        if (sServices == null) {
            String className = LC.zimbra_class_application.value();
            if (className != null && !className.equals("")) {
                try {
                    sServices = (ZimbraApplication)Class.forName(className)
                        .newInstance();
                } catch (Exception e) {
                    ZimbraLog.misc.error(
                        "could not instantiate ZimbraServices interface of class '"
                            + className + "'; defaulting to ZimbraServices", e);
                }
            }
            if (sServices == null)
                sServices = new ZimbraApplication();
        }
        return sServices;
    }

    public String getId() {
        return "zimbra";
    }

    public String getClientId() {
        return "01234567-89AB-CDEF--FEDC-BA9876543210";
    }

    public boolean supports(String className) {
        return true;
    }

    public boolean supports(Class cls) {
        return supports(cls.getName());
    }

    public void startup() {}

    public void initialize(boolean forMailboxd) {}

    public void initializeZimbraDb(boolean forMailboxd) throws ServiceException {}

    private boolean isShutdown;

    public void shutdown() {
        isShutdown = true;
    }

    public boolean isShutdown() {
        return isShutdown;
    }
    
    public void addExtensionName(String name) {
        assert false;
    }
    
    public List<String> getExtensionNames() {
        assert false;
        return null;
    }
}
