package com.zextras.mailbox.feature;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.account.AttributeManager;
import java.util.List;
import java.util.Map;

public record StoredEntry(String dn, Map<String, List<String>> attributes) {

  public Entry toLdapEntry() {
    AttributeManager attributeManager = AttributeManager.getInst();
    Entry entry = new Entry(dn);
    attributes.forEach(
        (name, values) -> entry.addAttribute(attribute(attributeManager, name, values)));
    return entry;
  }

  private static Attribute attribute(
      AttributeManager attributeManager, String name, List<String> values) {
    if (attributeManager != null && attributeManager.containsBinaryData(name)) {
      return new Attribute(
          name, values.stream().map(ByteUtil::decodeLDAPBase64).toArray(byte[][]::new));
    }
    return new Attribute(name, values);
  }
}
