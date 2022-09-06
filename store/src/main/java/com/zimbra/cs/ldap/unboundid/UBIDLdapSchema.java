// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap.unboundid;

import com.unboundid.ldap.sdk.schema.ObjectClassDefinition;
import com.unboundid.ldap.sdk.schema.Schema;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZLdapSchema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class UBIDLdapSchema extends ZLdapSchema {

  private Schema schema;

  UBIDLdapSchema(Schema schema) {
    this.schema = schema;
  }

  @Override
  public void debug() {}

  public static class UBIDObjectClassDefinition extends ZObjectClassDefinition {

    private ObjectClassDefinition ocDef;

    private UBIDObjectClassDefinition(ObjectClassDefinition ocDef) {
      this.ocDef = ocDef;
    }

    @Override
    public void debug() {}

    ObjectClassDefinition getNative() {
      return ocDef;
    }

    @Override
    public String getName() {
      return ocDef.getNameOrOID();
    }

    @Override
    public List<String> getSuperiorClasses() throws LdapException {
      return Arrays.asList(ocDef.getSuperiorClasses());
    }

    @Override
    public List<String> getOptionalAttributes() throws LdapException {
      return Arrays.asList(ocDef.getOptionalAttributes());
    }

    @Override
    public List<String> getRequiredAttributes() throws LdapException {
      return Arrays.asList(ocDef.getRequiredAttributes());
    }
  }

  @Override
  public ZObjectClassDefinition getObjectClass(String objectClass) throws LdapException {
    ObjectClassDefinition oc = schema.getObjectClass(objectClass);
    if (oc == null) {
      return null;
    } else {
      return new UBIDObjectClassDefinition(oc);
    }
  }

  @Override
  public List<ZObjectClassDefinition> getObjectClasses() throws LdapException {
    List<ZObjectClassDefinition> ocList = new ArrayList<ZObjectClassDefinition>();

    Set<ObjectClassDefinition> ocs = schema.getObjectClasses();
    for (ObjectClassDefinition oc : ocs) {
      UBIDObjectClassDefinition ubidOC = new UBIDObjectClassDefinition(oc);
      ocList.add(ubidOC);
    }

    Comparator comparator =
        new Comparator<UBIDObjectClassDefinition>() {
          public int compare(UBIDObjectClassDefinition first, UBIDObjectClassDefinition second) {
            return first.getName().compareTo(second.getName());
          }
        };

    Collections.sort(ocList, comparator);
    return ocList;
  }
}
