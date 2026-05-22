// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.unboundid.ldap.sdk.schema.ObjectClassDefinition;
import com.unboundid.ldap.sdk.schema.Schema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ZLdapSchema extends ZLdapElement {

    private Schema schema;

    public ZLdapSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public void debug() {
    }

    public static class ZObjectClassDefinition extends ZLdapElement {

        private ObjectClassDefinition ocDef;

        private ZObjectClassDefinition(ObjectClassDefinition ocDef) {
            this.ocDef = ocDef;
        }

        @Override
        public void debug() {
        }

        public ObjectClassDefinition getNative() {
            return ocDef;
        }

        public String getName() {
            return ocDef.getNameOrOID();
        }

        public List<String> getSuperiorClasses() throws LdapException {
            return Arrays.asList(ocDef.getSuperiorClasses());
        }

        public List<String> getOptionalAttributes() throws LdapException {
            return Arrays.asList(ocDef.getOptionalAttributes());
        }

        public List<String> getRequiredAttributes() throws LdapException {
            return Arrays.asList(ocDef.getRequiredAttributes());
        }
    }

    /**
     * Retrieves the object class with the specified name or OID from the server schema.
     *
     * @return The requested object class, or null if there is no such class defined in the server schema.
     */
    public ZObjectClassDefinition getObjectClass(String objectClass)
    throws LdapException {
        ObjectClassDefinition oc = schema.getObjectClass(objectClass);
        if (oc == null) {
            return null;
        } else {
            return new ZObjectClassDefinition(oc);
        }
    }

    public List<ZObjectClassDefinition> getObjectClasses() throws LdapException {
        List<ZObjectClassDefinition> ocList = new ArrayList<>();

        Set<ObjectClassDefinition> ocs = schema.getObjectClasses();
        for (ObjectClassDefinition oc : ocs) {
            ocList.add(new ZObjectClassDefinition(oc));
        }

        Comparator<ZObjectClassDefinition> comparator =
                (first, second) -> first.getName().compareTo(second.getName());

        ocList.sort(comparator);
        return ocList;
    }
}
