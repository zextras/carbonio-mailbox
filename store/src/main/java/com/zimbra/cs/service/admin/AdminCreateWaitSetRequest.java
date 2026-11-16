// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.mail.WaitSetRequest;
import com.zimbra.cs.session.WaitSetAccount;
import com.zimbra.cs.session.WaitSetError;
import com.zimbra.cs.session.WaitSetMgr;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AdminCreateWaitSetResponse;

public class AdminCreateWaitSetRequest extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        com.zimbra.soap.admin.message.AdminCreateWaitSetRequest req = zsc.elementToJaxb(request);
        AdminCreateWaitSetResponse resp = new AdminCreateWaitSetResponse();

        Set<MailItem.Type> defaultInterests = WaitSetRequest.parseInterestStr(req.getDefaultInterests(),
                EnumSet.noneOf(MailItem.Type.class));
        boolean adminAllowed = zsc.getAuthToken().isAdmin();

        boolean allAccts = req.getAllAccounts();
        if (allAccts) {
            WaitSetMgr.checkRightForAllAccounts(zsc);
        }

        List<WaitSetAccount> add = WaitSetRequest.parseAddUpdateAccounts(zsc, req.getAccounts(), defaultInterests);

        List<Mailbox> referencedMailboxes = new ArrayList<>();
        for (WaitSetAccount acct : add) {
            try {
                Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(acct.getAccountId(),
                        MailboxManager.FetchMode.AUTOCREATE);
                referencedMailboxes.add(mbox);
            } catch (ServiceException e) {
                ZimbraLog.session.debug("Caught exception preloading mailbox for waitset", e);
            }
        }

        Pair<String, List<WaitSetError>> result = WaitSetMgr.create(zsc.getRequestedAccountId(), adminAllowed,
                defaultInterests, allAccts, add);

        resp.setWaitSetId(result.getFirst());
        resp.setDefaultInterests(WaitSetRequest.interestToStr(defaultInterests));
        resp.setSequence(0);
        resp.setErrors(WaitSetRequest.encodeErrors(result.getSecond()));
        return zsc.jaxbToElement(resp);
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add("If allAccounts is specified, " + AdminRightCheckPoint.Notes.SYSTEM_ADMINS_ONLY);
        notes.add("Otherwise, for each requested account, " + AdminRightCheckPoint.Notes.ADMIN_LOGIN_AS);
    }

}
