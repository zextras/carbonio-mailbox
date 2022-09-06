// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class ModifyConfig extends AdminDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = AdminService.getAttrs(request);

    Config config = prov.getConfig();
    checkRight(zsc, context, config, attrs);

    // pass in true to checkImmutable
    prov.modifyAttrs(config, attrs, true);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {
              "cmd", "ModifyConfig",
            },
            attrs));

    Element response = zsc.createElement(AdminConstants.MODIFY_CONFIG_RESPONSE);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(
        String.format(
            AdminRightCheckPoint.Notes.MODIFY_ENTRY,
            Admin.R_modifyGlobalConfig.getName(),
            "global config"));
  }
}
