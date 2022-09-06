// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.service.formatter.ContactCSV;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class GetAvailableCsvFormats extends DocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    Element response = zsc.createElement(AccountConstants.GET_AVAILABLE_CSV_FORMATS_RESPONSE);
    for (String format : ContactCSV.getAllFormatNames())
      response.addElement(AccountConstants.E_CSV).addAttribute(AccountConstants.A_NAME, format);
    return response;
  }
}
