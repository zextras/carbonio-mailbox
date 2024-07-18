package com.zimbra.cs.service.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.service.admin.ToXML;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.message.SearchEnabledUsersRequest;

import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchEnabledUsers extends AccountDocumentHandler {
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(getZimbraSoapContext(context));

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    String query = request.getAttribute(AccountConstants.E_NAME);
    String feature =
        SearchEnabledUsersRequest.Features.valueOf(request.getAttribute(AccountConstants.E_FEATURE, "UNKNOWN")).getFeature();

    var options = new SearchDirectoryOptions();
    options.setTypes(SearchDirectoryOptions.ObjectType.accounts);

    var provisioning = Provisioning.getInstance();
    String cosFilter = "";
    if (!StringUtil.isNullOrEmpty(feature)) {
      cosFilter = provisioning.getAllCos().stream().filter(cos -> cos.getAttr(feature, "FALSE").equals("TRUE"))
          .map(cos -> MessageFormat.format("(zimbraCOSId={0})", cos.getId())).collect(Collectors.joining());
    }

    var featureFilter = StringUtil.isNullOrEmpty(feature) ? "" : MessageFormat.format("({0}=TRUE)", feature);
    var autoCompleteFilter = MessageFormat.format("|{0}{1}{2}",
        getWildcardFilter(query, "uid"),
        getWildcardFilter(query, "displayName"),
        getWildcardFilter(query, "mail")
    );
    String filter = MessageFormat.format("|(&({0}){1}){2}", autoCompleteFilter, featureFilter, cosFilter);
    options.setFilterString(ZLdapFilterFactory.FilterId.ADMIN_SEARCH, filter);


    var entries = provisioning.searchDirectory(options);

    var response = zsc.createElement(AccountConstants.SEARCH_ENABLED_USERS_RESPONSE);
    entries.forEach(a -> ToXML.encodeAccount(response, (Account) a));
    return response;
  }

  private static String getWildcardFilter(String query, String field) {
    return MessageFormat.format("({0}=*{1}*)", field, query);
  }
}
