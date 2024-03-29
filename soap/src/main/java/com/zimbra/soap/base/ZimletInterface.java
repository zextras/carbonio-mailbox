// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import org.w3c.dom.Element;

public interface ZimletInterface {

    void setZimletContext(ZimletContextInterface zimletContext);
    void setZimlet(ZimletDesc zimlet);
    void setZimletConfig(ZimletConfigInfo zimletConfig);
    void setZimletHandlerConfig(Element zimletHandlerConfig);

    ZimletContextInterface getZimletContext();
    ZimletDesc getZimlet();
    ZimletConfigInfo getZimletConfig();
    Element getZimletHandlerConfig();
}
