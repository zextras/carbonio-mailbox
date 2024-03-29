// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.cs.mailbox.MailItem.Type;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.common.account.Key;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;

public class ACLTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        HashMap<String,Object> attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, "17dd075e-2b47-44e6-8cb8-7fdfa18c1a9f");
        prov.createAccount("owner@zimbra.com", "secret", attrs);
        attrs = new HashMap<String,Object>();
        attrs.put(Provisioning.A_zimbraId, "a4e41fbe-9c3e-4ab5-8b34-c42f17e251cd");
        prov.createAccount("principal@zimbra.com", "secret", attrs);
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void testRegrant() throws Exception {
  Account owner = Provisioning.getInstance().get(Key.AccountBy.name, "owner@zimbra.com");
  Account grantee = Provisioning.getInstance().get(Key.AccountBy.name, "principal@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

  Folder folder = mbox.createFolder(null, "shared", new Folder.FolderOptions().setDefaultView(Type.FOLDER));
  OperationContext octxt = new OperationContext(owner);
  mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
  try {
   mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
   fail("regrant has succeeded");
  } catch (ServiceException se) {
   if (!se.getCode().equals(MailServiceException.GRANT_EXISTS)) {
    fail("regrant throws ServiceException with code " + se.getCode());
   }
  }
 }

 @Test
 void testRegrantDifferentPermission() throws Exception {
  Account owner = Provisioning.getInstance().get(Key.AccountBy.name, "owner@zimbra.com");
  Account grantee = Provisioning.getInstance().get(Key.AccountBy.name, "principal@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

  Folder folder = mbox.createFolder(null, "shared", new Folder.FolderOptions().setDefaultView(Type.FOLDER));
  OperationContext octxt = new OperationContext(owner);
  mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
  try {
   mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("rw"), null);
  } catch (ServiceException se) {
   if (!se.getCode().equals(MailServiceException.GRANT_EXISTS)) {
    fail("regrant throws ServiceException with code " + se.getCode());
   }
   fail("regrant has failed");
  }
 }

 @Test
 void testPublicAccess() throws Exception {
  Account owner = Provisioning.getInstance().get(Key.AccountBy.name, "owner@zimbra.com");
  owner.setExternalSharingEnabled(false);
  Account guestUser = GuestAccount.ANONYMOUS_ACCT;

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

  Folder folder = mbox.createFolder(null, "sharedCalender", new Folder.FolderOptions().setDefaultView(MailItem.Type.APPOINTMENT));
  OperationContext octxt = new OperationContext(owner);
  mbox.grantAccess(octxt, folder.getId(), guestUser.getId(), ACL.GRANTEE_PUBLIC, ACL.stringToRights("r"), null);

  UnderlyingData underlyingData = new UnderlyingData();
  underlyingData.setSubject("test subject");
  underlyingData.folderId = folder.getId();
  underlyingData.name = "name";
  underlyingData.type = MailItem.Type.APPOINTMENT.toByte();
  underlyingData.uuid = owner.getUid();
  underlyingData.parentId = folder.getId();
  underlyingData.setBlobDigest("test digest");

  CalendarItem calendarItem = new Appointment(mbox, underlyingData, true);
  assertTrue(calendarItem.canAccess(ACL.RIGHT_READ, guestUser, false));
 }
}
