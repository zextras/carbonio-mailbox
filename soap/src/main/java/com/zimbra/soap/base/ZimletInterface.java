// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import org.w3c.dom.Element;

public interface ZimletInterface {

  public void setZimletContext(ZimletContextInterface zimletContext);

  public void setZimlet(ZimletDesc zimlet);

  public void setZimletConfig(ZimletConfigInfo zimletConfig);

  public void setZimletHandlerConfig(Element zimletHandlerConfig);

  public ZimletContextInterface getZimletContext();

  public ZimletDesc getZimlet();

  public ZimletConfigInfo getZimletConfig();

  public Element getZimletHandlerConfig();
}
