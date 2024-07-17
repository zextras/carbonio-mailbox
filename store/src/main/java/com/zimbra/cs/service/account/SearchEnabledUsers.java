package com.zimbra.cs.service.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.service.admin.ToXML;
import com.zimbra.soap.ZimbraSoapContext;

import java.text.MessageFormat;
import java.util.Map;

public class SearchEnabledUsers extends AccountDocumentHandler {
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(getZimbraSoapContext(context));

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    String query = request.getAttribute(AccountConstants.E_NAME);

    var options = new SearchDirectoryOptions();
    options.setTypes(SearchDirectoryOptions.ObjectType.accounts);
    String filter = MessageFormat.format("|(uid=*{0}*)(displayName=*{0}*)", query);
    options.setFilterString(ZLdapFilterFactory.FilterId.ADMIN_SEARCH, filter);

    var entries = Provisioning.getInstance().searchDirectory(options);

    var response = zsc.createElement(AccountConstants.SEARCH_ENABLED_USERS_RESPONSE);
    entries.forEach(a -> ToXML.encodeAccount(response, (Account) a));
    return response;
  }
}
