// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.account.AttributeManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZAttributes extends ZLdapElement implements IAttributes {

    private static String[] EMPTY_STRING_ARRAY = new String[0];

    //
    // The wrapped object here is actually the SearchResultEntry or Entry object.
    // Unlike JNDI, unboundid handles attributes on the (SearchResult)Entry object.
    //
    private Entry entry;

    public ZAttributes(SearchResultEntry entry) {
        this.entry = entry;
    }

    public ZAttributes(Entry entry) {
        this.entry = entry;
    }

    @Override
    public void debug() {
        for (Attribute attr : entry.getAttributes()) {
            println(attr.toString());
        }
    }

    @Override
    public String getAttrString(String attrName) throws LdapException {
        return getAttrString(attrName, CheckBinary.NOCHECK);
    }

    // make public if necessary
    private String getAttrString(String attrName, CheckBinary checkBinary)
    throws LdapException {
        boolean containsBinaryData;
        String transferAttrName;

        if (checkBinary == CheckBinary.NOCHECK) {
            containsBinaryData = false;
            transferAttrName = attrName;
        } else {
            AttributeManager attrMgr = AttributeManager.getInst();
            containsBinaryData = attrMgr == null ? false : attrMgr.containsBinaryData(attrName);
            boolean isBinaryTransfer = attrMgr == null ? false : attrMgr.isBinaryTransfer(attrName);

            transferAttrName = LdapUtil.attrNameToBinaryTransferAttrName(isBinaryTransfer, attrName);
        }

        return getAttrString(transferAttrName, containsBinaryData);
    }

    @Override
    public String[] getMultiAttrString(String attrName) throws LdapException {
        return getMultiAttrString(attrName, CheckBinary.NOCHECK);
    }

    // make public if necessary
    private String[] getMultiAttrString(String attrName, CheckBinary checkBinary)
    throws LdapException {
        boolean containsBinaryData;
        boolean isBinaryTransfer;

        if (checkBinary == CheckBinary.NOCHECK) {
            containsBinaryData = false;
            isBinaryTransfer = false;
        } else {
            AttributeManager attrMgr = AttributeManager.getInst();
            containsBinaryData = attrMgr == null ? false : attrMgr.containsBinaryData(attrName);
            isBinaryTransfer = attrMgr == null ? false : attrMgr.isBinaryTransfer(attrName);
        }

        return getMultiAttrString(attrName, containsBinaryData, isBinaryTransfer);
    }

    @Override
    public String[] getMultiAttrString(String attrName, boolean containsBinaryData, boolean isBinaryTransfer)
    throws LdapException {
        String transferAttrName = LdapUtil.attrNameToBinaryTransferAttrName(isBinaryTransfer, attrName);
        return getMultiAttrString(transferAttrName, containsBinaryData);
    }

    @Override
    public List<String> getMultiAttrStringAsList(String attrName, CheckBinary checkBinary)
    throws LdapException {
        if (checkBinary == CheckBinary.NOCHECK) {
            return Arrays.asList(getMultiAttrString(attrName, false));
        } else {
            return Arrays.asList(getMultiAttrString(attrName));
        }
    }

    /**
     * Enumerates over the specified attributes and populates the specified map.
     * The key in the map is the attribute ID. For attrs with a single value,
     * the value is a String, and for attrs with multiple values the value is an
     * array of Strings.
     *
     * Note: this method always *check* binary.
     */
    public Map<String, Object> getAttrs() throws LdapException {
        return getAttrs(null);
    }

    private String getAttrStringInternal(Attribute attr, boolean containsBinaryData) {
        if (containsBinaryData) {
            byte[] bytes = attr.getValueByteArray();
            return ByteUtil.encodeLDAPBase64(bytes);
        } else {
            return attr.getValue();
        }
    }

    private String[] getMultiAttrStringInternal(Attribute attr, boolean containsBinaryData) {
        String[] result = new String[attr.size()];

        if (containsBinaryData) {
            byte[][] bytesArrays = attr.getValueByteArrays();
            for (int i = 0; i < bytesArrays.length; i++) {
                result[i] = ByteUtil.encodeLDAPBase64(bytesArrays[i]);
            }
        } else {
            String[] values = attr.getValues();
            System.arraycopy(values, 0, result, 0, values.length);
        }
        return result;
    }

    protected String getAttrString(String transferAttrName, boolean containsBinaryData)
    throws LdapException {
        Attribute attr = entry.getAttribute(transferAttrName);
        if (attr != null) {
            return getAttrStringInternal(attr, containsBinaryData);
        } else {
            return null;
        }
    }

    protected String[] getMultiAttrString(String transferAttrName, boolean containsBinaryData)
    throws LdapException {
        Attribute attr = entry.getAttribute(transferAttrName);
        // (ZCS-1047) AD sends 'userCertificate;binary' attribute as 'userCertificate' without appending ';binary' to it
        if (attr == null && transferAttrName.startsWith(ContactConstants.A_userCertificate)) {
            attr = entry.getAttribute(ContactConstants.A_userCertificate);
        }

        if (attr != null) {
            return getMultiAttrStringInternal(attr, containsBinaryData);
        } else {
            return EMPTY_STRING_ARRAY;
        }
    }

    /**
     * extraBinaryAttrs: if not null, attrs in the set are treated as binary attrs, in addition to
     * those marked binary in Zimbra's AttributeManager.
     */
    public Map<String, Object> getAttrs(Set<String> extraBinaryAttrs)
            throws LdapException {
        Map<String, Object> map = new HashMap<>();

        AttributeManager attrMgr = AttributeManager.getInst();

        for (Attribute attr : entry.getAttributes()) {
            String transferAttrName = attr.getName();

            String attrName = LdapUtil.binaryTransferAttrNameToAttrName(transferAttrName);

            boolean containsBinaryData =
                (attrMgr != null && attrMgr.containsBinaryData(attrName)) ||
                (extraBinaryAttrs != null && extraBinaryAttrs.contains(attrName));

            if (attr.size() == 1) {
                map.put(attrName, getAttrStringInternal(attr, containsBinaryData));
            } else {
                String result[] = getMultiAttrStringInternal(attr, containsBinaryData);
                map.put(attrName, result);
            }
        }
        return map;
    }

    @Override
    public boolean hasAttribute(String attrName) {
        return entry.hasAttribute(attrName);
    }

    @Override
    public boolean hasAttributeValue(String attrName, String value) {
        return entry.hasAttributeValue(attrName, value);
    }

}
