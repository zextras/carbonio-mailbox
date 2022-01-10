// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.account.Key.CalendarResourceBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetCalendarResourceRequest;

/**
 * @author jhahm
 */
public class GetCalendarResource extends AdminDocumentHandler {

    private static final String[] TARGET_RESOURCE_PATH = new String[] { AdminConstants.E_CALENDAR_RESOURCE };
    @Override
    protected String[] getProxiedResourceElementPath()  { return TARGET_RESOURCE_PATH; }

    /**
     * must be careful and only return calendar resources
     * a domain admin can see
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
    public Element handle(Element request, Map<String, Object> context)
    throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        GetCalendarResourceRequest req = zsc.elementToJaxb(request);
        boolean applyCos = !Boolean.FALSE.equals(req.getApplyCos());
        CalendarResourceBy calresBy = req.getCalResource().getBy().toKeyCalendarResourceBy();
        String value = req.getCalResource().getKey();

        CalendarResource resource = prov.get(calresBy, value);
        defendAgainstCalResourceHarvesting(resource, calresBy, value, zsc, Admin.R_getCalendarResourceInfo);

        AdminAccessControl aac = checkCalendarResourceRight(zsc, resource, AdminRight.PR_ALWAYS_ALLOW);

        Element response = zsc.createElement(AdminConstants.GET_CALENDAR_RESOURCE_RESPONSE);
        Set<String> reqAttrs = getReqAttrs(req.getAttrs(), AttributeClass.calendarResource);
        ToXML.encodeCalendarResource(response, resource, applyCos, reqAttrs, aac.getAttrRightChecker(resource));

        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_getCalendarResource);
        notes.add(String.format(AdminRightCheckPoint.Notes.GET_ENTRY, Admin.R_getCalendarResource.getName()));
    }
}
