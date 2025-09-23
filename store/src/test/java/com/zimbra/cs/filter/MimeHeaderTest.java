// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MimeHeaderTest extends MailboxTestSuite {
    private static String sampleMsg = "from: xyz@example.com\n"
            + "Subject: test message\n"
            + "to: foo@example.com, baz@example.com\n"
            + "cc: qux@example.com\n"
            + "Subject: Bonjour\n"
            + "MIME-Version: 1.0\n"
            + "Content-Type: multipart/mixed; boundary=\"----=_Part_64_1822363563.1505482033554\"\n"
            + "\n"
            + "------=_Part_64_1822363563.1505482033554\n"
            + "Content-Type: text/plain; charset=utf-8\n"
            + "Content-Transfer-Encoding: 7bit\n"
            + "\n"
            + "Test message 2\n"
            + "------=_Part_64_1822363563.1505482033554\n"
            + "Content-Type: message/rfc822\n"
            + "Content-Disposition: attachment\n"
            + "\n"
            + "Date: Fri, 15 Sep 2017 22:26:43 +0900 (JST)\n"
            + "From: admin@synacorjapan.com\n"
            + "To: user1 <user1@synacorjapan.com>\n"
            + "Message-ID: <523389747.44.1505482003470.JavaMail.zimbra@synacorjapan.com>\n"
            + "Subject: Hello\n"
            + "MIME-Version: 1.0\n"
            + "Content-Type: multipart/alternative; boundary=\"=_37c6ca38-873e-4a06-ad29-25a254075e83\"\n"
            + "\n"
            + "--=_37c6ca38-873e-4a06-ad29-25a254075e83\n"
            + "Content-Type: text/plain; charset=utf-8\n"
            + "Content-Transfer-Encoding: 7bit\n"
            + "\n"
            + "This is a sample email\n"
            + "\n"
            + "--=_37c6ca38-873e-4a06-ad29-25a254075e83\n"
            + "Content-Type: text/html; charset=utf-8\n"
            + "Content-Transfer-Encoding: 7bit\n"
            + "\n"
            + "<html><body><div style=\"font-family: arial, helvetica, sans-serif; font-size: 12pt; color: #000000\"><div>Test message</div></div></body></html>\n"
            + "--=_37c6ca38-873e-4a06-ad29-25a254075e83--\n"
            + "\n"
            + "------=_Part_64_1822363563.1505482033554--\n";

 @Test
 void test() throws Exception {
  // Default match type :is is used.
  String filterScript = "require [\"tag\", \"comparator-i;ascii-numeric\"];"
    + "if mime_header :comparator \"i;ascii-numeric\" \"Subject\" \"Hello\" {\n"
    + "  tag \"is\";\n"
    + "} else {\n"
    + "  tag \"not is\";\n"
    + "}";
  doTest(filterScript, "is");
 }

 /*
  * The ascii-numeric comparator should be looked up in the list of the "require".
  */
 @Test
 void testMissingComparatorNumericDeclaration() throws Exception {
  // Default match type :is is used.
  // No "comparator-i;ascii-numeric" capability text in the require command
  String filterScript = "require [\"tag\"];"
    + "if mime_header :comparator \"i;ascii-numeric\" \"Subject\" \"Hello\" {\n"
    + "  tag \"is\";\n"
    + "} else {\n"
    + "  tag \"not is\";\n"
    + "}";
  doTest(filterScript, null);
 }

    private void doTest(String filterScript, String expected) throws Exception {
        try {
            Account account = createAccount().create();
            RuleManager.clearCachedRules(account);
            Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

            account.unsetAdminSieveScriptBefore();
            account.unsetMailSieveScript();
            account.unsetAdminSieveScriptAfter();
            account.setMailSieveScript(filterScript);
            List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
                    new OperationContext(mbox), mbox,
                    new ParsedMessage(sampleMsg.getBytes(), false), 0,
                    account.getName(),
                    new DeliveryContext(),
                    Mailbox.ID_FOLDER_INBOX, true);
            assertEquals(1, ids.size());
            Message msg = mbox.getMessageById(null, ids.get(0).getId());
            assertEquals(expected, ArrayUtil.getFirstElement(msg.getTags()));
        } catch (Exception e) {
            fail("No exception should be thrown" + e);
        }
    }
}
