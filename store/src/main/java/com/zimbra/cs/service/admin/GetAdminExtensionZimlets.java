// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.account.accesscontrol.ACLAccessManager;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.zimlet.ZimletPresence.Presence;
import com.zimbra.cs.zimlet.ZimletUtil;
import com.zimbra.soap.ZimbraSoapContext;

public class GetAdminExtensionZimlets extends AdminDocumentHandler  {

    public boolean domainAuthSufficient(Map<String, Object> context) {
        return true;
    }

    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
		ZimbraSoapContext zsc = getZimbraSoapContext(context);
		
        Element response = zsc.createElement(AdminConstants.GET_ADMIN_EXTENSION_ZIMLETS_RESPONSE);
        Element zimlets = response.addUniqueElement(AccountConstants.E_ZIMLETS);
        doExtensionZimlets(zsc, context, zimlets);
        
        return response;
    }

	private void doExtensionZimlets(ZimbraSoapContext zsc, Map<String, Object> context, Element response) throws ServiceException {

        boolean mobileNGEnabled = true;
        boolean networkAdminEnabled = true;
        boolean networkNGEnabled  = true;
        
        try {
          networkNGEnabled = Provisioning.getInstance().getLocalServer().isNetworkModulesNGEnabled();
          mobileNGEnabled = Provisioning.getInstance().getLocalServer().isNetworkMobileNGEnabled();
          networkAdminEnabled = Provisioning.getInstance().getLocalServer().isNetworkAdminNGEnabled();
              
        } catch (ServiceException e) {
          ZimbraLog.mailbox.warn("Exception while getting zimbraNetworkModulesNG related attributes.", e);
        }
		Iterator<Zimlet> zimlets = Provisioning.getInstance().listAllZimlets().iterator();
		while (zimlets.hasNext()) {
		    
		    Zimlet z = zimlets.next();
		    
		    if (!hasRightsToList(zsc, z, Admin.R_listZimlet, Admin.R_getZimlet))
			    continue;
			
			if (z.isExtension()) {
			    boolean include = true;
                if ("com_zimbra_mobilesync".equals(z.getName()) && mobileNGEnabled && networkNGEnabled) {
                    include = !mobileNGEnabled;
                    if (!include) {
                        ZimbraLog.mailbox.info("Disabled '%s' as zimbraNetworkMobileNGEnabled is true.", z.getName());
                    }
                }
                
                if ("com_zimbra_hsm".equals(z.getName()) && networkNGEnabled) {
                    include = !networkNGEnabled;
                    if (!include) {
                        ZimbraLog.mailbox.info("Disabled '%s'  as zimbraNetworNGEnabled is true.", z.getName());
                    }
                }
                
                if ("com_zimbra_backuprestore".equals(z.getName()) && networkNGEnabled) {
                    include = !networkNGEnabled;
                    if (!include) {
                        ZimbraLog.mailbox.info("Disabled '%s' as zimbraNetworkNGEnabled is true.", z.getName());
                    }
                }
                if ("com_zimbra_delegatedadmin".equals(z.getName()) && networkAdminEnabled && networkNGEnabled) {
                    include = !networkAdminEnabled;
                    if (!include) {
                        ZimbraLog.mailbox.info("Disabled '%s' as zimbraNetworkAdminNGEnabled is true.", z.getName());
                    }
                    include = include && (AccessManager.getInstance() instanceof ACLAccessManager);
                }

                if (include) {
                    ZimletUtil.listZimlet(response, z, -1, Presence.enabled);
                    // admin zimlets are all enabled
                }
            }
        }
    }
	
    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_listZimlet);
        relatedRights.add(Admin.R_getZimlet);
        
        notes.add("Only zimlets on which the authed admin has effective " + 
                  Admin.R_listZimlet.getName() + " and " + Admin.R_getZimlet.getName() + 
                  " rights will appear in the response.");
        
        notes.add("e.g. there are zimlet1, zimlet2, zimlet3, if an admin has effective " + 
                  Admin.R_listZimlet.getName() + " and " + Admin.R_getZimlet.getName() +  
                  " rights on zimlet1, zimlet2, " + 
                  "then only zimlet1, zimlet2 will appear in the GetAdminExtensionZimletsResponse.  " + 
                  "The GetAdminExtensionZimletsRequest itself will not get PERM_DENIED.");
    }
	
}
