// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap.unboundid;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.schema.Schema;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.ldap.LdapUtil;

public class UBIDUtil {

  static ASN1OctetString newASN1OctetString(boolean isBinary, String value) {
    if (isBinary) {
      return new ASN1OctetString(ByteUtil.decodeLDAPBase64(value));
    } else {
      return new ASN1OctetString(value);
    }
  }

  static Attribute newAttribute(boolean isBinaryTransfer, String attrName, ASN1OctetString value) {
    String transferAttrName = LdapUtil.attrNameToBinaryTransferAttrName(isBinaryTransfer, attrName);
    return new Attribute(transferAttrName, value);
  }

  static Attribute newAttribute(
      boolean isBinaryTransfer, String attrName, ASN1OctetString[] values) {
    String transferAttrName = LdapUtil.attrNameToBinaryTransferAttrName(isBinaryTransfer, attrName);
    return new Attribute(transferAttrName, (Schema) null, values);
  }
}
