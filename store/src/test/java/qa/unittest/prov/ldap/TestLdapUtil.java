// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.prov.ldap;

import static org.junit.Assert.*;

import com.zimbra.cs.ldap.LdapUtil;
import org.junit.*;

public class TestLdapUtil extends LdapTest {

  @Test
  public void testAuthDN() {

    assertEquals(
        "schemers@example.zimbra.com", LdapUtil.computeDn("schemers@example.zimbra.com", null));

    assertEquals(
        "schemers@example.zimbra.com", LdapUtil.computeDn("schemers@example.zimbra.com", ""));

    assertEquals("WTF", LdapUtil.computeDn("schemers@example.zimbra.com", "WTF"));

    assertEquals(
        "schemers@example.zimbra.com", LdapUtil.computeDn("schemers@example.zimbra.com", "%n"));

    assertEquals("schemers", LdapUtil.computeDn("schemers@example.zimbra.com", "%u"));

    assertEquals("example.zimbra.com", LdapUtil.computeDn("schemers@example.zimbra.com", "%d"));

    assertEquals(
        "dc=example,dc=zimbra,dc=com", LdapUtil.computeDn("schemers@example.zimbra.com", "%D"));

    assertEquals(
        "uid=schemers,ou=people,dc=example,dc=zimbra,dc=com",
        LdapUtil.computeDn("schemers@example.zimbra.com", "uid=%u,ou=people,%D"));

    assertEquals(
        "n(schemers@example.zimbra.com)u(schemers)d(example.zimbra.com)D(dc=example,dc=zimbra,dc=com)(%)",
        LdapUtil.computeDn("schemers@example.zimbra.com", "n(%n)u(%u)d(%d)D(%D)(%%)"));
  }

  @Test
  @Ignore // only for experiment
  public void rdnUBID() throws Exception {
    // com.unboundid.ldap.sdk.RDN rdn = new com.unboundid.ldap.sdk.RDN("cn", "foo+/+/
    // \u4e2d\u6587");
    String rawValue = "## ,+\"\\<>;\u4e2d\u6587---createIdentity ";

    com.unboundid.ldap.sdk.RDN rdn = new com.unboundid.ldap.sdk.RDN("cn", rawValue);
    String minStr = rdn.toMinimallyEncodedString();
    String rdnStr = rdn.toNormalizedString();
    System.out.println(minStr);
    System.out.println(rdnStr);

    String escapedValue = com.unboundid.ldap.sdk.Filter.encodeValue(rawValue);
    System.out.println(escapedValue);

    /*
    String raw = "(&(objectclass=zimbraIdentity)(zimbraPrefIdentityName=## ,+\"\\<>;\u4e2d\u6587---createIdentity ))";
    String escaped = com.unboundid.ldap.sdk.Filter.encodeValue(raw);
    System.out.println(escaped);
    */

    /*
    com.unboundid.ldap.sdk.Filter filter =
        com.unboundid.ldap.sdk.Filter.create("(&(objectclass=zimbraIdentity)(zimbraPrefIdentityName=## ,+\"\\<>;\u4e2d\u6587---createIdentity ))");
    String norm = filter.toNormalizedString();
    System.out.println(norm);
    */
    /*
    String rdn = "cn=foo, bar";
    String norm = com.unboundid.ldap.sdk.RDN.normalize(rdn);
    com.unboundid.ldap.sdk.RDN RDN = new com.unboundid.ldap.sdk.RDN(norm);
    String min = RDN.toMinimallyEncodedString();
    System.out.println(rdn);
    System.out.println(norm);
    System.out.println(min);
    */

  }

  @Test
  public void escapeSearchFilterArg() {
    assertEquals("\\(", LdapUtil.escapeSearchFilterArg("("));
    assertEquals("\\)", LdapUtil.escapeSearchFilterArg(")"));
    assertEquals("\\*", LdapUtil.escapeSearchFilterArg("*"));
    assertEquals("\\\\", LdapUtil.escapeSearchFilterArg("\\"));
  }
}
