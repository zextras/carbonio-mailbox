// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import java.util.List;

public abstract class ZLdapSchema extends ZLdapElement {

  public abstract List<ZObjectClassDefinition> getObjectClasses() throws LdapException;

  /**
   * Retrieves the object class with the specified name or OID from the server schema.
   *
   * @param objectClass
   * @return The requested object class, or null if there is no such class defined in the server
   *     schema.
   * @throws LdapException
   */
  public abstract ZObjectClassDefinition getObjectClass(String objectClass) throws LdapException;

  public abstract static class ZObjectClassDefinition extends ZLdapElement {
    public abstract String getName();

    public abstract List<String> getSuperiorClasses() throws LdapException;

    public abstract List<String> getOptionalAttributes() throws LdapException;

    public abstract List<String> getRequiredAttributes() throws LdapException;
  }
}
