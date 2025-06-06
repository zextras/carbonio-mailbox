// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.account.ZAttrProvisioning.MailThreadingAlgorithm;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Threader.ThreadIndex;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.util.JMSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class ThreaderTest {
  private Account account;
    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @BeforeEach
    public void setup() throws Exception {
       MailboxTestUtil.clearData();
       account = Provisioning.getInstance().createAccount(UUID.randomUUID() + "@zimbra.com", "secret", new HashMap<String, Object>());
    }

    private static final String ROOT_SUBJECT = "sdkljfh sdjhfg kjdshkj iu 8 skfjd";
    private static final String ROOT_MESSAGE_ID = "<sakfuslkdhflskjch@sdkf.example.com>";
    private static final String ROOT_THREAD_TOPIC = ROOT_SUBJECT;
    private static final String ROOT_THREAD_INDEX = Threader.ThreadIndex.newThreadIndex();

    private static final String OTHER_SUBJECT = "kjsdfhg sdf sdgf asa aa sadfkjha 345";
    private static final String OTHER_MESSAGE_ID = "<lsdfkjghkds.afas.sdf@sdkf.example.com>";

    private static final String THIRD_MESSAGE_ID = "<dkjhgf.w98yerg.ksj72@sdkf.example.com>";
    private static final String FOURTH_MESSAGE_ID = "<783246tygirufhmnasdb@sdkf.example.com>";
    private static final String FIFTH_MESSAGE_ID = "<kjsdfg.45wy.setrhye.g@sdkf.example.com>";

    private Account getAccount() throws Exception {
        return Provisioning.getInstance().getAccount("test@zimbra.com");
    }

    public static ParsedMessage getRootMessage() throws Exception {
        return new ParsedMessage(getRootMimeMessage(), false);
    }

    static MimeMessage getRootMimeMessage() throws Exception {
        MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
        mm.setHeader("From", "Bob Evans <bob@example.com>");
        mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
        mm.setHeader("Subject", ROOT_SUBJECT);
        mm.setHeader("Message-ID", ROOT_MESSAGE_ID);
        mm.setHeader("Thread-Topic", ROOT_THREAD_TOPIC);
        mm.setHeader("Thread-Index", ROOT_THREAD_INDEX);
        mm.setText("nothing to see here");
        return mm;
    }

    static MimeMessage getSecondMessage() throws Exception {
        MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
        mm.setHeader("From", "Bob Evans <bob@example.com>");
        mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
        mm.setHeader("Message-ID", OTHER_MESSAGE_ID);
        mm.setText("still nothing to see here");
        return mm;
    }

    private void threadMessage(String msg, MailThreadingAlgorithm mode, ParsedMessage pm, Mailbox mbox, List<Integer> expectedMatches) throws Exception {
        mbox.beginTransaction("ThreaderTest", null);
        try {
            account.setMailThreadingAlgorithm(mode);
            Threader threader = new Threader(mbox, pm);
            List<Integer> matches = MailItem.toId(threader.lookupConversation());
            assertEquals(expectedMatches, matches, msg + " (threading: " + mode + ")");
        } finally {
            mbox.endTransaction(false);
        }
    }

 @Test
 void unrelated() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  mbox.addMessage(null, getRootMessage(), MailboxTest.STANDARD_DELIVERY_OPTIONS, null);

  // unrelated, not a reply
  MimeMessage mm = getSecondMessage();
  mm.setHeader("Subject", OTHER_SUBJECT);
  ParsedMessage pm = new ParsedMessage(mm, false);

  threadMessage("unrelated", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated", MailThreadingAlgorithm.subject, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated", MailThreadingAlgorithm.references, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated", MailThreadingAlgorithm.subjrefs, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());

  // unrelated, reply to some other message
  mm = getSecondMessage();
  mm.setHeader("Subject", "Re: " + OTHER_SUBJECT);
  mm.setHeader("In-Reply-To", THIRD_MESSAGE_ID);
  mm.setHeader("References", THIRD_MESSAGE_ID);
  pm = new ParsedMessage(mm, false);

  threadMessage("unrelated reply", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated reply", MailThreadingAlgorithm.subject, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated reply", MailThreadingAlgorithm.references, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated reply", MailThreadingAlgorithm.subjrefs, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated reply", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());
 }

 @Test
 void followup() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  Message msg = mbox.addMessage(null, getRootMessage(), MailboxTest.STANDARD_DELIVERY_OPTIONS, null);
  List<Integer> match = Arrays.asList(msg.getConversationId());

  // References and In-Reply-To set
  MimeMessage mm = getSecondMessage();
  mm.setHeader("Subject", "Re: " + ROOT_SUBJECT);
  mm.setHeader("In-Reply-To", ROOT_MESSAGE_ID);
  mm.setHeader("References", ROOT_MESSAGE_ID);
  ParsedMessage pm = new ParsedMessage(mm, false);

  threadMessage("followup", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("followup", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("followup", MailThreadingAlgorithm.references, pm, mbox, match);
  threadMessage("followup", MailThreadingAlgorithm.subjrefs, pm, mbox, match);
  threadMessage("followup", MailThreadingAlgorithm.strict, pm, mbox, match);

  // only In-Reply-To set
  mm = getSecondMessage();
  mm.setHeader("Subject", "Re: " + ROOT_SUBJECT);
  mm.setHeader("In-Reply-To", ROOT_MESSAGE_ID);
  pm = new ParsedMessage(mm, false);

  threadMessage("followup [irt]", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("followup [irt]", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("followup [irt]", MailThreadingAlgorithm.references, pm, mbox, match);
  threadMessage("followup [irt]", MailThreadingAlgorithm.subjrefs, pm, mbox, match);
  threadMessage("followup [irt]", MailThreadingAlgorithm.strict, pm, mbox, match);

  // only References set
  mm = getSecondMessage();
  mm.setHeader("Subject", "Re: " + ROOT_SUBJECT);
  mm.setHeader("References", ROOT_MESSAGE_ID);
  pm = new ParsedMessage(mm, false);

  threadMessage("followup [refs]", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("followup [refs]", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("followup [refs]", MailThreadingAlgorithm.references, pm, mbox, match);
  threadMessage("followup [refs]", MailThreadingAlgorithm.subjrefs, pm, mbox, match);
  threadMessage("followup [refs]", MailThreadingAlgorithm.strict, pm, mbox, match);
 }

 @Test
 void missingHeaders() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  Message msg = mbox.addMessage(null, getRootMessage(), MailboxTest.STANDARD_DELIVERY_OPTIONS, null);
  List<Integer> match = Arrays.asList(msg.getConversationId());

  // reply without any of the threading headers
  MimeMessage mm = getSecondMessage();
  mm.setHeader("Subject", "Re: " + ROOT_SUBJECT);
  ParsedMessage pm = new ParsedMessage(mm, false);

  threadMessage("followup [nohdr]", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("followup [nohdr]", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("followup [nohdr]", MailThreadingAlgorithm.references, pm, mbox, match);
  threadMessage("followup [nohdr]", MailThreadingAlgorithm.subjrefs, pm, mbox, match);
  threadMessage("followup [nohdr]", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());
 }

 @Test
 void nonreply() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  Message msg = mbox.addMessage(null, getRootMessage(), MailboxTest.STANDARD_DELIVERY_OPTIONS, null);
  List<Integer> match = Arrays.asList(msg.getConversationId());

  // not a reply, but matching Subject
  MimeMessage mm = getSecondMessage();
  mm.setHeader("Subject", ROOT_SUBJECT);
  ParsedMessage pm = new ParsedMessage(mm, false);

  threadMessage("matching subject", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("matching subject", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("matching subject", MailThreadingAlgorithm.references, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("matching subject", MailThreadingAlgorithm.subjrefs, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("matching subject", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());
 }

 @Test
 void changedSubject() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  Message msg = mbox.addMessage(null, getRootMessage(), MailboxTest.STANDARD_DELIVERY_OPTIONS, null);
  List<Integer> match = Arrays.asList(msg.getConversationId());

  // reply with different Subject
  MimeMessage mm = getSecondMessage();
  mm.setHeader("Subject", OTHER_SUBJECT);
  mm.setHeader("In-Reply-To", ROOT_MESSAGE_ID);
  mm.setHeader("References", ROOT_MESSAGE_ID);
  ParsedMessage pm = new ParsedMessage(mm, false);

  threadMessage("changed subject", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("changed subject", MailThreadingAlgorithm.subject, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("changed subject", MailThreadingAlgorithm.references, pm, mbox, match);
  threadMessage("changed subject", MailThreadingAlgorithm.subjrefs, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("changed subject", MailThreadingAlgorithm.strict, pm, mbox, match);
 }

 @Test
 void crossedThread() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  Message msg = mbox.addMessage(null, getRootMessage(), MailboxTest.STANDARD_DELIVERY_OPTIONS, null);
  List<Integer> match = Arrays.asList(msg.getConversationId());

  // reply with the same normalized subject, but not the same thread as the original message
  MimeMessage mm = getSecondMessage();
  mm.setHeader("Subject", "Re: " + ROOT_SUBJECT);
  mm.setHeader("In-Reply-To", THIRD_MESSAGE_ID);
  mm.setHeader("References", THIRD_MESSAGE_ID);
  ParsedMessage pm = new ParsedMessage(mm, false);

  threadMessage("crossed threads", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("crossed threads", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("crossed threads", MailThreadingAlgorithm.references, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("crossed threads", MailThreadingAlgorithm.subjrefs, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("crossed threads", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());
 }

 @Test
 void outlook() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  Message msg = mbox.addMessage(null, getRootMessage(), MailboxTest.STANDARD_DELIVERY_OPTIONS, null);
  List<Integer> match = Arrays.asList(msg.getConversationId());

  // reply from Outlook (no In-Reply-To or References header)
  MimeMessage mm = getSecondMessage();
  mm.setHeader("Subject", "Re: " + ROOT_SUBJECT);
  mm.setHeader("Thread-Topic", ROOT_THREAD_TOPIC);
  mm.setHeader("Thread-Index", Threader.ThreadIndex.addChild(Threader.ThreadIndex.parseHeader(ROOT_THREAD_INDEX)));
  ParsedMessage pm = new ParsedMessage(mm, false);

  threadMessage("outlook", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("outlook", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("outlook", MailThreadingAlgorithm.references, pm, mbox, match);
  threadMessage("outlook", MailThreadingAlgorithm.subjrefs, pm, mbox, match);
  threadMessage("outlook", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());
 }

 @Test
 void threadIndex() throws Exception {
  assertEquals(32, ThreadIndex.newThreadIndex().length(), "new thread index length");

  byte[] oldIndex = new byte[82];
  new Random().nextBytes(oldIndex);
  String newIndex = ThreadIndex.addChild(oldIndex);
  assertEquals(116, newIndex.length(), "child index length");
  byte[] head = new byte[oldIndex.length];
  System.arraycopy(ThreadIndex.parseHeader(newIndex), 0, head, 0, oldIndex.length);
  assertArrayEquals(oldIndex, head, "preserving old index");
 }

    private void checkConversations(Mailbox mbox, int msgidA, int msgidB, boolean match) throws ServiceException {
        Message msgA = mbox.getMessageById(null, msgidA);
        Message msgB = mbox.getMessageById(null, msgidB);
        if (match) {
         assertEquals(msgA.getConversationId(), msgB.getConversationId(), "in same conversation");
        } else {
         assertNotNull(msgB.getConversationId(), "in different conversations");
        }
    }

 @Test
 void redelivery() throws Exception {
  account.setMailThreadingAlgorithm(MailThreadingAlgorithm.references);

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

  // add thread starter
  int msgid1 = mbox.addMessage(null, getRootMessage(), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();

  // add second message, which should get slotted with the first message
  MimeMessage mm = getSecondMessage();
  mm.setHeader("Subject", "Re: " + ROOT_SUBJECT);
  mm.setHeader("In-Reply-To", ROOT_MESSAGE_ID);
  int msgid2 = mbox.addMessage(null, new ParsedMessage(mm, false), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();

  checkConversations(mbox, msgid1, msgid2, true);

  // add fourth message
  mm.setHeader("Message-ID", FOURTH_MESSAGE_ID);
  mm.setHeader("In-Reply-To", THIRD_MESSAGE_ID);
  int msgid4 = mbox.addMessage(null, new ParsedMessage(mm, false), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();

  // add fifth message, which should get slotted with the fourth message
  mm.setHeader("Message-ID", FIFTH_MESSAGE_ID);
  mm.setHeader("In-Reply-To", FOURTH_MESSAGE_ID);
  int msgid5 = mbox.addMessage(null, new ParsedMessage(mm, false), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();

  checkConversations(mbox, msgid4, msgid5, true);
  checkConversations(mbox, msgid1, msgid5, false);

  // add third message, joining the two conversations
  mm.setHeader("Message-ID", THIRD_MESSAGE_ID);
  mm.setHeader("In-Reply-To", OTHER_MESSAGE_ID);
  ParsedMessage pm = new ParsedMessage(mm, false);
  int msgid3 = mbox.addMessage(null, pm, MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();

  checkConversations(mbox, msgid3, msgid5, true);
  checkConversations(mbox, msgid3, msgid1, true);

  // redeliver the same message to the mailbox (e.g. two different FILEINTO filters)
  int msgid3A = mbox.addMessage(null, pm, MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();

  checkConversations(mbox, msgid3A, msgid3, true);
 }

 @Test
 void bogusThreadIndexHeader() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());

  MimeMessage mm = getRootMimeMessage();
  mm.setHeader("Thread-Index", Threader.IGNORE_THREAD_INDEX);

  Message msg = mbox.addMessage(null, new ParsedMessage(mm, false), MailboxTest.STANDARD_DELIVERY_OPTIONS, null);
  List<Integer> match = Arrays.asList(msg.getConversationId());

  // unrelated, not a reply
  mm = getSecondMessage();
  mm.setHeader("Subject", OTHER_SUBJECT);
  mm.setHeader("Thread-Index", Threader.IGNORE_THREAD_INDEX);
  ParsedMessage pm = new ParsedMessage(mm, false);

  threadMessage("unrelated bogus thread index", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated bogus thread index", MailThreadingAlgorithm.subject, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated bogus thread index", MailThreadingAlgorithm.references, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated bogus thread index", MailThreadingAlgorithm.subjrefs, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("unrelated bogus thread index", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());

  //same subject, but don't match on references
  mm = getSecondMessage();
  mm.setHeader("Subject", ROOT_SUBJECT);
  mm.setHeader("Thread-Index", Threader.IGNORE_THREAD_INDEX);
  pm = new ParsedMessage(mm, false);

  //only subject algorithm should match; others should not
  threadMessage("same subject bogus thread index", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("same subject bogus thread index", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("same subject bogus thread index", MailThreadingAlgorithm.references, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("same subject bogus thread index", MailThreadingAlgorithm.subjrefs, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("same subject bogus thread index", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());

  //reply
  mm = getSecondMessage();
  mm.setHeader("Subject", "RE: " + ROOT_SUBJECT);
  mm.setHeader("Thread-Index", Threader.IGNORE_THREAD_INDEX);
  pm = new ParsedMessage(mm, false);

  //all should match except strict
  threadMessage("reply bogus thread index", MailThreadingAlgorithm.none, pm, mbox, Collections.<Integer>emptyList());
  threadMessage("reply bogus thread index", MailThreadingAlgorithm.subject, pm, mbox, match);
  threadMessage("reply bogus thread index", MailThreadingAlgorithm.references, pm, mbox, match);
  threadMessage("reply bogus thread index", MailThreadingAlgorithm.subjrefs, pm, mbox, match);
  threadMessage("reply bogus thread index", MailThreadingAlgorithm.strict, pm, mbox, Collections.<Integer>emptyList());

 }

}
