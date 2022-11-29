// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ExtensionUtil;
import com.zimbra.cs.session.RemoteSoapSession;
import com.zimbra.cs.session.SoapSession;

public class SoapSessionFactory {

    private static SoapSessionFactory sSessionFactory = null;

    public synchronized static SoapSessionFactory getInstance() {
        if (sSessionFactory == null) {
            String className = LC.zimbra_class_soapsessionfactory.value();
            if (className != null && !className.equals("")) {
                try {
                    try {
                        sSessionFactory = (SoapSessionFactory) Class.forName(className).newInstance();
                    } catch (ClassNotFoundException cnfe) {
                        // ignore and look in extensions
                        sSessionFactory = (SoapSessionFactory) ExtensionUtil.findClass(className).newInstance();
                    }
                } catch (Exception e) {
                    ZimbraLog.account.error("could not instantiate SoapSessionFactory class '" + className + "'; defaulting to SoapSessionFactory", e);
                }
            }
            if (sSessionFactory == null) {
                sSessionFactory = new SoapSessionFactory();
            }
        }
        return sSessionFactory;
    }

    public SoapSession getSoapSession(ZimbraSoapContext zsc) throws ServiceException {
        if (zsc.isAuthUserOnLocalhost()) {
            return new SoapSession(zsc);
        } else {
            return new RemoteSoapSession(zsc);
        }
    }
}
