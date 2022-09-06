// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetVersionInfoResponse;
import com.zimbra.soap.admin.type.VersionInfo;
import java.util.List;
import java.util.Map;

public class GetVersionInfo extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext lc = getZimbraSoapContext(context);

    VersionInfo versionInfo = new VersionInfo();
    String fullVersionInfo = BuildInfo.VERSION;
    if (!StringUtil.isNullOrEmpty(BuildInfo.TYPE)) {
      fullVersionInfo = fullVersionInfo + "." + BuildInfo.TYPE;
      versionInfo.setType(BuildInfo.TYPE);
    }
    versionInfo.setVersion(fullVersionInfo);
    versionInfo.setRelease(BuildInfo.RELEASE);
    versionInfo.setBuildDate(BuildInfo.DATE);
    versionInfo.setHost(BuildInfo.HOST);

    versionInfo.setMajorVersion(BuildInfo.MAJORVERSION);
    versionInfo.setMinorVersion(BuildInfo.MINORVERSION);
    versionInfo.setMicroVersion(BuildInfo.MICROVERSION);
    versionInfo.setPlatform(BuildInfo.PLATFORM);
    GetVersionInfoResponse resp = new GetVersionInfoResponse(versionInfo);
    return lc.jaxbToElement(resp);
  }

  @Override
  public boolean needsAdminAuth(Map<String, Object> context) {
    return true;
  }

  @Override
  public boolean needsAuth(Map<String, Object> context) {
    return false;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
  }
}
