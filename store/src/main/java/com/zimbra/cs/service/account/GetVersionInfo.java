// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class GetVersionInfo extends AccountDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    if (!Provisioning.getInstance()
        .getLocalServer()
        .getBooleanAttr(Provisioning.A_zimbraSoapExposeVersion, false)) {
      throw ServiceException.PERM_DENIED("Version info is not available.");
    }
    ZimbraSoapContext lc = getZimbraSoapContext(context);

    Element response = lc.createElement(AccountConstants.GET_VERSION_INFO_RESPONSE);
    Element infoEl = response.addElement(AccountConstants.E_VERSION_INFO_INFO);

    String fullVersionInfo = BuildInfo.VERSION;
    if (!StringUtil.isNullOrEmpty(BuildInfo.TYPE))
      fullVersionInfo = fullVersionInfo + "." + BuildInfo.TYPE;

    infoEl.addAttribute(AccountConstants.A_VERSION_INFO_VERSION, fullVersionInfo);
    infoEl.addAttribute(AccountConstants.A_VERSION_INFO_RELEASE, BuildInfo.RELEASE);
    infoEl.addAttribute(AccountConstants.A_VERSION_INFO_DATE, BuildInfo.DATE);
    infoEl.addAttribute(AccountConstants.A_VERSION_INFO_HOST, BuildInfo.HOST);
    return response;
  }

  public boolean needsAdminAuth(Map<String, Object> context) {
    return false;
  }

  public boolean needsAuth(Map<String, Object> context) {
    return false;
  }
}
