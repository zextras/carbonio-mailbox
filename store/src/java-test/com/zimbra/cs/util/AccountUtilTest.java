package com.zimbra.cs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.Map;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AccountUtilTest {
  static final String GLOBAL_TEST_ACCOUNT_NAME = "gloabaltestaccount@zextras.com";
  Provisioning prov;

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    MailboxTestUtil.clearData();
    prov = Provisioning.getInstance();
    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraMailCanonicalAddress, "canonicaltest123@zextras.com");
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createDomain("zextras.com", attrs);
    prov.createAccount(GLOBAL_TEST_ACCOUNT_NAME, "secret", attrs);
  }

  @Test
  public void shouldGetCanonicalAddressWhenSet() throws ServiceException {

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraMailCanonicalAddress, "canonicaltest321@zextras.com");
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("test321@zextras.com", "secret", attrs);

    Account account = prov.get(Key.AccountBy.name, "test321@zextras.com");
    assertEquals("canonicaltest321@zextras.com", AccountUtil.getCanonicalAddress(account));
  }

  @Test
  public void shouldGetCanonicalAddressWhenNotSet() throws ServiceException {

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("test321@zextras.com", "secret", attrs);

    Account account = prov.get(Key.AccountBy.name, "test321@zextras.com");
    assertEquals("test321@zextras.com", AccountUtil.getCanonicalAddress(account));
  }

  @Test
  public void shouldGetSoapUriWhenCalled() throws ServiceException {

    Account account = prov.get(Key.AccountBy.name, GLOBAL_TEST_ACCOUNT_NAME);
    Server server = prov.getServer(account);
    server.setServiceHostname("demo.zextras.com");
    server.setMailPort(80);
    assertEquals("http://demo.zextras.com:80/service/soap/", AccountUtil.getSoapUri(account));
  }

  @Test
  public void shouldReturnBooleanWhenIsGalAccountCalled() throws ServiceException {

    Account account = prov.get(Key.AccountBy.name, GLOBAL_TEST_ACCOUNT_NAME);
    Domain domain = prov.getDomain(account);

    // galAccountId is unset in domain; isGalSyncAccount should return false
    domain.unsetGalAccountId();
    assertFalse(AccountUtil.isGalSyncAccount(account));

    // galAccountId is set in domain; isGalSyncAccount should return true
    domain.setGalAccountId(new String[] {account.getId()});
    assertTrue(AccountUtil.isGalSyncAccount(account));
  }

  @Test
  public void shouldReturnFalseWhenDomainIsExternalAndAddressHasInternalDomainCalled()
      throws ServiceException {

    Map<String, Object> attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("kiraplsignh@gmail.com", "secret", attrs);

    Account account = prov.get(Key.AccountBy.name, "kiraplsignh@gmail.com");
    assertFalse(AccountUtil.addressHasInternalDomain(account.getName()));
  }

  @Test
  public void shouldReturnTrueWhenDomainIsExternalAndAddressHasInternalDomainCalled()
      throws ServiceException {

    Account account = prov.get(Key.AccountBy.name, GLOBAL_TEST_ACCOUNT_NAME);
    assertTrue(AccountUtil.addressHasInternalDomain(account.getName()));
  }

  @After
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
