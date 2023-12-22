// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.cs.account.Account;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;

public class ImapPathTest {

    private static final String LOCAL_USER = "localimaptest@zimbra.com";
    private Account acct = null;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
        Provisioning prov = Provisioning.getInstance();
        HashMap<String,Object> attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, "12aa345b-2b47-44e6-8cb8-7fdfa18c1a9f");
        acct = prov.createAccount(LOCAL_USER, "secret", attrs);
    }

    @AfterEach
    public void tearDown() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void testWildCardStar() throws Exception {
  ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
  ImapPath i4Path = new ImapPath("*", credentials, ImapPath.Scope.UNPARSED);
  assertNotNull(i4Path, "Should be able to instantiate ImapPath for '*'");
  String owner = i4Path.getOwner();
  assertNull(owner, "owner part of the path should be null. Was " + owner);
  assertTrue(i4Path.belongsTo(credentials), "belongsTo should return TRUE with same credentials as were passed to the constructor");
  assertEquals(acct, i4Path.getOwnerAccount(), "Incorrect owner account");
  assertEquals("\"*\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
 }

 @Test
 void testWildCardPercent() throws Exception {
  ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
  ImapPath i4Path = new ImapPath("%", credentials, ImapPath.Scope.UNPARSED);
  assertNotNull(i4Path, "Should be able to instantiate ImapPath for '*'");
  String owner = i4Path.getOwner();
  assertNull(owner, "owner part of the path should be null. Was " + owner);
  assertTrue(i4Path.belongsTo(credentials), "belongsTo should return TRUE with same credentials as were passed to the constructor");
  assertEquals(acct, i4Path.getOwnerAccount(), "Incorrect owner account");
  assertEquals("\"%\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
 }

 @Test
 void testWildCardPercent2() throws Exception {
  ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
  ImapPath i4Path = new ImapPath("%/%", credentials, ImapPath.Scope.UNPARSED);
  assertNotNull(i4Path, "Should be able to instantiate ImapPath for '*'");
  String owner = i4Path.getOwner();
  assertNull(owner, "owner part of the path should be null. Was " + owner);
  assertTrue(i4Path.belongsTo(credentials), "belongsTo should return TRUE with same credentials as were passed to the constructor");
  assertEquals(acct, i4Path.getOwnerAccount(), "Incorrect owner account");
  assertEquals("\"%/%\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
 }

 @Test
 void testHomeWildCard() throws Exception {
  ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
  ImapPath i4Path = new ImapPath("/home/*", credentials, ImapPath.Scope.UNPARSED);
  assertNotNull(i4Path, "Should be able to instantiate ImapPath for '/home/*'");
  String owner = i4Path.getOwner();
  assertNull(owner, "owner part of the path should be null. Was " + owner);
  assertTrue(i4Path.belongsTo(credentials), "belongsTo should return TRUE with same credentials as were passed to the constructor");
  assertEquals(acct, i4Path.getOwnerAccount(), "Incorrect owner account");
  assertEquals("\"home/*\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
 }

 @Test
 void testHomePercent() throws Exception {
  ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
  ImapPath i4Path = new ImapPath("/home/%", credentials, ImapPath.Scope.UNPARSED);
  assertNotNull(i4Path, "Should be able to instantiate ImapPath for '/home/%'");
  String owner = i4Path.getOwner();
  assertNull(owner, "owner part of the path should be null. Was " + owner);
  assertTrue(i4Path.belongsTo(credentials), "belongsTo should return TRUE with same credentials as were passed to the constructor");
  assertEquals(acct, i4Path.getOwnerAccount(), "Incorrect owner account");
  assertEquals("\"home/%\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
 }

 @Test
 void testHomePercent2() throws Exception {
  ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
  ImapPath i4Path = new ImapPath("/home/%/%", credentials, ImapPath.Scope.UNPARSED);
  assertNotNull(i4Path, "Should be able to instantiate ImapPath for '/home/%/%'");
  String owner = i4Path.getOwner();
  assertNull(owner, "owner part of the path should be null. Was " + owner);
  assertTrue(i4Path.belongsTo(credentials), "belongsTo should return TRUE with same credentials as were passed to the constructor");
  assertEquals(acct, i4Path.getOwnerAccount(), "Incorrect owner account");
  assertEquals("\"home/%/%\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
 }
}
