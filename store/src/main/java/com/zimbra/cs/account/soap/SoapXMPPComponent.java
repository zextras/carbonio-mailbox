// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.XMPPComponent;
import java.util.Map;

/** */
public class SoapXMPPComponent extends XMPPComponent implements SoapEntry {

  SoapXMPPComponent(Element e, Provisioning prov) throws ServiceException {
    super(
        e.getAttribute(AdminConstants.A_NAME),
        e.getAttribute(AdminConstants.A_ID),
        SoapProvisioning.getAttrs(e),
        prov);
  }

  public void modifyAttrs(
      SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable)
      throws ServiceException {}

  public void reload(SoapProvisioning prov) throws ServiceException {}
}
