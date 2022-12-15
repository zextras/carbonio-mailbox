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
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.DeleteCalendarResourceRequest;

/**
 * @author jhahm
 */
public class DeleteCalendarResource extends AdminDocumentHandler {

    private static final String[] TARGET_RESOURCE_PATH = new String[] { AdminConstants.E_ID };
    @Override
    protected String[] getProxiedResourcePath()  { return TARGET_RESOURCE_PATH; }

    /**
     * must be careful and only allow deletes domain admin has access to
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

    /**
     * Deletes a calendar resource account and its mailbox.
     */
    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();
        DeleteCalendarResourceRequest req = zsc.elementToJaxb(request);
        String id = req.getId();
        if (null == id) {
            throw ServiceException.INVALID_REQUEST("missing required attribute: " + AdminConstants.E_ID, null);
        }

        // Confirm that the account exists and that the mailbox is located on the current host
        CalendarResource resource = prov.get(CalendarResourceBy.id, id);
        defendAgainstCalResourceHarvesting(resource, CalendarResourceBy.id, id, zsc, Admin.R_deleteCalendarResource);

        if (!Provisioning.onLocalServer(resource)) {
            // Request must be sent to the host that the mailbox is on, so that
            // the mailbox can be deleted
            throw ServiceException.WRONG_HOST(resource.getAttr(Provisioning.A_zimbraMailHost), null);
        }
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(resource);

        prov.deleteCalendarResource(id);
        mbox.deleteMailbox();

        ZimbraLog.security.info(ZimbraLog.encodeAttrs(new String[] {"cmd", "DeleteCalendarResource", "name",
                resource.getName(), "id", resource.getId()}));

        Element response = zsc.createElement(AdminConstants.DELETE_CALENDAR_RESOURCE_RESPONSE);
        return response;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_deleteCalendarResource);
    }
}
