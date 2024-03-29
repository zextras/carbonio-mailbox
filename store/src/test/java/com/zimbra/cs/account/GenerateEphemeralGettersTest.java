// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import com.zimbra.cs.account.AttributeManagerUtil.SetterType;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class GenerateEphemeralGettersTest {

 @Test
 void testStringGetters() throws Exception {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral, AttributeFlag.expirable);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_ASTRING,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.PURGE, true);
  ;

  String getter =
    "    public String getEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        return getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " null).getValue(null);\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(String zimbraEphemeralAttribute,"
      + " com.zimbra.cs.ephemeral.EphemeralInput.Expiration expiration) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " zimbraEphemeralAttribute, false, expiration);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  String purger =
    "    public void purgeEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        purgeEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
  testGeneratedMethod(sb, purger);
 }

 @Test
 void testMultiStringGetters() throws Exception {
  Set<AttributeFlag> flags =
    Sets.newHashSet(AttributeFlag.ephemeral, AttributeFlag.expirable, AttributeFlag.dynamic);
  AttributeInfo attributeInfo =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_ASTRING,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.multi,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder stringBuilder = new StringBuilder();
  AttributeManagerUtil.generateGetter(
    stringBuilder, attributeInfo, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(stringBuilder, attributeInfo, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(stringBuilder, attributeInfo, false, SetterType.ADD, true);
  AttributeManagerUtil.generateSetter(
    stringBuilder, attributeInfo, false, SetterType.UNSET, true);
  AttributeManagerUtil.generateSetter(
    stringBuilder, attributeInfo, false, SetterType.REMOVE, true);
  AttributeManagerUtil.generateSetter(
    stringBuilder, attributeInfo, false, SetterType.PURGE, true);
  ;
  AttributeManagerUtil.generateSetter(stringBuilder, attributeInfo, false, SetterType.HAS, true);
  ;

  String getter =
    "    public String getEphemeralAttribute(String dynamicComponent) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        return getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " dynamicComponent).getValue(null);\n"
      + "    }";

  String adder =
    "    public void addEphemeralAttribute(String dynamicComponent, String"
      + " zimbraEphemeralAttribute, com.zimbra.cs.ephemeral.EphemeralInput.Expiration"
      + " expiration) throws com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " dynamicComponent, zimbraEphemeralAttribute, true, expiration);\n"
      + "    }";

  String remover =
    "    public void removeEphemeralAttribute(String dynamicComponent, String"
      + " zimbraEphemeralAttribute) throws com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " dynamicComponent, zimbraEphemeralAttribute);\n"
      + "    }";

  String purger =
    "    public void purgeEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        purgeEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  String has =
    "    public boolean hasEphemeralAttribute(String dynamicComponent) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        return hasEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " dynamicComponent);\n"
      + "    }";

  testGeneratedMethod(stringBuilder, getter);
  testGeneratedMethod(stringBuilder, adder);
  testGeneratedMethod(stringBuilder, remover);
  testGeneratedMethod(stringBuilder, purger);
  testGeneratedMethod(stringBuilder, has);
 }

 @Test
 void testIntGetters() throws Exception {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_INTEGER,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);

  String getter =
    "    public int getEphemeralAttribute() throws com.zimbra.common.service.ServiceException"
      + " {\n"
      + "        return getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " null).getIntValue(-1);\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(int zimbraEphemeralAttribute) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " Integer.toString(zimbraEphemeralAttribute), false, null);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
 }

 @Test
 void testLongGetters() throws Exception {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_LONG,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);

  String getter =
    "    public long getEphemeralAttribute() throws com.zimbra.common.service.ServiceException"
      + " {\n"
      + "        return getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " null).getLongValue(-1L);\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(long zimbraEphemeralAttribute) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " Long.toString(zimbraEphemeralAttribute), false, null);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
 }

 @Test
 void testBooleanGetters() throws Exception {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_BOOLEAN,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);

  String getter =
    "    public boolean isEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        return getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " null).getBoolValue(false);\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(boolean zimbraEphemeralAttribute) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " zimbraEphemeralAttribute ? TRUE : FALSE, false, null);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
 }

 @Test
 void testEnumGetters() {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_ENUM,
      null,
      "foo,bar",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);

  String getter =
    "    public ZAttrProvisioning.EphemeralAttribute getEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        try { String v ="
      + " getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null).getValue();"
      + " return v == null ? null : ZAttrProvisioning.EphemeralAttribute.fromString(v); }"
      + " catch(com.zimbra.common.service.ServiceException e) { return null; }\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(ZAttrProvisioning.EphemeralAttribute"
      + " zimbraEphemeralAttribute) throws com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " zimbraEphemeralAttribute.toString(), false, null);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
 }

 @Test
 void testPortGetters() {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_PORT,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);

  String getter =
    "    public int getEphemeralAttribute() throws com.zimbra.common.service.ServiceException"
      + " {\n"
      + "        return getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " null).getIntValue(-1);\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(int zimbraEphemeralAttribute) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " Integer.toString(zimbraEphemeralAttribute), false, null);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
 }

 @Test
 void testDurationGetters() {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_DURATION,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);

  String getter =
    "    public long getEphemeralAttribute() throws com.zimbra.common.service.ServiceException"
      + " {\n"
      + "        return"
      + " getEphemeralTimeInterval(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " -1L);\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(String zimbraEphemeralAttribute) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " zimbraEphemeralAttribute, false, null);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
 }

 @Test
 void testTimeGetters() {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_GENTIME,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);

  String getter =
    "    public Date getEphemeralAttribute() throws com.zimbra.common.service.ServiceException"
      + " {\n"
      + "        String v = getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " null).getValue(null); return v == null ? null :"
      + " LdapDateUtil.parseGeneralizedTime(v);\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(Date zimbraEphemeralAttribute) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " zimbraEphemeralAttribute==null ? \"\" :"
      + " LdapDateUtil.toGeneralizedTime(zimbraEphemeralAttribute), false, null);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
 }

 @Test
 void testBinaryGetters() {
  Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.ephemeral);
  AttributeInfo ai =
    new AttributeInfo(
      "zimbraEphemeralAttribute",
      1,
      null,
      0,
      null,
      AttributeType.TYPE_BINARY,
      null,
      "",
      true,
      null,
      null,
      AttributeCardinality.single,
      Sets.newHashSet(AttributeClass.account),
      null,
      flags,
      null,
      null,
      null,
      null,
      null,
      "Test Ephemeral Attribute",
      null,
      null,
      null);
  StringBuilder sb = new StringBuilder();
  AttributeManagerUtil.generateGetter(sb, ai, false, AttributeClass.account);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.SET, true);
  AttributeManagerUtil.generateSetter(sb, ai, false, SetterType.UNSET, true);

  String getter =
    "    public byte[] getEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        String v = getEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute,"
      + " null).getValue(null); return v == null ? null : ByteUtil.decodeLDAPBase64(v);\n"
      + "    }";

  String setter =
    "    public void setEphemeralAttribute(byte[] zimbraEphemeralAttribute) throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute, null,"
      + " zimbraEphemeralAttribute==null ? \"\" :"
      + " ByteUtil.encodeLDAPBase64(zimbraEphemeralAttribute), false, null);\n"
      + "    }";

  String unsetter =
    "    public void unsetEphemeralAttribute() throws"
      + " com.zimbra.common.service.ServiceException {\n"
      + "        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraEphemeralAttribute);\n"
      + "    }";

  testGeneratedMethod(sb, getter);
  testGeneratedMethod(sb, setter);
  testGeneratedMethod(sb, unsetter);
 }

  private void testGeneratedMethod(StringBuilder generated, String shouldContain) {
    assertTrue(
        generated.toString().contains(shouldContain),
        String.format(
            "String '%s' should contain string '%s'", generated.toString(), shouldContain));
  }
}
