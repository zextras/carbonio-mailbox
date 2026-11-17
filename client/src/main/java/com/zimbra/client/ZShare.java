// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.XmlParseException;
import com.zimbra.common.zclient.ZClientException;

public class ZShare implements ToZJSONObject {

    /*
    <share xmlns="urn:zimbraShare" version="0.1" action="new" >
      <grantee id="4a4894bc-6d63-4589-afe8-b19352ffa779" email="user1@macpro.local" name="user1@macpro.local" />
      <grantor id="84ad414f-baef-43c1-938d-9f1ecf2a2489" email="user3@macpro.local" name="Demo User Three" />
      <link id="10" name="Calendar" view="appointment" perm="r" />
      <notes></notes>
    </share>
     */

    public static final String E_SHARE = "share";
    public static final String E_GRANTEE = "grantee";
    public static final String E_GRANTOR = "grantor";
    public static final String E_LINK = "link";

    public static final String A_ID = "id";
    public static final String A_EMAIL = "email";
    public static final String A_NAME = "name";
    public static final String A_VIEW = "view";
    public static final String A_PERM = "perm";
    public static final String A_ACTION = "action";
    public static final String A_VERSION = "version";

    private ZGrantee mGrantee;
    private ZGrantor mGrantor;
    private ZLink mLink;
    private String mVersion;
    private ZShareAction mAction;

	public static ZShare parseXml(String xml) throws ServiceException {
        try {
            return new ZShare(Element.parseXML(xml));
        } catch (XmlParseException e) {
            throw ZClientException.ZIMBRA_SHARE_PARSE_ERROR("can't parse share", e);
        }
    }

    public ZShare(Element e) throws ServiceException {
        Element granteeEl = e.getOptionalElement(E_GRANTEE);
        if (granteeEl != null)
            mGrantee = new ZGrantee(granteeEl);

        Element grantorEl = e.getOptionalElement(E_GRANTOR);
        if (grantorEl != null)
            mGrantor = new ZGrantor(grantorEl);

        Element linkEl = e.getOptionalElement(E_LINK);
        if (linkEl != null)
            mLink = new ZLink(linkEl);

        mVersion = e.getAttribute(A_VERSION);
        mAction = ZShareAction.fromString(e.getAttribute(A_ACTION));
    }

	@Override
    public ObjectNode toObjectNode() {
        ObjectNode zjo = JsonNodeFactory.instance.objectNode();
        if (mVersion != null) zjo.put(A_VERSION, mVersion);
        zjo.put(A_ACTION, mAction.name());
        if (mGrantee != null) zjo.set(E_GRANTEE, mGrantee.toObjectNode());
        if (mGrantor != null) zjo.set(E_GRANTOR, mGrantor.toObjectNode());
        if (mLink != null) zjo.set(E_LINK, mLink.toObjectNode());
        return zjo;
    }

    @Override
    public String toString() {
        return String.format("[ZShare %s]", mGrantor);
    }

	public enum ZShareAction {
        NEW, EDIT, DELETE, ACCEPT, DECLINE;

        public static ZShareAction fromString(String s) throws ServiceException {
            try {
                return ZShareAction.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw ZClientException.CLIENT_ERROR("invalid share action "+s+", valid values: new, edit, delete, accept, decline", e);
            }
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static class ZGrantInfo implements ToZJSONObject {

        private String mId;
        private String mEmail;
        private String mName;

        public ZGrantInfo(Element e) throws ServiceException {
            mId = e.getAttribute(A_ID);
            mEmail = e.getAttribute(A_EMAIL);
            mName =  e.getAttribute(A_NAME);
        }

        @Override
        public ObjectNode toObjectNode() {
            ObjectNode zjo = JsonNodeFactory.instance.objectNode();
            if (mId != null) zjo.put(A_ID, mId);
            if (mEmail != null) zjo.put(A_EMAIL, mEmail);
            if (mName != null) zjo.put(A_NAME, mName);
            return zjo;
        }

        @Override
        public String toString() {
            return String.format("[ZGrantInfo id=%s email=%s name=%s]", mId, mEmail, mName);
        }
    }

    public static class ZGrantor extends ZGrantInfo {

			public ZGrantor(Element e) throws ServiceException {
            super(e);
        }

    }

    public static class ZGrantee extends ZGrantInfo {

			public ZGrantee(Element e) throws ServiceException {
            super(e);
        }

    }

    public static class ZLink implements ToZJSONObject {

        private String mId;
        private ZFolder.View mView;
        private String mName;
        private String mPermission;

			public ZLink(Element e) throws ServiceException {
            mId = e.getAttribute(A_ID);
            mPermission = e.getAttribute(A_PERM);
            mName =  e.getAttribute(A_NAME);
            mView = ZFolder.View.fromString(e.getAttribute(A_VIEW));
        }

        @Override
        public ObjectNode toObjectNode() {
            ObjectNode zjo = JsonNodeFactory.instance.objectNode();
            if (mId != null) zjo.put(A_ID, mId);
            if (mPermission != null) zjo.put(A_PERM, mPermission);
            zjo.put(A_VIEW, mView.name());
            if (mName != null) zjo.put(A_NAME, mName);
            return zjo;
        }

        @Override
        public String toString() {
            return String.format("[ZLink id=%s name=%s]", mId, mName);
        }
    }
}
