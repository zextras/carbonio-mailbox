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
import com.zimbra.cs.account.AccessManager.AttrRightChecker;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author schemers
 */
public class GetAllConfig extends AdminDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    Config config = prov.getConfig();

    AdminAccessControl aac = checkRight(zsc, context, config, AdminRight.PR_ALWAYS_ALLOW);

    Element response = zsc.createElement(AdminConstants.GET_ALL_CONFIG_RESPONSE);
    encodeConfig(response, config, null, aac.getAttrRightChecker(config));

    return response;
  }

  public static void encodeConfig(
      Element e, Config config, Set<String> reqAttrs, AttrRightChecker attrRightChecker) {
    Map attrs = config.getUnicodeAttrs();
    ToXML.encodeAttrs(e, attrs, reqAttrs, attrRightChecker);
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_getGlobalConfig);
  }
}
