// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.SearchAccountsOptions;
import com.zimbra.cs.account.SearchAccountsOptions.IncludeType;
import com.zimbra.cs.account.SearchDirectoryOptions.MakeObjectOpt;
import com.zimbra.cs.account.SearchDirectoryOptions.SortOpt;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.session.AdminSession;
import com.zimbra.cs.session.Session;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GetQuotaUsage extends AdminDocumentHandler {

  public static final String BY_NAME = "name";
  public static final String BY_ID = "id";

  public static final String SORT_PERCENT_USED = "percentUsed";
  public static final String SORT_TOTAL_USED = "totalUsed";
  public static final String SORT_QUOTA_LIMIT = "quotaLimit";
  public static final String SORT_ACCOUNT = "account";
  private static final String QUOTA_USAGE_CACHE_KEY = "GetQuotaUsage";
  private static final String QUOTA_USAGE_ALL_SERVERS_CACHE_KEY = "GetQuotaUsageAllServers";

  /**
   * Get list of {@link AccountQuota} containing unique elements, removes duplicate {@link
   * AccountQuota} from passed AccountQuota list using {@link AccountQuota#id} as unique identifier
   *
   * @param accountQuotas List containing {@link AccountQuota} that we want to filter
   * @return return List of uniquely identified AccountQuotas
   * @author Keshav Bhatt
   * @since 22.12.0
   */
  public static List<AccountQuota> getUniqueAccountQuotaList(List<AccountQuota> accountQuotas) {
    final Set<String> accountQuotaMap = new HashSet<>();
    return accountQuotas.stream()
        .filter(a -> accountQuotaMap.add(a.getId()))
        .collect(Collectors.toList());
  }

  private static void shutdownAndAwaitTermination(ExecutorService executor)
      throws ServiceException {
    executor.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait for existing tasks to terminate
      // make wait timeout configurable?
      if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
        throw ServiceException.FAILURE(
            "Time out waiting for "
                + AdminConstants.E_GET_AGGR_QUOTA_USAGE_ON_SERVER_REQUEST
                + " result",
            null);
      }
    } catch (InterruptedException ie) {
      executor.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }

  static synchronized QuotaUsageParams getCachedQuotaUsage(
      AdminSession session, boolean allServers) {
    return (QuotaUsageParams)
        session.getData(allServers ? QUOTA_USAGE_ALL_SERVERS_CACHE_KEY : QUOTA_USAGE_CACHE_KEY);
  }

  static synchronized void setCachedQuotaUsage(
      AdminSession session, QuotaUsageParams params, boolean allServers) {
    session.setData(allServers ? QUOTA_USAGE_ALL_SERVERS_CACHE_KEY : QUOTA_USAGE_CACHE_KEY, params);
  }

  static synchronized void clearCachedQuotaUsage(AdminSession session) {
    session.clearData(QUOTA_USAGE_CACHE_KEY);
    session.clearData(QUOTA_USAGE_ALL_SERVERS_CACHE_KEY);
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    int limit = (int) request.getAttributeLong(AdminConstants.A_LIMIT, Integer.MAX_VALUE);
    if (limit == 0) {
      limit = Integer.MAX_VALUE;
    }
    int offset = (int) request.getAttributeLong(AdminConstants.A_OFFSET, 0);
    String domain = request.getAttribute(AdminConstants.A_DOMAIN, null);
    String sortBy = request.getAttribute(AdminConstants.A_SORT_BY, SORT_TOTAL_USED);
    boolean sortAscending = request.getAttributeBool(AdminConstants.A_SORT_ASCENDING, false);
    boolean refresh = request.getAttributeBool(AdminConstants.A_REFRESH, false);

    if (!(sortBy.equals(SORT_TOTAL_USED)
        || sortBy.equals(SORT_PERCENT_USED)
        || sortBy.equals(SORT_QUOTA_LIMIT)
        || sortBy.equals(SORT_ACCOUNT))) {
      throw ServiceException.INVALID_REQUEST("sortBy must be percentUsed or totalUsed", null);
    }

    //
    // if we are a domain admin only, restrict to domain
    // hmm, this SOAP is not domainAuthSufficient, bug?
    //
    // Note: isDomainAdminOnly *always* returns false for pure ACL based AccessManager
    if (isDomainAdminOnly(zsc)) {
      // need a domain, if domain is not specified, use the authed admins own domain.
      if (domain == null) {
        domain = getAuthTokenAccountDomain(zsc).getName();
      }

      // sanity check
      if (domain == null) {
        throw ServiceException.INVALID_REQUEST("no domain", null);
      }
    }

    Domain d = null;
    if (domain != null) {
      d = prov.get(Key.DomainBy.name, domain);
      if (d == null) {
        throw AccountServiceException.NO_SUCH_DOMAIN(domain);
      }
    }

    // if we have a domain, check the domain right getDomainQuotaUsage
    // if we don't have a domain, only allow system admin
    if (d != null) {
      checkDomainRight(zsc, d, AdminRights.R_getDomainQuotaUsage);
    } else {
      checkRight(zsc, null, AdminRight.PR_SYSTEM_ADMIN_ONLY);
    }

    boolean allServers = d != null && request.getAttributeBool(AdminConstants.A_ALL_SERVERS, false);

    List<AccountQuota> quotas = null;
    QuotaUsageParams params = new QuotaUsageParams(d, sortBy, sortAscending);
    AdminSession session = (AdminSession) getSession(zsc, Session.Type.ADMIN);
    if (session != null) {
      QuotaUsageParams cachedParams = getCachedQuotaUsage(session, allServers);
      if (cachedParams != null && cachedParams.equals(params) && !refresh) {
        quotas = cachedParams.getResult();
      }
    }
    if (quotas == null) {
      if (allServers) {
        final List<AccountQuota> accountQuotasFromAllServers =
            delegateRequestToAllServers(request.clone(), zsc.getRawAuthToken(), prov);

        final List<AccountQuota> uniqueAccountQuotas =
            getUniqueAccountQuotaList(accountQuotasFromAllServers);

        quotas = getSortedAccountsQuotaList(uniqueAccountQuotas, sortBy, sortAscending);
        // explicitly set the result
        params.setResult(quotas);
      } else {
        quotas = params.doSearch();
      }
      if (session != null) {
        setCachedQuotaUsage(session, params, allServers);
      }
    }

    Element response = zsc.createElement(AdminConstants.GET_QUOTA_USAGE_RESPONSE);
    int i;
    int limitMax = offset + limit;
    for (i = offset; i < limitMax && i < quotas.size(); i++) {
      AccountQuota quota = quotas.get(i);
      final String quotaId = quota.getId();
      Element account = response.addElement(AdminConstants.E_ACCOUNT);
      account.addAttribute(AdminConstants.A_NAME, quota.getName());
      account.addAttribute(AdminConstants.A_ID, quotaId);
      account.addAttribute(AdminConstants.A_QUOTA_USED, quota.getQuotaUsed());
      account.addAttribute(AdminConstants.A_QUOTA_LIMIT, quota.getQuotaLimit());
    }
    response.addAttribute(AdminConstants.A_MORE, i < quotas.size());
    response.addAttribute(AdminConstants.A_SEARCH_TOTAL, quotas.size());
    return response;
  }

  private List<AccountQuota> delegateRequestToAllServers(
      final Element request, final ZAuthToken authToken, Provisioning prov)
      throws ServiceException {
    // first set "allServers" to false
    request.addAttribute(AdminConstants.A_ALL_SERVERS, false);
    // don't set any "limit" in the delegated requests
    request.addAttribute(AdminConstants.A_LIMIT, 0);
    request.addAttribute(AdminConstants.A_OFFSET, 0);
    List<Server> servers = prov.getAllMailClientServers();
    // make number of threads in pool configurable?
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future<List<AccountQuota>>> futures = new LinkedList<>();
    for (final Server server : servers) {
      futures.add(
          executor.submit(
              () -> {
                ZimbraLog.misc.debug(
                    "Invoking %s on server %s",
                    AdminConstants.E_GET_QUOTA_USAGE_REQUEST, server.getName());
                String adminUrl = URLUtil.getAdminURL(server, AdminConstants.ADMIN_SERVICE_URI);
                SoapHttpTransport mTransport = new SoapHttpTransport(adminUrl);
                mTransport.setAuthToken(authToken);
                Element resp;
                try {
                  resp = mTransport.invoke(request.clone());
                } catch (Exception e) {
                  throw ServiceException.FAILURE(
                      "Error in invoking "
                          + AdminConstants.E_GET_QUOTA_USAGE_REQUEST
                          + " on server "
                          + server.getName(),
                      e);
                }
                List<Element> accountElts =
                    resp.getPathElementList(new String[] {AdminConstants.E_ACCOUNT});
                List<AccountQuota> retList = new ArrayList<>();
                for (Element accountElt : accountElts) {
                  AccountQuota quota = new AccountQuota();
                  quota.setName(accountElt.getAttribute(AdminConstants.A_NAME));
                  quota.setId(accountElt.getAttribute(AdminConstants.A_ID));
                  quota.setQuotaUsed(accountElt.getAttributeLong(AdminConstants.A_QUOTA_USED));
                  quota.setQuotaLimit(accountElt.getAttributeLong(AdminConstants.A_QUOTA_LIMIT));
                  quota.setPercentQuotaUsed(
                      quota.getQuotaLimit() > 0
                          ? (quota.getQuotaUsed() / (float) quota.getQuotaLimit())
                          : 0);
                  retList.add(quota);
                }
                return retList;
              }));
    }
    shutdownAndAwaitTermination(executor);

    // Aggregate all results
    List<AccountQuota> retList = new ArrayList<>();
    for (Future<List<AccountQuota>> future : futures) {
      List<AccountQuota> result;
      try {
        result = future.get();
      } catch (Exception e) {
        throw ServiceException.FAILURE("Error in getting task execution result", e);
      }
      retList.addAll(result);
    }
    return retList;
  }

  /**
   * Sort {@link AccountQuota} with provided parameters
   *
   * @param accountQuotas {@link List} list containing unsorted {@link AccountQuota}
   * @param sortBy {@link String} sort by provided string parameter
   * @param sortAscending {@link Boolean} sort in ascending order
   * @return List of sorted {@link AccountQuota}
   */
  public static List<AccountQuota> getSortedAccountsQuotaList(
      final List<AccountQuota> accountQuotas, String sortBy, boolean sortAscending) {
    // sort
    boolean sortByTotal = sortBy.equals(SORT_TOTAL_USED);
    boolean sortByQuota = sortBy.equals(SORT_QUOTA_LIMIT);
    boolean sortByAccount = sortBy.equals(SORT_ACCOUNT);
    Comparator<AccountQuota> comparator =
        new QuotaComparator(sortByTotal, sortByQuota, sortByAccount, sortAscending);
    accountQuotas.sort(comparator);
    return accountQuotas;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(AdminRights.R_getDomainQuotaUsage);

    notes.add(
        "If a domain is specified, need the the domain right "
            + AdminRights.R_getDomainQuotaUsage.getName()
            + ".  If domain is not specified, only system admins are allowed.");
  }

  public static class AccountQuota {

    private String name;
    private String id;
    private long quotaLimit;
    private long sortQuotaLimit;
    private long quotaUsed;
    private float percentQuotaUsed;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public long getQuotaLimit() {
      return quotaLimit;
    }

    public void setQuotaLimit(long quotaLimit) {
      this.quotaLimit = quotaLimit;
    }

    public long getSortQuotaLimit() {
      return sortQuotaLimit;
    }

    public void setSortQuotaLimit(long sortQuotaLimit) {
      this.sortQuotaLimit = sortQuotaLimit;
    }

    public long getQuotaUsed() {
      return quotaUsed;
    }

    public void setQuotaUsed(long quotaUsed) {
      this.quotaUsed = quotaUsed;
    }

    public float getPercentQuotaUsed() {
      return percentQuotaUsed;
    }

    public void setPercentQuotaUsed(float percentQuotaUsed) {
      this.percentQuotaUsed = percentQuotaUsed;
    }
  }

  public static class QuotaUsageParams {

    String domainId;
    String sortBy;
    boolean sortAscending;

    List<AccountQuota> mResult;

    QuotaUsageParams(Domain d, String sortBy, boolean sortAscending) {
      domainId = (d == null) ? "" : d.getId();
      this.sortBy = (sortBy == null) ? "" : sortBy;
      this.sortAscending = sortAscending;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof QuotaUsageParams)) {
        return false;
      }
      if (o == this) {
        return true;
      }

      QuotaUsageParams other = (QuotaUsageParams) o;
      return domainId.equals(other.domainId)
          && sortBy.equals(other.sortBy)
          && sortAscending == other.sortAscending;
    }

    List<AccountQuota> doSearch() throws ServiceException {
      if (mResult != null) {
        return mResult;
      }

      ArrayList<AccountQuota> result = new ArrayList<>();

      Provisioning prov = Provisioning.getInstance();

      SearchAccountsOptions searchOpts = new SearchAccountsOptions();
      searchOpts.setIncludeType(IncludeType.ACCOUNTS_ONLY);
      searchOpts.setMakeObjectOpt(MakeObjectOpt.NO_SECONDARY_DEFAULTS);
      searchOpts.setSortOpt(SortOpt.SORT_ASCENDING);

      Domain d = domainId.equals("") ? null : prov.get(Key.DomainBy.id, domainId);
      if (d != null) {
        searchOpts.setDomain(d);
      }
      List<NamedEntry> accounts =
          prov.searchAccountsOnServer(Provisioning.getInstance().getLocalServer(), searchOpts);

      Map<String, Long> quotaUsed = MailboxManager.getInstance().getMailboxSizes(accounts);

      for (NamedEntry obj : accounts) {
        if (!(obj instanceof Account)) {
          continue;
        }
        Account acct = (Account) obj;
        AccountQuota aq = new AccountQuota();
        aq.setId(acct.getId());
        aq.setName(acct.getName());
        aq.setQuotaLimit(AccountUtil.getEffectiveQuota(acct));
        aq.setSortQuotaLimit(aq.getQuotaLimit() == 0 ? Long.MAX_VALUE : aq.getQuotaLimit());
        Long used = quotaUsed.get(acct.getId());
        aq.setQuotaUsed(used == null ? 0 : used);
        aq.setPercentQuotaUsed(
            aq.getQuotaLimit() > 0 ? (aq.getQuotaUsed() / (float) aq.getQuotaLimit()) : 0);
        result.add(aq);
      }

      boolean sortByTotal = sortBy.equals(SORT_TOTAL_USED);
      boolean sortByQuota = sortBy.equals(SORT_QUOTA_LIMIT);
      boolean sortByAccount = sortBy.equals(SORT_ACCOUNT);
      Comparator<AccountQuota> comparator =
          new QuotaComparator(sortByTotal, sortByQuota, sortByAccount, sortAscending);
      result.sort(comparator);
      mResult = result;
      return mResult;
    }

    List<AccountQuota> getResult() {
      return mResult;
    }

    void setResult(List<AccountQuota> result) {
      mResult = result;
    }
  }

  public static class QuotaComparator implements Comparator<AccountQuota> {

    private final boolean sortByTotal;
    private final boolean sortByQuota;
    private final boolean sortByAccount;
    private final boolean sortAscending;

    public QuotaComparator(
        boolean sortByTotal, boolean sortByQuota, boolean sortByAccount, boolean sortAscending) {
      this.sortByTotal = sortByTotal;
      this.sortByQuota = sortByQuota;
      this.sortByAccount = sortByAccount;
      this.sortAscending = sortAscending;
    }

    @Override
    public int compare(AccountQuota a, AccountQuota b) {
      int comp = 0;
      if (sortByTotal) {
        if (a.getQuotaUsed() > b.getQuotaUsed()) {
          comp = 1;
        } else if (a.getQuotaUsed() < b.getQuotaUsed()) {
          comp = -1;
        }
      } else if (sortByQuota) {
        if (a.getSortQuotaLimit() > b.getSortQuotaLimit()) {
          comp = 1;
        } else if (a.getSortQuotaLimit() < b.getSortQuotaLimit()) {
          comp = -1;
        }
      } else if (sortByAccount) {
        comp = a.getName().compareToIgnoreCase(b.getName());
      } else {
        if (a.getPercentQuotaUsed() > b.getPercentQuotaUsed()) {
          comp = 1;
        } else if (a.getPercentQuotaUsed() < b.getPercentQuotaUsed()) {
          comp = -1;
        }
      }
      return sortAscending ? comp : -comp;
    }
  }
}
