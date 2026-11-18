// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.session.RemoteSoapSession;
import com.zimbra.cs.session.SoapSession;

public class SoapSessionFactory {

    private static SoapSessionFactory sSessionFactory = null;

    public static synchronized SoapSessionFactory getInstance() {
        if (sSessionFactory == null) {
            sSessionFactory = new SoapSessionFactory();
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
