// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.gal;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;

import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.GalContact;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.type.GalSearchType;

public class GalGroupMembers {

    // common super interface for all the DLMembers classes
    public abstract static class DLMembersResult {
        protected Set<String> mMembersSet;

        protected abstract Set<String> getAllMembers() throws ServiceException;

        protected Set<String> createMembersSet() {
            return new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        }
    }

    public abstract static class DLMembers extends DLMembersResult {

        public abstract int getTotal();

        public abstract String getDLZimbraId();

        /**
         *
         * @param beginIndex the beginning index, inclusive.
         * @param endIndex   the ending index, exclusive.
         * @param resp
         */
        public abstract void encodeMembers(int beginIndex, int endIndex, Element resp);

        protected Element encodeMember(Element parent, String member) {
            return parent.addElement(AccountConstants.E_DLM).setText(member);
        }
    }

    public static class ContactDLMembers extends DLMembers {
        private Contact mContact;
        private JSONArray mMembers;

        public ContactDLMembers(Contact contact) {
            mContact = contact;

            String members = mContact.get(ContactConstants.A_member);

            if (members != null) {
                try {
                    mMembers = Contact.getMultiValueAttrArray(members);
                } catch (JSONException e) {
                    ZimbraLog.account.warn("unable to get members from Contact " + mContact.getId(), e);
                }
            }
        }

        @Override
        public int getTotal() {
            if (mMembers == null)
                return 0;
            else
                return mMembers.length();
        }

        @Override
        public String getDLZimbraId() {
            return mContact.get(ContactConstants.A_zimbraId);
        }

        @Override
        public void encodeMembers(int beginIndex, int endIndex, Element resp) {
            if (mMembers == null)
                return;

            if (endIndex <= getTotal()) {
                try {
                    for (int i = beginIndex; i < endIndex; i++) {
                        encodeMember(resp, mMembers.getString(i));
                    }
                } catch (JSONException e) {
                    ZimbraLog.account.warn("unable to get members from Contact " + mContact.getId(), e);
                }
            }
        }

        @Override
        public Set<String> getAllMembers() {
            if (mMembersSet != null) {
                return mMembersSet;
            } else {
                mMembersSet = createMembersSet();
            }

            if (mMembers != null) {
                try {
                    for (int i = 0; i < getTotal(); i++) {
                        mMembersSet.add(mMembers.getString(i));
                    }
                } catch (JSONException e) {
                    ZimbraLog.account.warn("unable to get members from Contact " + mContact.getId(), e);
                }
            }

            return mMembersSet;
        }

    }

    public static class GalContactDLMembers extends DLMembers {
        private GalContact mGalContact;
        private String[] mMembers;
        private Set<String> mMembersSet;

        private GalContactDLMembers(GalContact galContact) {
            mGalContact = galContact;

            Object members = mGalContact.getAttrs().get(ContactConstants.A_member);
            if (members instanceof String)
                mMembers = new String[]{(String)members};
            else if (members instanceof String[])
                mMembers = (String[])members;
        }

        @Override
        public int getTotal() {
            if (mMembers == null)
                return 0;
            else
                return mMembers.length;
        }

        @Override
        public String getDLZimbraId() {
            return mGalContact.getSingleAttr(ContactConstants.A_zimbraId);
        }

        @Override
        public void encodeMembers(int beginIndex, int endIndex, Element resp) {
            if (mMembers == null)
                return;

            if (endIndex <= getTotal()) {
                for (int i = beginIndex; i < endIndex; i++) {
                    encodeMember(resp, mMembers[i]);
                }
            }
        }

        @Override
        public Set<String> getAllMembers() {
            if (mMembersSet != null) {
                return mMembersSet;
            } else {
                mMembersSet = createMembersSet();
            }

            if (mMembers != null) {
                mMembersSet.addAll(Arrays.asList(mMembers));
            }

            return mMembersSet;
        }

    }

    public static class LdapDLMembers extends DLMembers {
        private Group group;
        private String[] allMembers;

