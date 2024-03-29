// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.session.WaitSetAccount;
import com.zimbra.cs.session.WaitSetError;
import com.zimbra.cs.session.WaitSetMgr;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.base.CreateWaitSetReq;
import com.zimbra.soap.base.CreateWaitSetResp;
import com.zimbra.soap.mail.message.CreateWaitSetRequest;
import com.zimbra.soap.mail.message.CreateWaitSetResponse;

/**
 *
 */
public class CreateWaitSet extends MailDocumentHandler {
    /*
     <!--*************************************
          CreateWaitSet: must be called once to initialize the WaitSet
          and to set its "default interest types"
         ************************************* -->
        <CreateWaitSetRequest defTypes="DEFAULT_INTEREST_TYPES" [all="1"]>
          [ <add>
            [<a id="ACCTID" [token="lastKnownSyncToken"] [types="if_not_default"]/>]+
            </add> ]
        </CreateWaitSetRequest>

        <CreateWaitSetResponse waitSet="setId" defTypes="types" seq="0">
          [ <error ...something.../>]*
        </CreateWaitSetResponse>
     */

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        CreateWaitSetRequest req = zsc.elementToJaxb(request);
        CreateWaitSetResponse resp = new CreateWaitSetResponse();
        staticHandle(this, req, context, resp);
        return zsc.jaxbToElement(resp);  /* MUST use zsc variant NOT JaxbUtil */
    }

    public static void staticHandle(DocumentHandler handler, CreateWaitSetReq request, Map<String, Object> context,
            CreateWaitSetResp response) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        String defInterestStr = request.getDefaultInterests();
        Set<MailItem.Type> defaultInterests = WaitSetRequest.parseInterestStr(defInterestStr,
                EnumSet.noneOf(MailItem.Type.class));
        boolean adminAllowed = zsc.getAuthToken().isAdmin();

        boolean allAccts = request.getAllAccounts();
        if (allAccts) {
            WaitSetMgr.checkRightForAllAccounts(zsc);
        }

        List<WaitSetAccount> add = WaitSetRequest.parseAddUpdateAccounts(zsc, request.getAccounts(), defaultInterests);

        // workaround for 27480: load the mailboxes NOW, before we grab the waitset lock
        List<Mailbox> referencedMailboxes = new ArrayList<>();
        for (WaitSetAccount acct : add) {
            try {
                MailboxManager.FetchMode fetchMode = MailboxManager.FetchMode.AUTOCREATE;
                Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(acct.getAccountId(), fetchMode);
                referencedMailboxes.add(mbox);
            } catch (ServiceException e) {
                ZimbraLog.session.debug("Caught exception preloading mailbox for waitset", e);
            }
        }


        Pair<String, List<WaitSetError>> result = WaitSetMgr.create(zsc.getRequestedAccountId(), adminAllowed,
                defaultInterests, allAccts, add);
        String wsId = result.getFirst();
        List<WaitSetError> errors = result.getSecond();

        response.setWaitSetId(wsId);
        response.setDefaultInterests(WaitSetRequest.interestToStr(defaultInterests));
        response.setSequence(0);
        response.setErrors(WaitSetRequest.encodeErrors(errors));
    }
}
