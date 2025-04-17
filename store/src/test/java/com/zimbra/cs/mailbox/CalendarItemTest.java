// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.util.AccountUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CalendarItemTest {
  private OperationContext octxt;
  private CalendarItem calItem;
  private final Integer SEQ = 123;

  /**
   * @throws java.lang.Exception
   */
  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    octxt = mock(OperationContext.class);
    final Account account = AccountUtil.createAccount();
    Mailbox mbox =
        MailboxManager.getInstance().getMailboxByAccount(account);
    // have to spy because later real method is called
    calItem = mock(CalendarItem.class, CALLS_REAL_METHODS);
    calItem.mData = new UnderlyingData();
    calItem.mMailbox = mbox;
  }

 /**
  * @throws java.lang.Exception
   */
 @Test
 void testPerformSetPrevFoldersOperation() throws Exception {
  when(calItem.getModifiedSequence()).thenReturn(SEQ);

  calItem.performSetPrevFoldersOperation(octxt);
  // above method adds 2 in mod sequence, so verify SEQ+2 and Mailbox.ID_FOLDER_TRASH

  assertEquals(calItem.mData.getPrevFolders(), (SEQ + 2) + ":" + Mailbox.ID_FOLDER_TRASH);
  assertEquals(calItem.getPrevFolders(), (SEQ + 2) + ":" + Mailbox.ID_FOLDER_TRASH);
  assertEquals(calItem.mData.getPrevFolders(), calItem.getPrevFolders());
 }
}