        public LdapDLMembers(Group group) throws ServiceException {
            this.group = group;
            this.allMembers = Provisioning.getInstance().getGroupMembers(group);
        }

        @Override
        public void encodeMembers(int beginIndex, int endIndex, Element resp) {
            if (endIndex <= getTotal()) {
                for (int i = beginIndex; i < endIndex; i++) {
                    encodeMember(resp, allMembers[i]);
                }
            }
        }

        @Override
        public String getDLZimbraId() {
            return group.getId();
        }

        @Override
        public int getTotal() {
            return allMembers.length;
        }

        @Override
        protected Set<String> getAllMembers() throws ServiceException {
            return group.getAllMembersSet();
        }

    }

    public static class ProxiedDLMembers extends DLMembersResult {
        private Element mResponse;
        Set<String> mMembersSet;

        public ProxiedDLMembers(Element response) {
            mResponse = response;
            mResponse.detach();
        }

        public Element getResponse() {
            return mResponse;
        }

        @Override
        public Set<String> getAllMembers() {
            if (mMembersSet != null) {
                return mMembersSet;
            } else {
                mMembersSet = createMembersSet();
            }

            for (Element eDLM : mResponse.listElements(AccountConstants.E_DLM)) {
                mMembersSet.add(eDLM.getText());
            }

            return mMembersSet;
        }
    }

    private static class GalGroupMembersCallback extends GalSearchResultCallback {
        private DLMembersResult mDLMembers;

        GalGroupMembersCallback(GalSearchParams params) {
            super(params);
        }

        @Override
        public boolean passThruProxiedGalAcctResponse() {
            return true;
        }

        DLMembersResult getDLMembers() {
            return mDLMembers;
        }

        @Override
        public void handleProxiedResponse(Element resp) {
            mDLMembers = new ProxiedDLMembers(resp);
        }

        @Override
        public Element handleContact(Contact contact) throws ServiceException {
            mDLMembers = new ContactDLMembers(contact);
            return null;
        }

        @Override
        public void handleContact(GalContact galContact) throws ServiceException {
            mDLMembers = new GalContactDLMembers(galContact);
        }

        @Override
        public void handleElement(Element e) throws ServiceException {
            // should never be called
        }
    }


    public static DLMembersResult searchGal(ZimbraSoapContext zsc, Account account, String groupName, Element request)
    throws ServiceException {
        GalSearchParams params = new GalSearchParams(account, zsc);
        params.setQuery(groupName);
        params.setType(GalSearchType.group);
        params.setLimit(1);
        params.setFetchGroupMembers(true);
        params.setRequest(request);
        GalGroupMembersCallback callback = new GalGroupMembersCallback(params);
        params.setResultCallback(callback);

        GalSearchControl gal = new GalSearchControl(params);
        gal.search(false);
        return callback.getDLMembers();
    }

    /**
     * return all members of a GAL group
     *
     * @param groupName
     * @param account    The requested account.  It is needed for getting the GAL configuration.
     * @return
     * @throws ServiceException
     */
    public static Set<String> getGroupMembers(String groupName, Account account) throws ServiceException {

        // create a ZimbraSoapContext and request for GAL sync account proxy (in case it has to do so)
        // use the global admin's credentials to bypass any permission check
        //
        AuthToken adminAuthToken = AuthProvider.getAdminAuthToken();
        ZimbraSoapContext zsc = new ZimbraSoapContext(adminAuthToken, account.getId(), SoapProtocol.Soap12, SoapProtocol.Soap12);

        Element request = Element.create(SoapProtocol.Soap12, AccountConstants.GET_DISTRIBUTION_LIST_MEMBERS_REQUEST);
        Element eDL = request.addElement(AdminConstants.E_DL).setText(groupName);

        DLMembersResult dlMembersResult = searchGal(zsc, account, groupName, request);

        if (dlMembersResult == null) {
            throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(groupName);
        }

        return dlMembersResult.getAllMembers();
    }

}
