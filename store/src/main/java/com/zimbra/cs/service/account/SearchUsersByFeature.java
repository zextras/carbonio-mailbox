package com.zimbra.cs.service.account;

import com.unboundid.ldap.sdk.Filter;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.service.admin.ToXML;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.message.SearchUsersByFeatureRequest;
import io.vavr.control.Option;

import java.util.Collections;
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
    var allDomains = provisioning.getConfig().isCarbonioSearchAllDomainsByFeature();
    var domain = provisioning.getDomainById(account.getDomainId());

    var options = buildSearchOptions(domain, allDomains);

    options.setFilterString(ZLdapFilterFactory.FilterId.ADMIN_SEARCH, getSearchFilter(query, feature, provisioning, allDomains, domain).toString());

    var searchResult = provisioning.searchDirectory(options);
    var offset = request.getAttributeInt(AccountConstants.A_OFFSET, 0);
    var limit = request.getAttributeInt(AccountConstants.A_LIMIT, DEFAULT_MAX_RESULTS);
    var entries = searchResult.stream()
        .skip(offset)
        .limit(limit);

    boolean more = searchResult.size() > offset + limit;
    return buildResponse(zsc, entries, searchResult.size(), more);
  }

  private static SearchDirectoryOptions buildSearchOptions(Domain domain, boolean allDomains) throws ServiceException {
    var options = new SearchDirectoryOptions();
    options.setTypes(SearchDirectoryOptions.ObjectType.accounts);
    if (!allDomains) {
      options.setDomain(domain);
    }
    options.setSortAttr(ZAttrProvisioning.A_displayName);
    options.setSortOpt(SearchDirectoryOptions.SortOpt.SORT_ASCENDING);
    return options;
  }

  private static Element buildResponse(ZimbraSoapContext zsc, Stream<NamedEntry> entries, int total, boolean more) {
    var response = zsc.createElement(AccountConstants.SEARCH_USERS_BY_FEATURE_RESPONSE);
    var attributes = new HashSet<String>();
    attributes.add(ZAttrProvisioning.A_mail);
    attributes.add(ZAttrProvisioning.A_uid);
    attributes.add(ZAttrProvisioning.A_displayName);
    response.addAttribute(AccountConstants.A_TOTAL, total);
    response.addAttribute(AccountConstants.A_MORE, more);
    entries.forEach(a -> ToXML.encodeAccount(response, (Account) a, true, attributes, null));
    return response;
  }

  private static Filter getSearchFilter(String query, String feature, Provisioning provisioning, boolean allDomains, Domain userDomain) throws ServiceException {
    var autoCompleteFilter = Filter.createORFilter(
        getWildcardFilter(ZAttrProvisioning.A_uid, query),
        getWildcardFilter(ZAttrProvisioning.A_displayName, query),
        getWildcardFilter(ZAttrProvisioning.A_mail, query)
    );
    var notHiddenInGalFilter = Filter.createNOTFilter(
        Filter.createEqualityFilter(ZAttrProvisioning.A_zimbraHideInGal, LDAP_TRUE)
    );

    var featureFilter = getFeatureFilter(feature, provisioning, allDomains, userDomain);

    return featureFilter
        .map(f ->  Filter.createANDFilter(autoCompleteFilter, notHiddenInGalFilter, f))
        .getOrElse(Filter.createANDFilter(autoCompleteFilter, notHiddenInGalFilter));

  }

  private static Option<Filter> getFeatureFilter(String feature, Provisioning provisioning, boolean allDomains, Domain userDomain) throws ServiceException {
    if (StringUtil.isNullOrEmpty(feature)) {
      return Option.none();
    }
    var accountFeatureFilter = Filter.createEqualityFilter(feature, LDAP_TRUE);

    Map<String, Cos> defaultCOSes = allDomains
        ? provisioning.getAllDomains().stream().filter(d -> getDefaultCOS(provisioning, d) != null)
          .collect(Collectors.toMap(Domain::getId, d -> getDefaultCOS(provisioning, d)))
        : Collections.singletonMap(userDomain.getId(), provisioning.getDefaultCOS(userDomain));

    var cosWithFeature = provisioning.getAllCos().stream().filter(cos -> cos.getAttr(feature, LDAP_FALSE).equals(LDAP_TRUE)).collect(Collectors.toList());
    if (!cosWithFeature.isEmpty()) {
      var cosFilters = cosWithFeature.stream()
          .map(cos -> getCosFeatureFilter(cos, feature)).collect(Collectors.toList());
      for (var pair : defaultCOSes.entrySet()) {
        var defaultCos = pair.getValue();
        if (defaultCos != null && defaultCos.getAttr(feature, LDAP_FALSE).equals(LDAP_TRUE)) {
          if (allDomains) {
            cosFilters.add(getDefaultCosFeatureFilterForDomain(provisioning.getDomainById(pair.getKey()), feature));
          } else {
            cosFilters.add(getDefaultCosFeatureFilter(feature));
          }
        }
      }
      return Option.of(Filter.createORFilter(Stream.concat(Stream.of(accountFeatureFilter), cosFilters.stream()).toArray(Filter[]::new)));
    } else {
      return Option.of(accountFeatureFilter);
    }
  }

  private static Cos getDefaultCOS(Provisioning provisioning, Domain d) {
    try {
      return provisioning.getDefaultCOS(d);
    } catch (ServiceException e) {
      return null;
    }
  }

  private static Filter getDefaultCosFeatureFilter(String feature) {
    return Filter.createANDFilter(
        Filter.createNOTFilter(Filter.createPresenceFilter(ZAttrProvisioning.A_zimbraCOSId)),
        Filter.createNOTFilter(Filter.createEqualityFilter(feature, LDAP_FALSE))
    );
  }

  private static Filter getDefaultCosFeatureFilterForDomain(Domain domain, String feature) {
    return Filter.createANDFilter(
        Filter.createSubstringFilter(ZAttrProvisioning.A_mail, null, null, "@" + domain.getName()),
        getDefaultCosFeatureFilter(feature)
    );
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
