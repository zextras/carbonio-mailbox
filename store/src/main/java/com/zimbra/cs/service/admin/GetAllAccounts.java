// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.QName;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.account.Key.ServerBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.Server;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class GetAllAccounts extends AdminDocumentHandler {

    public static final String BY_NAME = "name";
    public static final String BY_ID = "id";
    
    /**
     * must be careful and only allow access to domain if domain admin
     */
    public boolean domainAuthSufficient(Map context) {
        return true;
    }
    
	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
	    
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
	    Provisioning prov = Provisioning.getInstance();
	    
        Element response = null;

        Element s = request.getOptionalElement(AdminConstants.E_SERVER);
        Server server = null;
        if (s != null) {
            String key = s.getAttribute(AdminConstants.A_BY);
            String value = s.getText();
            if (key.equals(BY_NAME)) 
                server = prov.get(Key.ServerBy.name, value);
            else if (key.equals(BY_ID))
                server = prov.get(Key.ServerBy.id, value);
            else
                throw ServiceException.INVALID_REQUEST("unknown value for server by: "+key, null);
                
            if (server == null)
                throw AccountServiceException.NO_SUCH_SERVER(value);
        }
        
        Element d = request.getOptionalElement(AdminConstants.E_DOMAIN);
        
        if (d != null || isDomainAdminOnly(zsc)) {
            
            String key = d == null ? BY_NAME : d.getAttribute(AdminConstants.A_BY);
            String value = d == null ? getAuthTokenAccountDomain(zsc).getName() : d.getText();
	    
            Domain domain = null;
        
            if (key.equals(BY_NAME)) {
                domain = prov.get(Key.DomainBy.name, value);
            } else if (key.equals(BY_ID)) {
                domain = prov.get(Key.DomainBy.id, value);
            } else {
                throw ServiceException.INVALID_REQUEST("unknown value for domain by: "+key, null);
            }
	    
            if (domain == null)
                throw AccountServiceException.NO_SUCH_DOMAIN(value);
            
            checkDomainRight(zsc, domain, AdminRight.PR_ALWAYS_ALLOW); 

            response = zsc.createElement(getResponseQName());
            doDomain(zsc, response, domain, server);

        } else {
            response = zsc.createElement(getResponseQName());
            List domains = prov.getAllDomains();
            if (domains != null) {
	            for (Iterator dit=domains.iterator(); dit.hasNext(); ) {
	                Domain domain = (Domain) dit.next();
	                doDomain(zsc, response, domain, server);                
	            }
            } else { //domains not supported, for now only offline
            	doDomain(zsc, response, null, server);
            }
        }
        return response;        
	}

    protected QName getResponseQName() {
        return AdminConstants.GET_ALL_ACCOUNTS_RESPONSE;
    }

    protected static class AccountVisitor implements NamedEntry.Visitor {
        ZimbraSoapContext mZsc;
        AdminDocumentHandler mHandler;
        Element mParent;
        AdminAccessControl mAAC;
        
        AccountVisitor(ZimbraSoapContext zsc, AdminDocumentHandler handler, Element parent) throws ServiceException {
            mZsc = zsc;
            mHandler = handler;
            mParent = parent;
            mAAC = AdminAccessControl.getAdminAccessControl(zsc);
        }
        
        public void visit(com.zimbra.cs.account.NamedEntry entry) throws ServiceException {
            if (mAAC.hasRightsToList(entry, Admin.R_listAccount, null)) {
                ToXML.encodeAccount(mParent, (Account)entry, true, null, mAAC.getAttrRightChecker(entry)); 
            }
        }
    }
    
    protected void doDomain(ZimbraSoapContext zsc, final Element e, Domain d, Server s) throws ServiceException {
        AccountVisitor visitor = new AccountVisitor(zsc, this, e);
        Provisioning.getInstance().getAllAccounts(d, s, visitor);
    }
    
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_listAccount);
        relatedRights.add(Admin.R_getAccount);
        
        notes.add(AdminRightCheckPoint.Notes.LIST_ENTRY);
    }
}
