/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2006, 2007 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Alias;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.DomainBy;
import com.zimbra.cs.account.Provisioning.SearchOptions;
import com.zimbra.cs.service.account.ToXML;
import com.zimbra.cs.session.AdminSession;
import com.zimbra.cs.session.SessionCache;
import com.zimbra.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author schemers
 */
public class SearchDirectory extends AdminDocumentHandler {

    public static final String BY_NAME = "name";
    public static final String BY_ID = "id";

    public static final int MAX_SEARCH_RESULTS = 5000;
    
    /**
     * must be careful and only allow access to domain if domain admin
     */
    public boolean domainAuthSufficient(Map context) {
        return true;
    }
    
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {

        ZimbraSoapContext lc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        String query = request.getAttribute(AdminService.E_QUERY);

        int maxResults = (int) request.getAttributeLong(AdminService.A_MAX_RESULTS, MAX_SEARCH_RESULTS);
        int limit = (int) request.getAttributeLong(AdminService.A_LIMIT, Integer.MAX_VALUE);
        if (limit == 0)
            limit = Integer.MAX_VALUE;
        int offset = (int) request.getAttributeLong(AdminService.A_OFFSET, 0);        
        String domain = request.getAttribute(AdminService.A_DOMAIN, null);
        boolean applyCos = request.getAttributeBool(AdminService.A_APPLY_COS, true);
        String attrsStr = request.getAttribute(AdminService.A_ATTRS, null);
        String sortBy = request.getAttribute(AdminService.A_SORT_BY, null);        
        String types = request.getAttribute(AdminService.A_TYPES, "accounts");
        boolean sortAscending = request.getAttributeBool(AdminService.A_SORT_ASCENDING, true);        

        int flags = 0;
        
        if (types.indexOf("accounts") != -1) flags |= Provisioning.SA_ACCOUNT_FLAG;
        if (types.indexOf("aliases") != -1) flags |= Provisioning.SA_ALIAS_FLAG;
        if (types.indexOf("distributionlists") != -1) flags |= Provisioning.SA_DISTRIBUTION_LIST_FLAG;
        if (types.indexOf("resources") != -1) flags |= Provisioning.SA_CALENDAR_RESOURCE_FLAG;
        if (types.indexOf("domains") != -1) flags |= Provisioning.SA_DOMAIN_FLAG;

        String[] attrs = attrsStr == null ? null : attrsStr.split(",");

        // if we are a domain admin only, restrict to domain
        if (isDomainAdminOnly(lc)) {
            if ((flags & Provisioning.SA_DOMAIN_FLAG) == Provisioning.SA_DOMAIN_FLAG)
                throw ServiceException.PERM_DENIED("can not search for domains");

            if (domain == null) {
                domain = getAuthTokenAccountDomain(lc).getName();
            } else {
                if (!canAccessDomain(lc, domain)) 
                    throw ServiceException.PERM_DENIED("can not access domain"); 
            }
        }

        Domain d = null;
        if (domain != null) {
            d = prov.get(DomainBy.name, domain);
            if (d == null)
                throw AccountServiceException.NO_SUCH_DOMAIN(domain);
        }

        List accounts;
        AdminSession session = (AdminSession) lc.getSession(SessionCache.SESSION_ADMIN);
        if (session != null) {
            accounts = session.searchAccounts(d, query, attrs, sortBy, sortAscending, flags, offset, maxResults);
        } else {
            SearchOptions options = new SearchOptions();
            options.setDomain(d);
            options.setFlags(flags);
            options.setMaxResults(maxResults);
            options.setQuery(query);
            options.setReturnAttrs(attrs);
            options.setSortAscending(sortAscending);
            options.setSortAttr(sortBy);
            accounts = prov.searchDirectory(options);
        }

        Element response = lc.createElement(AdminService.SEARCH_DIRECTORY_RESPONSE);
        int i, limitMax = offset+limit;
        for (i=offset; i < limitMax && i < accounts.size(); i++) {
            NamedEntry entry = (NamedEntry) accounts.get(i);
        	if (entry instanceof CalendarResource) {
        	    ToXML.encodeCalendarResource(response, (CalendarResource) entry, applyCos);
        	} else if (entry instanceof Account) {
                ToXML.encodeAccount(response, (Account) entry, applyCos);
            } else if (entry instanceof DistributionList) {
                doDistributionList(response, (DistributionList) entry);
            } else if (entry instanceof Alias) {
                doAlias(response, (Alias) entry);
            } else if (entry instanceof Domain) {
                GetDomain.doDomain(response, (Domain) entry, applyCos);
            }
        }          

        response.addAttribute(AdminService.A_MORE, i < accounts.size());
        response.addAttribute(AdminService.A_SEARCH_TOTAL, accounts.size());        
        return response;
    }

    static void doDistributionList(Element e, DistributionList list) throws ServiceException {
        Element elist = e.addElement(AdminService.E_DL);
        elist.addAttribute(AdminService.A_NAME, list.getName());
        elist.addAttribute(AdminService.A_ID, list.getId());
        Map attrs = list.getAttrs();
        doAttrs(elist, attrs);
    }

    static void doAlias(Element e, Alias a) throws ServiceException {
        Element ealias = e.addElement(AdminService.E_ALIAS);
        ealias.addAttribute(AdminService.A_NAME, a.getName());
        ealias.addAttribute(AdminService.A_ID, a.getId());        
        Map attrs = a.getAttrs();
        doAttrs(ealias, attrs);
    }

    static void doAttrs(Element e, Map attrs) {
        for (Iterator mit = attrs.entrySet().iterator(); mit.hasNext(); ) {
            Map.Entry entry = (Entry) mit.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String[]) {
                String sv[] = (String[]) value;
                for (int i = 0; i < sv.length; i++)
                    e.addElement(AdminService.E_A).addAttribute(AdminService.A_N, name).setText(sv[i]);
            } else if (value instanceof String)
                e.addElement(AdminService.E_A).addAttribute(AdminService.A_N, name).setText((String) value);
        }       
    }   
}
