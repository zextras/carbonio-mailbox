// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.prov.ldap;

import static org.junit.Assert.*;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.type.CacheEntryType;
import java.util.Map;
import org.junit.*;
import qa.unittest.TestUtil;
import qa.unittest.prov.Names;

public class TestLdapProvAlias extends LdapTest {
  private static LdapProvTestUtil provUtil;
  private static Provisioning prov;
  private static Domain domain;

  @BeforeClass
  public static void init() throws Exception {
    provUtil = new LdapProvTestUtil();
    prov = provUtil.getProv();
    domain = provUtil.createDomain(baseDomainName(), null);
  }

  @AfterClass
  public static void cleanup() throws Exception {
    Cleanup.deleteAll(baseDomainName());
  }

  private Account createAccount(String localPart) throws Exception {
    return createAccount(localPart, null);
  }

  private DistributionList createDistributionList(String localpart) throws Exception {
    return createDistributionList(localpart, null);
  }

  private DistributionList createDistributionList(String localPart, Map<String, Object> attrs)
      throws Exception {
    return provUtil.createDistributionList(localPart, domain, attrs);
  }

  private void deleteDistributionList(DistributionList dl) throws Exception {
    provUtil.deleteDistributionList(dl);
  }

  private Account createAccount(String localPart, Map<String, Object> attrs) throws Exception {
    return provUtil.createAccount(localPart, domain, attrs);
  }

  private void deleteAccount(Account acct) throws Exception {
    provUtil.deleteAccount(acct);
  }

  @Test
  public void addAccountAlias() throws Exception {
    String ACCT_NAME_LOCALPART = Names.makeAliasNameLocalPart(genAcctNameLocalPart("acct"));
    Account acct = createAccount(ACCT_NAME_LOCALPART);
    String ACCT_ID = acct.getId();

    String ALIAS_LOCALPART = Names.makeAliasNameLocalPart(genAcctNameLocalPart("alias"));
    String ALIAS_NAME = TestUtil.getAddress(ALIAS_LOCALPART, domain.getName());

    prov.addAlias(acct, ALIAS_NAME);

    prov.flushCache(CacheEntryType.account, null);
    Account acctByAlias = prov.get(AccountBy.name, ALIAS_NAME);

    assertEquals(ACCT_ID, acctByAlias.getId());

    deleteAccount(acctByAlias);

    // get account by alias again
    prov.flushCache(CacheEntryType.account, null);
    acctByAlias = prov.get(AccountBy.name, ALIAS_NAME);
    assertNull(acctByAlias);
  }

  @Test
  public void removeAccountAlias() throws Exception {
    String ACCT_NAME_LOCALPART = Names.makeAccountNameLocalPart(genAcctNameLocalPart("acct"));
    Account acct = createAccount(ACCT_NAME_LOCALPART);
    String ACCT_ID = acct.getId();

    String ALIAS_LOCALPART = Names.makeAliasNameLocalPart(genAcctNameLocalPart("alias"));
    String ALIAS_NAME = TestUtil.getAddress(ALIAS_LOCALPART, domain.getName());

    prov.addAlias(acct, ALIAS_NAME);

    prov.flushCache(CacheEntryType.account, null);
    Account acctByAlias = prov.get(AccountBy.name, ALIAS_NAME);

    assertEquals(ACCT_ID, acctByAlias.getId());

    prov.removeAlias(acct, ALIAS_NAME);

    prov.flushCache(CacheEntryType.account, null);
    acctByAlias = prov.get(AccountBy.name, ALIAS_NAME);

    assertNull(acctByAlias);

    deleteAccount(acct);
  }

  @Test
  public void addDistributionListAlias() throws Exception {
    String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl"));
    DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
    String DL_ID = dl.getId();

    String ALIAS_LOCALPART = Names.makeAliasNameLocalPart(genGroupNameLocalPart("alias"));
    String ALIAS_NAME = TestUtil.getAddress(ALIAS_LOCALPART, domain.getName());

    prov.addAlias(dl, ALIAS_NAME);

    prov.flushCache(CacheEntryType.account, null);
    DistributionList dlByAlias = prov.get(Key.DistributionListBy.name, ALIAS_NAME);

    assertEquals(DL_ID, dlByAlias.getId());

    deleteDistributionList(dlByAlias);

    // get dl by alias again
    prov.flushCache(CacheEntryType.group, null);
    dlByAlias = prov.get(Key.DistributionListBy.name, ALIAS_NAME);
    assertNull(dlByAlias);
  }

  @Test
  public void removeDistributionListAlias() throws Exception {
    String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl"));
    DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
    String DL_ID = dl.getId();

    String ALIAS_LOCALPART = Names.makeAliasNameLocalPart(genGroupNameLocalPart("alias"));
    String ALIAS_NAME = TestUtil.getAddress(ALIAS_LOCALPART, domain.getName());

    prov.addAlias(dl, ALIAS_NAME);

    prov.flushCache(CacheEntryType.group, null);
    DistributionList dlByAlias = prov.get(Key.DistributionListBy.name, ALIAS_NAME);

    assertEquals(DL_ID, dlByAlias.getId());

    prov.removeAlias(dl, ALIAS_NAME);

    prov.flushCache(CacheEntryType.group, null);
    dlByAlias = prov.get(Key.DistributionListBy.name, ALIAS_NAME);

    assertNull(dlByAlias);

    deleteDistributionList(dl);
  }
}
