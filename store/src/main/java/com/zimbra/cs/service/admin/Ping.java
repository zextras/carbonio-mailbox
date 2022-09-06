// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.PingResponse;
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class Ping extends AdminDocumentHandler {

  @Override
  public boolean needsAuth(Map<String, Object> context) {
    if (DebugConfig.allowUnauthedPing) {
      return false;
    } else {
      return super.needsAuth(context);
    }
  }

  @Override
  public boolean needsAdminAuth(Map<String, Object> context) {
    if (DebugConfig.allowUnauthedPing) {
      return false;
    } else {
      return super.needsAdminAuth(context);
    }
  }

  /* (non-Javadoc)
   * @see com.zimbra.soap.DocumentHandler#handle(org.dom4j.Element, java.util.Map)
   */
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext lc = getZimbraSoapContext(context);
    return lc.jaxbToElement(new PingResponse());
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
  }
}
