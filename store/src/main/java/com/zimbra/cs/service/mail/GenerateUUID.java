// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.ldap.LdapUtil;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class GenerateUUID extends MailDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element response = zsc.createElement(MailConstants.GENERATE_UUID_RESPONSE);
    response.setText(LdapUtil.generateUUID());
    return response;
  }
}
