package com.zimbra.cs.util;

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountUtilTest {
  static final String GLOBAL_TEST_ACCOUNT_NAME = "gloabaltestaccount@zextras.com";
  Provisioning prov;

  @BeforeEach
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
 void shouldGetCanonicalAddressWhenSet() throws ServiceException {

  Map<String, Object> attrs = Maps.newHashMap();
  attrs.put(Provisioning.A_zimbraMailCanonicalAddress, "canonicaltest321@zextras.com");
  attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  prov.createAccount("test321@zextras.com", "secret", attrs);

  Account account = prov.get(Key.AccountBy.name, "test321@zextras.com");
  assertEquals("canonicaltest321@zextras.com", AccountUtil.getCanonicalAddress(account));
 }

 @Test
 void shouldGetCanonicalAddressWhenNotSet() throws ServiceException {

  Map<String, Object> attrs = Maps.newHashMap();
  attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  prov.createAccount("test321@zextras.com", "secret", attrs);

  Account account = prov.get(Key.AccountBy.name, "test321@zextras.com");
  assertEquals("test321@zextras.com", AccountUtil.getCanonicalAddress(account));
 }

 @Test
 void shouldGetSoapUriWhenCalled() throws ServiceException {

  Account account = prov.get(Key.AccountBy.name, GLOBAL_TEST_ACCOUNT_NAME);
  Server server = prov.getServer(account);
  server.setServiceHostname("demo.zextras.com");
  server.setMailPort(80);
  assertEquals("http://demo.zextras.com:80/service/soap/", AccountUtil.getSoapUri(account));
 }

 @Test
 void shouldReturnBooleanWhenIsGalAccountCalled() throws ServiceException {

  Account account = prov.get(Key.AccountBy.name, GLOBAL_TEST_ACCOUNT_NAME);
  Domain domain = prov.getDomain(account);

  // galAccountId is unset in domain; isGalSyncAccount should return false
  domain.unsetGalAccountId();
  assertFalse(AccountUtil.isGalSyncAccount(account));

  // galAccountId is set in domain; isGalSyncAccount should return true
  domain.setGalAccountId(new String[]{account.getId()});
  assertTrue(AccountUtil.isGalSyncAccount(account));
 }

 @Test
 void shouldReturnFalseWhenDomainIsExternalAndAddressHasInternalDomainCalled()
   throws ServiceException {

  Map<String, Object> attrs = Maps.newHashMap();
  attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  prov.createAccount("kiraplsignh@gmail.com", "secret", attrs);

  Account account = prov.get(Key.AccountBy.name, "kiraplsignh@gmail.com");
  assertFalse(AccountUtil.addressHasInternalDomain(account.getName()));
 }

 @Test
 void shouldReturnTrueWhenDomainIsExternalAndAddressHasInternalDomainCalled()
   throws ServiceException {

  Account account = prov.get(Key.AccountBy.name, GLOBAL_TEST_ACCOUNT_NAME);
  assertTrue(AccountUtil.addressHasInternalDomain(account.getName()));
 }

  @AfterEach
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
