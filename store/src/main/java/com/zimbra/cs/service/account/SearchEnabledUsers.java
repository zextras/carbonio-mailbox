package com.zimbra.cs.service.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.soap.ZimbraSoapContext;

import java.util.Map;

public class SearchEnabledUsers extends AccountDocumentHandler {
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(getZimbraSoapContext(context));

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    Element response = zsc.createElement(AccountConstants.SEARCH_ENABLED_USERS_RESPONSE);
    return response;
  }
}
