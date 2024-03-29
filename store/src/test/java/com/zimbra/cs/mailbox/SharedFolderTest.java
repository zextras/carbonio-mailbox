// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.HashMap;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.common.account.Key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;

public class SharedFolderTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        HashMap<String,Object> attrs;
        attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        prov.createAccount("owner@zimbra.com", "secret", attrs);
        attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        prov.createAccount("grantee1@zimbra.com", "secret", attrs);
        attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        prov.createAccount("grantee2@zimbra.com", "secret", attrs);
        attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        attrs.put(Provisioning.A_zimbraIsExternalVirtualAccount, "TRUE");
        prov.createAccount("virtual_grantee1@zimbra.com", "secret", attrs);
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void adminGrant() throws Exception {
  Account owner, grantee, virtualGrantee;
  Provisioning prov = Provisioning.getInstance();
  owner = prov.get(Key.AccountBy.name, "owner@zimbra.com");
  grantee = prov.get(Key.AccountBy.name, "grantee1@zimbra.com");
  virtualGrantee = prov.get(Key.AccountBy.name, "virtual_grantee1@zimbra.com");

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(owner.getId());

  Folder.FolderOptions fopt = new Folder.FolderOptions();
  fopt.setDefaultView(MailItem.Type.MESSAGE);
  Folder f1 = mbox.createFolder(null, "/Inbox/f1", fopt);

  String exception;

  // allow granting admin right to internal user
  try {
   exception = null;
   mbox.grantAccess(null, f1.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("rwidxa"), null);
  } catch (ServiceException e) {
   exception = e.getCode();
  }
  assertNull(exception, "got wrong exception");

  // don't allow granting admin right to virtual account
  try {
   exception = null;
   mbox.grantAccess(null, f1.getId(), virtualGrantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("rwidxa"), null);
  } catch (ServiceException e) {
   exception = e.getCode();
  }
  assertEquals(MailServiceException.CANNOT_GRANT, exception, "got wrong exception");

  // don't allow granting admin right to public
  try {
   exception = null;
   mbox.grantAccess(null, f1.getId(), null, ACL.GRANTEE_PUBLIC, ACL.stringToRights("rwidxa"), null);
  } catch (ServiceException e) {
   exception = e.getCode();
  }
  assertEquals(MailServiceException.CANNOT_GRANT, exception, "got wrong exception");

  // allow granting non-admin rights to virtual account
  try {
   exception = null;
   mbox.grantAccess(null, f1.getId(), virtualGrantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("rwidx"), null);
  } catch (ServiceException e) {
   exception = e.getCode();
  }
  assertNull(exception, "got wrong exception");

  // allow granting non-admin rights to public
  try {
   exception = null;
   mbox.grantAccess(null, f1.getId(), null, ACL.GRANTEE_PUBLIC, ACL.stringToRights("rwidx"), null);
  } catch (ServiceException e) {
   exception = e.getCode();
  }
  assertNull(exception, "got wrong exception");
 }
}
