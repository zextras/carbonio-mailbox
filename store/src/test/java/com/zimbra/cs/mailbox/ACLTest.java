// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;

public class ACLTest {

    @BeforeClass
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

    @Before
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

    @Test
    public void testRegrant() throws Exception {
        Account owner = Provisioning.getInstance().get(Key.AccountBy.name, "owner@zimbra.com");
        Account grantee = Provisioning.getInstance().get(Key.AccountBy.name, "principal@zimbra.com");
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

        Folder folder = mbox.createFolder(null, "shared", new Folder.FolderOptions().setDefaultView(MailItem.Type.DOCUMENT));
        OperationContext octxt = new OperationContext(owner);
        mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
        try {
            mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
            Assert.fail("regrant has succeeded");
        } catch (ServiceException se) {
            if (!se.getCode().equals(MailServiceException.GRANT_EXISTS)) {
                Assert.fail("regrant throws ServiceException with code "+se.getCode());
            }
        }
    }

    @Test
    public void testRegrantDifferentPermission() throws Exception {
        Account owner = Provisioning.getInstance().get(Key.AccountBy.name, "owner@zimbra.com");
        Account grantee = Provisioning.getInstance().get(Key.AccountBy.name, "principal@zimbra.com");
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

        Folder folder = mbox.createFolder(null, "shared", new Folder.FolderOptions().setDefaultView(MailItem.Type.DOCUMENT));
        OperationContext octxt = new OperationContext(owner);
        mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
        try {
            mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER, ACL.stringToRights("rw"), null);
        } catch (ServiceException se) {
            if (!se.getCode().equals(MailServiceException.GRANT_EXISTS)) {
                Assert.fail("regrant throws ServiceException with code "+se.getCode());
            }
            Assert.fail("regrant has failed");
        }
    }
    
    @Test
	public void testPublicAccess() throws Exception {
		Account owner = Provisioning.getInstance().get(Key.AccountBy.name,"owner@zimbra.com");
		owner.setExternalSharingEnabled(false);
		Account guestUser = GuestAccount.ANONYMOUS_ACCT;

		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

		Folder folder = mbox.createFolder(null, "sharedCalender",new Folder.FolderOptions().setDefaultView(MailItem.Type.APPOINTMENT));
		OperationContext octxt = new OperationContext(owner);
		mbox.grantAccess(octxt, folder.getId(), guestUser.getId(),ACL.GRANTEE_PUBLIC, ACL.stringToRights("r"), null);

		UnderlyingData underlyingData = new UnderlyingData();
		underlyingData.setSubject("test subject");
		underlyingData.folderId = folder.getId();
		underlyingData.name = "name";
		underlyingData.type = MailItem.Type.APPOINTMENT.toByte();
		underlyingData.uuid = owner.getUid();
		underlyingData.parentId = folder.getId();
		underlyingData.setBlobDigest("test digest");

		CalendarItem calendarItem = new Appointment(mbox, underlyingData, true);
		Assert.assertTrue(calendarItem.canAccess(ACL.RIGHT_READ, guestUser,false));
	}
}
