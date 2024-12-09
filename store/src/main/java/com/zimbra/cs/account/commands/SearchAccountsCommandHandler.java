package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Alias;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class SearchAccountsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public SearchAccountsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doSearchAccounts(args);
  }


  private void doSearchAccounts(String[] args) throws ServiceException, ArgException {
    boolean verbose = false;
    int i = 1;

    if (args[i].equals("-v")) {
      verbose = true;
      i++;
      if (args.length < i - 1) {
        provUtil.usage();
        return;
      }
    }

    if (args.length < i + 1) {
      provUtil.usage();
      return;
    }

    String query = args[i];

    Map<String, Object> attrs = provUtil.getMap(args, i + 1);
    String limitStr = (String) attrs.get("limit");
    int limit = limitStr == null ? Integer.MAX_VALUE : Integer.parseInt(limitStr);

    String offsetStr = (String) attrs.get("offset");
    int offset = offsetStr == null ? 0 : Integer.parseInt(offsetStr);

    String sortBy = (String) attrs.get("sortBy");
    String sortAscending = (String) attrs.get("sortAscending");
    boolean isSortAscending = sortAscending == null || "1".equalsIgnoreCase(sortAscending);

    String[] attrsToGet = null;

    String typesStr = (String) attrs.get("types");
    if (typesStr == null) {
      typesStr =
              SearchDirectoryOptions.ObjectType.accounts.name()
                      + ","
                      + SearchDirectoryOptions.ObjectType.aliases.name()
                      + ","
                      + SearchDirectoryOptions.ObjectType.distributionlists.name()
                      + ","
                      + SearchDirectoryOptions.ObjectType.dynamicgroups.name()
                      + ","
                      + SearchDirectoryOptions.ObjectType.resources.name();
    }

    String domainStr = (String) attrs.get("domain");

    var prov = provUtil.getProvisioning();
    SearchDirectoryOptions searchOpts = new SearchDirectoryOptions(attrsToGet);
    if (domainStr != null) {
      Domain d = provUtil.lookupDomain(domainStr, prov);
      searchOpts.setDomain(d);
    }
    searchOpts.setTypes(typesStr);
    searchOpts.setSortOpt(isSortAscending ? SearchDirectoryOptions.SortOpt.SORT_ASCENDING : SearchDirectoryOptions.SortOpt.SORT_DESCENDING);
    searchOpts.setSortAttr(sortBy);

    // if LdapClient is not initialized(the case for SoapProvisioning), FilterId
    // is not initialized. Use null for SoapProvisioning, it will be set to
    // FilterId.ADMIN_SEARCH in SearchDirectory soap handler.
    ZLdapFilterFactory.FilterId filterId = (prov instanceof LdapProv) ? ZLdapFilterFactory.FilterId.ADMIN_SEARCH : null;
    searchOpts.setFilterString(filterId, query);
    searchOpts.setConvertIDNToAscii(true); // query must be already RFC 2254 escaped

    List<NamedEntry> accounts = prov.searchDirectory(searchOpts);

    for (int j = offset; j < offset + limit && j < accounts.size(); j++) {
      NamedEntry account = accounts.get(j);
      if (verbose) {
        if (account instanceof Account) {
          provUtil.dumpAccount((Account) account, true, null);
        } else if (account instanceof Alias) {
          provUtil.dumpAlias((Alias) account);
        } else if (account instanceof DistributionList) {
          provUtil.dumpGroup((DistributionList) account, null);
        } else if (account instanceof Domain) {
          provUtil.dumpDomain((Domain) account, null);
        }
      } else {
        provUtil.getConsole().println(account.getName());
      }
    }
  }
}
