package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.SearchAccountsOptions;
import com.zimbra.cs.account.SearchDirectoryOptions;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.ldap.ZLdapFilterFactory;

import java.util.Set;

class GetAllAccountsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetAllAccountsCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }


  @Override public void handle(String[] args) throws ServiceException, UsageException {
    doGetAllAccounts(args);
  }

  private void doGetAllAccounts(String[] args) throws ServiceException, UsageException {

    LdapProv ldapProv = (LdapProv) provUtil.getProvisioning();
    var console = provUtil.getConsole();

    boolean verbose = false;
    boolean applyDefault = true;
    String d = null;
    String s = null;

    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else if (arg.equals("-e")) {
        applyDefault = false;
      } else if (arg.equals("-s")) {
        i++;
        if (i < args.length) {
          if (s == null) {
            s = args[i];
          } else {
            console.println("invalid arg: " + args[i] + ", already specified -s with " + s);
            provUtil.usage();
            return;
          }
        } else {
          provUtil.usage();
          return;
        }
      } else {
        if (d == null) {
          d = arg;
        } else {
          console.println("invalid arg: " + arg + ", already specified domain: " + d);
          provUtil.usage();
          return;
        }
      }
      i++;
    }

    if (!applyDefault && !verbose) {
      console.println(ProvUtil.ERR_INVALID_ARG_EV);
      provUtil.usage();
      return;
    }

    Server server = null;
    if (s != null) {
      server = provUtil.lookupServer(s);
    }
    if (d == null) {
      doGetAllAccounts(ldapProv, null, server, verbose, applyDefault, null);
    } else {
      Domain domain = provUtil.lookupDomain(d, ldapProv);
      doGetAllAccounts(ldapProv, domain, server, verbose, applyDefault, null);
    }
  }

  /** prov is always LdapProv here */
  private void doGetAllAccounts(
          LdapProv ldapProv,
          Domain domain,
          Server server,
          final boolean verbose,
          final boolean applyDefault,
          final Set<String> attrNames)
          throws ServiceException {
    NamedEntry.Visitor visitor =
            entry -> {
              if (verbose) {
                dumper.dumpAccount((Account) entry, applyDefault, attrNames);
              } else {
                provUtil.getConsole().println(entry.getName());
              }
            };

    SearchAccountsOptions options = new SearchAccountsOptions();
    if (domain != null) {
      options.setDomain(domain);
    }
    options.setIncludeType(SearchAccountsOptions.IncludeType.ACCOUNTS_ONLY);
    if (!applyDefault) {
      options.setMakeObjectOpt(SearchDirectoryOptions.MakeObjectOpt.NO_DEFAULTS);
    }

    if (server == null) {
      options.setFilter(ZLdapFilterFactory.getInstance().allAccountsOnly());
      ldapProv.searchDirectory(options, visitor);
    } else {
      ldapProv.searchAccountsOnServer(server, options, visitor);
    }
  }


}
