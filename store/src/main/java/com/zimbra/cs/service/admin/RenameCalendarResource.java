// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.CalendarResourceBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.RenameCalendarResourceRequest;

/**
 * @author jhahm
 */
public class RenameCalendarResource extends AdminDocumentHandler {

    private static final String[] TARGET_RESOURCE_PATH = new String[] { AdminConstants.E_ID };
    @Override
    protected String[] getProxiedResourcePath()  { return TARGET_RESOURCE_PATH; }

    /**
     * must be careful and only allow renames from/to
     * domains a domain admin can see
     */
    @Override
    public boolean domainAuthSufficient(Map context) {
        return true;
    }

    /**
     * @return true - which means accept responsibility for measures to prevent account harvesting by delegate admins
     */
    @Override
    public boolean defendsAgainstDelegateAdminAccountHarvesting() {
        return true;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        RenameCalendarResourceRequest req = zsc.elementToJaxb(request);

        String id = req.getId();
        String newName = req.getNewName();

        CalendarResource resource = prov.get(CalendarResourceBy.id, id);
        defendAgainstCalResourceHarvesting(resource, CalendarResourceBy.id, id, zsc, Admin.R_renameCalendarResource);
        String oldName = resource.getName();

        // check if the admin can rename the calendar resource
        checkAccountRight(zsc, resource, Admin.R_renameCalendarResource);

        // check if the admin can "create calendar resource" in the domain (can be same or diff)
        checkDomainRightByEmail(zsc, newName, Admin.R_createCalendarResource);

        prov.renameCalendarResource(id, newName);

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                new String[] {"cmd", "RenameCalendarResource", "name", oldName, "newName", newName}));

        // get again with new name...

        resource = prov.get(CalendarResourceBy.id, id);
        if (resource == null) {
            throw ServiceException.FAILURE("unable to get calendar resource after rename: " + id, null);
        }
        Element response = zsc.createElement(AdminConstants.RENAME_CALENDAR_RESOURCE_RESPONSE);
        ToXML.encodeCalendarResource(response, resource, true);
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_renameCalendarResource);
        relatedRights.add(Admin.R_createCalendarResource);
    }
}
