package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.InvalidCommandException;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.EntrySearchFilter;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.account.ldap.LdapEntrySearchFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;

import java.util.List;

class SearchCalendarResourcesCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public SearchCalendarResourcesCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException, InvalidCommandException {
    doSearchCalendarResources(args);
  }

  private void doSearchCalendarResources(String[] args) throws ServiceException, InvalidCommandException {

    boolean verbose = false;
    int i = 1;

    if (args.length < i + 1) {
      provUtil.usage();
      return;
    }
    if (args[i].equals("-v")) {
      verbose = true;
      i++;
    }
    if (args.length < i + 1) {
      provUtil.usage();
      return;
    }
    Domain d = provUtil.lookupDomain(args[i++]);

    if ((args.length - i) % 3 != 0) {
      provUtil.usage();
      return;
    }

    EntrySearchFilter.Multi multi = new EntrySearchFilter.Multi(false, EntrySearchFilter.AndOr.and);
    for (; i < args.length; ) {
      String attr = args[i++];
      String op = args[i++];
      String value = args[i++];
      try {
        EntrySearchFilter.Single single = new EntrySearchFilter.Single(false, attr, op, value);
        multi.add(single);
      } catch (IllegalArgumentException e) {
        provUtil.getConsole().printError("Bad search op in: " + attr + " " + op + " '" + value + "'");
        e.printStackTrace();
        provUtil.usage();
        return;
      }
    }
    EntrySearchFilter filter = new EntrySearchFilter(multi);
    String filterStr = LdapEntrySearchFilter.toLdapCalendarResourcesFilter(filter);

    SearchDirectoryOptions searchOpts = new SearchDirectoryOptions();
    searchOpts.setDomain(d);
    searchOpts.setTypes(SearchDirectoryOptions.ObjectType.resources);
    searchOpts.setSortOpt(SearchDirectoryOptions.SortOpt.SORT_ASCENDING);
    searchOpts.setFilterString(ZLdapFilterFactory.FilterId.ADMIN_SEARCH, filterStr);

    List<NamedEntry> resources = provUtil.getProvisioning().searchDirectory(searchOpts);

    // List<NamedEntry> resources = prov.searchCalendarResources(d, filter, null, null, true);
    for (NamedEntry entry : resources) {
      CalendarResource resource = (CalendarResource) entry;
      if (verbose) {
        dumper.dumpCalendarResource(resource, true, null);
      } else {
        provUtil.getConsole().println(resource.getName());
      }
    }
  }
}
