// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.mailbox.Contact.Attachment;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.util.TagUtil;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.cs.session.PendingModifications.Change;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author schemers
 */
public class ModifyContact extends MailDocumentHandler  {

    private static final String[] TARGET_FOLDER_PATH = new String[] { MailConstants.E_CONTACT, MailConstants.A_ID };
    @Override
    protected String[] getProxiedIdPath(Element request)     { return TARGET_FOLDER_PATH; }
    @Override
    protected boolean checkMountpointProxy(Element request)  { return false; }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        OperationContext octxt = getOperationContext(zsc, context);

        boolean replace = request.getAttributeBool(MailConstants.A_REPLACE, false);
        boolean verbose = request.getAttributeBool(MailConstants.A_VERBOSE, true);
        boolean wantImapUid = request.getAttributeBool(MailConstants.A_WANT_IMAP_UID, true);
        boolean wantModSeq = request.getAttributeBool(MailConstants.A_WANT_MODIFIED_SEQUENCE, false);

        Element cn = request.getElement(MailConstants.E_CONTACT);
        ItemId iid = new ItemId(cn.getAttribute(MailConstants.A_ID), zsc);
        String tagsAttr = cn.getAttribute(MailConstants.A_TAG_NAMES, null);

        Contact contact = mbox.getContactById(octxt, iid.getId());

        ParsedContact pc;
        if (replace) {
            Pair<Map<String,Object>, List<Attachment>> cdata = CreateContact.parseContact(cn, zsc, octxt, contact);
            pc = new ParsedContact(cdata.getFirst(), cdata.getSecond());
        } else {
            pc = CreateContact.parseContactMergeMode(cn, zsc, octxt, contact);
        }

        if (CreateContact.needToMigrateDlist(zsc)) {
            CreateContact.migrateFromDlist(pc);
        }

        mbox.modifyContact(octxt, iid.getId(), pc);
        if (tagsAttr != null) {
            String[] tags =  TagUtil.decodeTags(tagsAttr);
            if (tags != null) {
                mbox.setTags(octxt, iid.getId(), MailItem.Type.CONTACT, MailItem.FLAG_UNCHANGED, tags);
            }
        }

        Contact con = mbox.getContactById(octxt, iid.getId());
        return makeResponse(zsc, octxt, con, verbose, wantImapUid, wantModSeq);
    }

    private Element makeResponse(ZimbraSoapContext zsc, OperationContext octxt, Contact con, boolean verbose,
            boolean wantImapUid, boolean wantModSeq) throws ServiceException {
        Element response = zsc.createElement(MailConstants.MODIFY_CONTACT_RESPONSE);
        if (con != null) {
            if (verbose) {
                int fields = ToXML.NOTIFY_FIELDS;
                if (wantImapUid) {
                    fields |= Change.IMAP_UID;
                }
                if (wantModSeq) {
                    fields |= Change.MODSEQ;
                }
                ItemIdFormatter ifmt = new ItemIdFormatter(zsc);
                ToXML.encodeContact(response, ifmt, octxt, con,
                    null, null /* memberAttrFilter */, true /* summary */,
                    null /* attrFilter */, fields, null /* migratedDList */,
                        false /* returnHiddenAttrs */,
                        GetContacts.NO_LIMIT_MAX_MEMBERS, true /* returnCertInfo */);
            } else {
                Element contct = response.addNonUniqueElement(MailConstants.E_CONTACT);
                contct.addAttribute(MailConstants.A_ID, con.getId());
                if (wantImapUid) {
                    contct.addAttribute(MailConstants.A_IMAP_UID, con.getImapUid());
                }
                if (wantModSeq) {
                    contct.addAttribute(MailConstants.A_MODIFIED_SEQUENCE, con.getModifiedSequence());
                }
            }
        }
        return response;
    }

    static Map<String, String> parseFields(List<Element> elist) throws ServiceException {
        if (elist == null || elist.isEmpty())
            return null;

        HashMap<String, String> attrs = new HashMap<>();
        for (Element e : elist) {
            String name = e.getAttribute(MailConstants.A_ATTRIBUTE_NAME);
            attrs.put(name, e.getText());
        }
        return attrs;
    }
}
