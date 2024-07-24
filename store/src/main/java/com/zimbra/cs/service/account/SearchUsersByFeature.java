package com.zimbra.cs.service.account;

import com.unboundid.ldap.sdk.Filter;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.service.admin.ToXML;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.message.SearchUsersByFeatureRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zimbra.cs.ldap.LdapConstants.LDAP_TRUE;
import static com.zimbra.cs.ldap.LdapConstants.LDAP_FALSE;

public class SearchUsersByFeature extends AccountDocumentHandler {

  public static final int DEFAULT_MAX_RESULTS = 10;

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(getZimbraSoapContext(context));

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    String query = request.getAttribute(AccountConstants.E_NAME);
    String feature =
        SearchUsersByFeatureRequest.Features.valueOf(request.getAttribute(AccountConstants.E_FEATURE, "UNKNOWN")).getFeature();

    var provisioning = Provisioning.getInstance();

    var options = buildSearchOptions(provisioning, account);
    options.setFilterString(ZLdapFilterFactory.FilterId.ADMIN_SEARCH, getSearchFilter(query, feature, provisioning).toString());

    var entries = provisioning.searchDirectory(options).stream().limit(request.getAttributeInt(AccountConstants.A_LIMIT, DEFAULT_MAX_RESULTS));

    return buildResponse(zsc, entries);
  }

  private static SearchDirectoryOptions buildSearchOptions(Provisioning provisioning,
                                                                  Account account) throws ServiceException {
    var options = new SearchDirectoryOptions();
    options.setTypes(SearchDirectoryOptions.ObjectType.accounts);
    var domain = provisioning.getDomainById(account.getDomainId());
    options.setDomain(domain);
    options.setSortAttr(ZAttrProvisioning.A_uid);
    options.setSortOpt(SearchDirectoryOptions.SortOpt.SORT_ASCENDING);
    return options;
  }

  private static Element buildResponse(ZimbraSoapContext zsc, Stream<NamedEntry> entries) {
    var response = zsc.createElement(AccountConstants.SEARCH_USERS_BY_FEATURE_RESPONSE);
    var attributes = new HashSet<String>();
    attributes.add(ZAttrProvisioning.A_mail);
    attributes.add(ZAttrProvisioning.A_uid);
    attributes.add(ZAttrProvisioning.A_displayName);
    entries.forEach(a -> ToXML.encodeAccount(response, (Account) a, true, attributes, null));
    return response;
  }

  private static Filter getSearchFilter(String query, String feature, Provisioning provisioning) throws ServiceException {
    var autoCompleteFilter = Filter.createORFilter(
        getWildcardFilter(ZAttrProvisioning.A_uid, query),
        getWildcardFilter(ZAttrProvisioning.A_displayName, query),
        getWildcardFilter(ZAttrProvisioning.A_mail, query)
    );
    var notHiddenInGalFilter = Filter.createNOTFilter(
        Filter.createEqualityFilter(ZAttrProvisioning.A_zimbraHideInGal, LDAP_TRUE)
    );

    var featureFilter = getFeatureFilter(feature, provisioning);

    return featureFilter == null
        ? Filter.createANDFilter(autoCompleteFilter, notHiddenInGalFilter)
        : Filter.createANDFilter(autoCompleteFilter, notHiddenInGalFilter, featureFilter);
  }

  private static Filter getFeatureFilter(String feature, Provisioning provisioning) throws ServiceException {
    if (StringUtil.isNullOrEmpty(feature)) {
      return null;
    }
    var accountFeatureFilter = Filter.createEqualityFilter(feature, LDAP_TRUE);

    var cosWithFeature = provisioning.getAllCos().stream().filter(cos -> cos.getAttr(feature, LDAP_FALSE).equals(LDAP_TRUE)).collect(Collectors.toList());
    if (!cosWithFeature.isEmpty()) {
      var cosFilters = cosWithFeature.stream()
          .map(cos -> getCosFeatureFilter(cos, feature)).collect(Collectors.toList());
      return Filter.createORFilter(Stream.concat(Stream.of(accountFeatureFilter), cosFilters.stream()).toArray(Filter[]::new));
    } else {
      return accountFeatureFilter;
    }
  }

  private static Filter getCosFeatureFilter(Cos cos, String feature) {
    return Filter.createANDFilter(
        Filter.createEqualityFilter(ZAttrProvisioning.A_zimbraCOSId, cos.getId()),
        Filter.createNOTFilter(Filter.createEqualityFilter(feature, LDAP_FALSE))
    );
  }

  private static Filter getWildcardFilter(String field, String query) {
    return Filter.createSubstringFilter(field, null, new String[] {StringUtil.isNullOrEmpty(query) ? "." : query}, null);
  }
}
