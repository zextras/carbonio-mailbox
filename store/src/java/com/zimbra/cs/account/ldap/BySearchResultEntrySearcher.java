// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.SearchDirectoryOptions.ObjectType;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.ZLdapContext;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZSearchControls;
import com.zimbra.cs.ldap.ZSearchResultEntry;
import com.zimbra.cs.ldap.ZSearchResultEnumeration;
import com.zimbra.cs.ldap.ZSearchScope;

public class BySearchResultEntrySearcher {
    public interface SearchEntryProcessor {
        public void processSearchEntry(ZSearchResultEntry sr);
    }

    private final LdapProvisioning prov;
    private final Domain domain;
    private final SearchEntryProcessor visitor;
    private final String[] returnAttrs;
    private final ZLdapContext zlc;
    public BySearchResultEntrySearcher(LdapProvisioning prov, ZLdapContext zlc, Domain domain,
            String [] retAttrs, SearchEntryProcessor visitor) {
        this.prov = prov;
        this.zlc = zlc;
        this.domain = domain;
        this.returnAttrs = retAttrs;
        this.visitor = visitor;
    }

    public void doSearch(ZLdapFilter filter, Set<ObjectType> types) throws ServiceException {
        String[] bases = prov.getSearchBases(domain, types);
        for (String base : bases) {
            try {
                ZSearchControls ctrl = ZSearchControls.createSearchControls(ZSearchScope.SEARCH_SCOPE_SUBTREE,
                        ZSearchControls.SIZE_UNLIMITED, returnAttrs);
                ZSearchResultEnumeration results =
                        prov.getHelper().searchDir(base, filter, ctrl, zlc, LdapServerType.REPLICA);
                while(results.hasMore()) {
                    ZSearchResultEntry sr = results.next();
                    visitor.processSearchEntry(sr);
                }
                results.close();
            } catch (ServiceException e) {
                ZimbraLog.search.debug("Unexpected exception whilst searching", e);
            }
        }
    }
}
