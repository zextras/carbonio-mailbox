// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.cs.mailbox.ContactAutoComplete.ContactEntryType;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.ContactAutoComplete;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.ContactAutoComplete.AutoCompleteResult;
import com.zimbra.cs.mailbox.ContactAutoComplete.ContactEntry;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.type.GalSearchType;

public class AutoComplete extends MailDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(getZimbraSoapContext(context));
        OperationContext octxt = getOperationContext(zsc, context);

        String name = request.getAttribute(MailConstants.A_NAME);

        // remove commas (bug 46540)
        name = name.replace(",", " ").trim();

        if (StringUtils.isEmpty(name)) {
            throw ServiceException.INVALID_REQUEST("name parameter is empty", null);
        }

        GalSearchType type = GalSearchType.fromString(request.getAttribute(MailConstants.A_TYPE, "account"));
        int limit = account.getContactAutoCompleteMaxResults();
        boolean needCanExpand = request.getAttributeBool(MailConstants.A_NEED_EXP, false);

        AutoCompleteResult result = query(request, zsc, account, false, name, limit, type, needCanExpand, octxt);
        Element response = zsc.createElement(MailConstants.AUTO_COMPLETE_RESPONSE);
        toXML(response, result, zsc.getAuthtokenAccountId());

        return response;
    }

    @Override
    public boolean needsAuth(Map<String, Object> context) {
        return true;
    }

    private ArrayList<Integer> csvToArray(String csv) {
        if (csv == null)
            return null;
        ArrayList<Integer> array = new ArrayList<>();
        for (String f : csv.split(",")) {
            array.add(Integer.parseInt(f));
        }
        return array;
    }

    protected AutoCompleteResult query(Element request, ZimbraSoapContext zsc, Account account,
            boolean excludeGal, String name, int limit, GalSearchType type, OperationContext octxt) throws ServiceException {
        return query(request, zsc, account, excludeGal, name, limit, type, false, octxt);
    }

    protected AutoCompleteResult query(Element request, ZimbraSoapContext zsc, Account account,
            boolean excludeGal, String name, int limit, GalSearchType type, boolean needCanExpand, OperationContext octxt) throws ServiceException {

       ArrayList<Integer> folders = csvToArray(request.getAttribute(MailConstants.A_FOLDERS, null));
       ContactAutoComplete autoComplete = new ContactAutoComplete(account, zsc, octxt);
       autoComplete.setNeedCanExpand(needCanExpand);
       autoComplete.setSearchType(type);
       boolean includeGal = !excludeGal && request.getAttributeBool(MailConstants.A_INCLUDE_GAL, autoComplete.includeGal());
       autoComplete.setIncludeGal(includeGal);
       return autoComplete.query(name, folders, limit);
    }

    protected void toXML(Element response, AutoCompleteResult result, String authAccountId) {
        response.addAttribute(MailConstants.A_CANBECACHED, result.canBeCached);
        for (ContactEntry entry : result.entries) {
            Element cn = response.addNonUniqueElement(MailConstants.E_MATCH);
            
            // for contact group, emails of members will be expanded 
            // separately on user request
            if (!entry.isContactGroup()) {
                cn.addAttribute(MailConstants.A_EMAIL, entry.getEmail());
            }
            cn.addAttribute(MailConstants.A_MATCH_TYPE, getType(entry));
            cn.addAttribute(MailConstants.A_RANKING, Integer.toString(entry.getRanking()));
            cn.addAttribute(MailConstants.A_IS_GROUP, entry.isGroup());
            if (entry.isGroup() && entry.canExpandGroupMembers()) {
                cn.addAttribute(MailConstants.A_EXP, true);
            }
            
            ItemId id = entry.getId();
            if (id != null) {
                cn.addAttribute(MailConstants.A_ID, id.toString(authAccountId));
            }
            
            int folderId = entry.getFolderId();
            if (folderId > 0) {
                cn.addAttribute(MailConstants.A_FOLDER, Integer.toString(folderId));
            }
            if (entry.isContactGroup()) {
                cn.addAttribute(MailConstants.A_DISPLAYNAME, entry.getDisplayName());
            }

            String firstName = entry.getFirstName();
            if (firstName != null) {
                cn.addAttribute(MailConstants.A_FIRSTNAME, firstName);
            }

            String middleName = entry.getMiddleName();
            if (middleName != null) {
                cn.addAttribute(MailConstants.A_MIDDLENAME, middleName);
            }

            String lastName = entry.getLastName();
            if (lastName != null) {
                cn.addAttribute(MailConstants.A_LASTNAME, lastName);
            }

            String fullName = entry.getFullName();
            if (fullName != null) {
                cn.addAttribute(MailConstants.A_FULLNAME, fullName);
            }

            String nickname = entry.getNickname();
            if (nickname != null) {
                cn.addAttribute(MailConstants.A_NICKNAME, nickname);
            }

            String company = entry.getCompany();
            if (company != null) {
                cn.addAttribute(MailConstants.A_COMPANY, company);
            }

            String fileAs = entry.getFileAs();
            if (fileAs != null) {
                cn.addAttribute(MailConstants.A_FILEAS, fileAs);
            }
        }
    }

    private String getType(ContactEntry entry) {
        if (entry.getFolderId() == ContactAutoComplete.FOLDER_ID_GAL)
            return ContactEntryType.GAL.getName();
        else if (entry.getFolderId() == ContactAutoComplete.FOLDER_ID_UNKNOWN)
            return ContactEntryType.RANKING_TABLE.getName();
        else
            return ContactEntryType.CONTACT.getName();
    }
}
