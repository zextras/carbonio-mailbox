// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3

package com.zimbra.cs.mailbox;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import org.mockito.Mockito;

public class CalendarItemTest {
    private OperationContext octxt;
    private CalendarItem calItem;
    private final Integer SEQ = 123;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        octxt = Mockito.mock(OperationContext.class);
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
        // have to spy because later real method is called
        calItem = Mockito.mock(CalendarItem.class, Mockito.CALLS_REAL_METHODS);
        calItem.mData = new UnderlyingData();
        calItem.mMailbox = mbox;
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void testPerformSetPrevFoldersOperation() throws Exception {
        Mockito.when(calItem.getModifiedSequence()).thenReturn(SEQ);

        calItem.performSetPrevFoldersOperation(octxt);
        // above method adds 2 in mod sequence, so verify SEQ+2 and Mailbox.ID_FOLDER_TRASH

        Assert.assertEquals(calItem.mData.getPrevFolders(), (SEQ+2)+":" + Mailbox.ID_FOLDER_TRASH);
        Assert.assertEquals(calItem.getPrevFolders(), (SEQ+2)+":" + Mailbox.ID_FOLDER_TRASH);
        Assert.assertEquals(calItem.mData.getPrevFolders(), calItem.getPrevFolders());
    }
}