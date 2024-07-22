package com.zimbra.cs.service.account;

import com.unboundid.ldap.sdk.Filter;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.service.admin.ToXML;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.message.SearchEnabledUsersRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    var autoCompleteFilter = Filter.createORFilter(
        getWildcardFilter("uid", query),
        getWildcardFilter("displayName", query),
        getWildcardFilter("mail", query)
    );
    var notHiddenInGalFilter = Filter.createNOTFilter(
        Filter.createEqualityFilter("zimbraHideInGal", "TRUE")
    );

    Filter featureFilter = null;
    if (!StringUtil.isNullOrEmpty(feature)) {
      var accountFeatureFilter = Filter.createEqualityFilter(feature, "TRUE");

      var cosWithFeature = provisioning.getAllCos().stream().filter(cos -> cos.getAttr(feature, "FALSE").equals("TRUE")).collect(Collectors.toList());
      if (!cosWithFeature.isEmpty()) {
        var cosFilters = cosWithFeature.stream()
            .map(cos -> getCosFeatureFilter(cos, feature)).collect(Collectors.toList());
        featureFilter = Filter.createORFilter(Stream.concat(Stream.of(accountFeatureFilter), cosFilters.stream()).toArray(Filter[]::new));
      } else {
        featureFilter = accountFeatureFilter;
      }
    }

    var filter = featureFilter == null ? Filter.createANDFilter(autoCompleteFilter, notHiddenInGalFilter) : Filter.createANDFilter(autoCompleteFilter, notHiddenInGalFilter, featureFilter);
    options.setFilterString(ZLdapFilterFactory.FilterId.ADMIN_SEARCH, filter.toString());

    var entries = provisioning.searchDirectory(options);

    var response = zsc.createElement(AccountConstants.SEARCH_ENABLED_USERS_RESPONSE);
    var attributes = new HashSet<String>();
    attributes.add("mail");
    attributes.add("uid");
    attributes.add("displayName");
    entries.forEach(a -> ToXML.encodeAccount(response, (Account) a, true, attributes, null));
    return response;
  }

  private static Filter getCosFeatureFilter(Cos cos, String feature) {
    return Filter.createANDFilter(
        Filter.createEqualityFilter("zimbraCOSId", cos.getId()),
        Filter.createNOTFilter(Filter.createEqualityFilter(feature, "FALSE"))
    );
  }

  private static Filter getWildcardFilter(String field, String query) {
    return Filter.createSubstringFilter(field, null, new String[] {query}, null);
  }
}
