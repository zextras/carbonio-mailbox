// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.RuleManager;
import com.zimbra.cs.lmtpserver.LmtpAddress;
import com.zimbra.cs.lmtpserver.LmtpEnvelope;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemId;

public class RelationalExtensionTest {
    private static String sampleMsg = "Received: from edge01e.zimbra.com ([127.0.0.1])\n"
            + "\tby localhost (edge01e.zimbra.com [127.0.0.1]) (amavisd-new, port 10032)\n"
            + "\twith ESMTP id DN6rfD1RkHD7; Fri, 24 Jun 2016 01:45:31 -0400 (EDT)\n"
            + "Received: from localhost (localhost [127.0.0.1])\n"
            + "\tby edge01e.zimbra.com (Postfix) with ESMTP id 9245B13575C;\n"
            + "\tFri, 24 Jun 2016 01:45:31 -0400 (EDT)\n"
            + "x-priority: 1\n"
            + "X-Spam-score: -5\n"
            + "from: xyz@example.com\n"
            + "Subject: =?ISO-2022-JP?B?GyRCJDMkcyRLJEEkTxsoQg==?=\n"
            + "to: foo@example.com, baz@example.com\n"
            + "cc: qux@example.com\n";

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret",
                new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void testCountAddressNumericGE1() {
  // RFC 5231 6. Example first example would evaluate to true
  String filterScript = "require [\"relational\", \"comparator-i;ascii-numeric\"];"
    + "if address :count \"ge\" :comparator \"i;ascii-numeric\" "
    + "[\"to\", \"cc\"] [\"3\"] { tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testCountAddressNumericGE2() {
  // RFC 5231 6. Example second example would evaluate to false
  String filterScript = "require [\"relational\", \"tag\", \"comparator-i;ascii-numeric\"];\n"
    + "if anyof (address :count \"ge\" :comparator \"i;ascii-numeric\"\n"
    + "[\"to\"] [\"3\"],\n"
    + "address :count \"ge\" :comparator \"i;ascii-numeric\"\n"
    + "[\"cc\"] [\"3\"] )\n"
    + "{ tag \"Priority\";} else { tag \"No Priority\";}";
  doTest(filterScript, "No Priority");
 }

 @Test
 void testCountHeaderNumericGE1() {
  // RFC 5231 6. Example third example would evaluate to false
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :count \"ge\" :comparator \"i;ascii-numeric\" "
    + "[\"received\"] [\"3\"] { tag \"Priority\";} else { tag \"No Priority\";}";
  doTest(filterScript, "No Priority");
 }

 @Test
 void testCountHeaderNumericGE2() {
  // RFC 5231 6. Example fourth example would evaluate to true
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :count \"ge\" :comparator \"i;ascii-numeric\" "
    + "[\"received\", \"subject\"] [\"3\"] { tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testCountHeaderNumericGE3() {
  // RFC 5231 6. Example fifth example would evaluate to false
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :count \"ge\" :comparator \"i;ascii-numeric\" "
    + "[\"to\", \"cc\"] [\"3\"] { tag \"Priority\";} else { tag \"No Priority\";}";
  doTest(filterScript, "No Priority");
 }

 @Test
 void testValueAddressNumericGT() {
  // invalid comparison
  // RFC 4790 Section 9.1.1.  ASCII Numeric Collation Description
  // | ... Before converting from string to integer, the input
  // | string is truncated at the first non-digit character. All input is
  // | valid; strings that do not start with a digit represent positive
  // | infinity.
  // So the email address string of the To address (foo@example.com, baz@example.com)
  // will be treated as an empty string "", and it represents positive infinity.
  // Positive infinity is definitely grater than 1, so this test should return TRUE.
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if address :value \"gt\" :comparator \"i;ascii-numeric\" "
    + "[\"to\"] [\"0\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testValueAddressCasemapGT() {
  // "from" address starts with 'N'-'Z'
  String filterScript = "require [\"relational\", \"tag\", \"comparator-i;ascii-numeric\"];\n"
    + "if address :value \"gt\" :comparator \"i;ascii-casemap\" "
    + "[\"from\"] [\"M\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testValueHeaderNumericGT() {
  // RFC 5231 7. Extended Example (modified)
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :value \"gt\" :comparator \"i;ascii-numeric\" "
    + "[\"x-priority\"] [\"3\"] { tag \"Priority\";} else { tag \"No Priority\";}";
  doTest(filterScript, "No Priority");
 }

 @Test
 void testValueHeadeNumericrLT() {
  // RFC 5231 7. Extended Example
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :value \"lt\" :comparator \"i;ascii-numeric\" "
    + "[\"x-priority\"] [\"3\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testValueHeaderNumericLE() {
  // RFC 5231 7. Extended Example (modified)
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :value \"le\" :comparator \"i;ascii-numeric\" "
    + "[\"x-priority\"] [\"3\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testValueHeaderNumericEQ() {
  // RFC 5231 7. Extended Example (modified)
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :value \"eq\" :comparator \"i;ascii-numeric\" "
    + "[\"x-priority\"] [\"1\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testValueHeaderNumericNE() {
  // RFC 5231 7. Extended Example (modified)
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :value \"ne\" :comparator \"i;ascii-numeric\" "
    + "[\"x-priority\"] [\"1\"] { tag \"Priority\";} else { tag \"No Priority\";}";
  doTest(filterScript, "No Priority");
 }

 @Test
 void testValueHeaderCasemapGT() {
  // RFC 5231 7. Extended Example (modified)
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\"];\n"
    + "if header :value \"gt\" :comparator \"i;ascii-casemap\" "
    + "[\"from\"] [\"M\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testValueHeaderCasemapI18NGT() {
  // RFC 5231 7. Extended Example (modified)
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\"];\n"
    + "if header :value \"gt\" :comparator \"i;ascii-casemap\" "
    + "[\"subject\"] [\"か\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testBadFormat_nokeys() {
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\"];\n"
    + "if header :value \"gt\" :comparator \"i;ascii-casemap\" "
    + "[\"subject\"] :foo { tag \"Priority\";} else { tag \"No Priority\";}";
  /*
   * The following error will occur:
   * org.apache.jsieve.exception.SyntaxException: Expecting a StringList of keys Line 2 column 1.
    */
  doTest(filterScript, null);
 }

 @Test
 void testBadFormat_noTestName() {
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\"];\n"
    + "if relational :value \"gt\" :comparator \"i;ascii-casemap\" "
    + "[\"subject\"] [\"test\"] { tag \"Priority\";} else { tag \"No Priority\";}";
  /*
   * The following error will occur:
   * org.apache.jsieve.exception.SyntaxException: Found unexpected arguments. Line 2 column 1.
    */
  doTest(filterScript, null);
 }

 @Test
 void testCountEnvelopeToNumericEQ() {
  // RFC 5231 10. Security Considerations
  // An implementation MUST ensure that the test for envelope "to" only
  // reflects the delivery to the current user.
  String filterScript = "require [\"envelope\", \"relational\", \"comparator-i;ascii-numeric\"];"
    + "if envelope :count \"eq\" :comparator \"i;ascii-numeric\" "
    + "[\"TO\"] [\"1\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testCountEnvelopeToNumericGT() {
  // RFC 5231 10. Security Considerations
  // An implementation MUST ensure that the test for envelope "to" only
  // reflects the delivery to the current user.  Using this test, it MUST
  // not be possible for a user to determine if this message was delivered
  // to someone else.
  // The number of sample LMTP RCPT TO addresses is 2, but the number of
  // address to be evaluated for the "envelope" test should be 1.
  String filterScript = "require [\"envelope\", \"relational\", \"comparator-i;ascii-numeric\"];"
    + "if envelope :count \"gt\" :comparator \"i;ascii-numeric\" "
    + "[\"to\"] [\"1\"] { tag \"Priority\";} else { tag \"No Priority\";}";
  doTest(filterScript, "No Priority");
 }

 @Test
 void testValueEnvelopeFromNumericGT() {
  // invalid comparison
  // See the comment on testValueAddressNumericGT.
  String filterScript = "require [\"envelope\", \"relational\", \"comparator-i;ascii-numeric\"];"
    + "if envelope :value \"gt\" :comparator \"i;ascii-numeric\" "
    + "[\"from\"] [\"1\"] {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testValueEnvelopeCasemapGT() {
  // LMTP MAIL FROM envelope (<abc@zimbra.com>) does not start 'N'-'Z'
  String filterScript = "require [\"envelope\", \"relational\"];"
    + "if envelope :value \"gt\" :comparator \"i;ascii-casemap\" "
    + "[\"from\"] [\"M\"] { tag \"Priority\";} else { tag \"No Priority\";}";
  doTest(filterScript, "No Priority");
 }

 @Test
 void testValueEnvelopeCasemap() {
  // LMTP MAIL FROM envelope (<abc@zimbra.com>) matchs the all upper case string
  // RFC 5228 Section 2.7.3. "i;ascii-casemap" comparator which treats uppercase and lowercase
  //   characters in the US-ASCII subset of UTF-8 as the same
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"envelope\"];\n"
    + "if envelope :value \"eq\" :comparator \"i;ascii-casemap\" "
    + "[\"from\"] \"ABC@ZIMBRA.COM\" {" + "tag \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 @Test
 void testValueEnvelopeOctet() {
  // LMTP MAIL FROM envelope (<abc@zimbra.com>) does not match the all upper case string
  // RFC 5228 Section 2.7.3. i;octet comparator simply compares octets
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"envelope\"];\n"
    + "if envelope :value \"eq\" :comparator \"i;octet\" "
    + "[\"from\"] \"ABC@ZIMBRA.COM\" { tag \"Priority\";} else { tag \"No Priority\";}";
  doTest(filterScript, "No Priority");
 }

 @Test
 void testBadFormat_invalidEnvHeaderName() {
  String filterScript = "require [\"envelope\", \"relational\"];"
    + "if envelope :value \"gt\" :comparator \"i;ascii-casemap\" "
    + "[\"AUTH\"] [\"M\"] {" + "tag \"Priority\";} else { tag \"No Priority\";}";
  // The following error will occur:
  // org.apache.jsieve.exception.SyntaxException: Unexpected header name as a value for <envelope-part>: 'AUTH'
  doTest(filterScript, null);
 }

 @Test
 void testValueEnvelopeFromNumeric_AllUpperCase() {
  String filterScript = "REQUIRE [\"envelope\", \"relational\", \"tag\", \"comparator-i;ascii-numeric\"];\n"
    + "IF ENVELOPE :COUNT \"EQ\" :COMPARATOR \"I;ASCII-NUMERIC\" "
    + "[\"TO\"] [\"1\"] {" + "TAG \"Priority\";}";
  doTest(filterScript, "Priority");
 }

 // Due to the negative value test, the filter execution is cancelled;
 // and none of tag commands should be executed.
 @Test
 void testValueHeaderNumericNegativeValue() {
  String filterScript = "require [\"tag\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :value \"lt\" :comparator \"i;ascii-numeric\" "
    + "[\"x-priority\"] [\"-1\"] { tag \"Priority\"; } else { tag \"No Priority\";}"
    + "tag \"Negative\";";
  doTest(filterScript, null);
 }

 // Due to the negative value test, the filter execution is cancelled;
 // and none of tag commands should be executed.
 @Test
 void testValueAddressNumericNegativeValue() {
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if address :value \"lt\" :comparator \"i;ascii-numeric\" "
    + "[\"to\"] [\"-1\"] { tag \"to\"; } else { tag \"No To\";}"
    + "tag \"Negative\";";
  doTest(filterScript, null);
 }

 // Due to the negative value test, the filter execution is cancelled;
 // and none of tag commands should be executed.
 @Test
 void testValueEnvelopeFromNumericNegativeValue() {
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if envelope :value \"lt\" :comparator \"i;ascii-numeric\" "
    + "[\"from\"] [\"-1\"] { tag \"from\"; } else { tag \"No From\";}"
    + "tag \"Negative\";";
  doTest(filterScript, null);
 }

 // Due to the negative value test, the filter execution is cancelled;
 // and none of tag commands should be executed.
 @Test
 void testCountHeaderNumericNegativeValue() {
  String filterScript = "require [\"fileinto\", \"tag\", \"flag\", \"log\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if header :count \"le\" :comparator \"i;ascii-numeric\" "
    + "[\"received\"] [\"-3\"] { tag \"received\";} else { tag \"No Received\";}"
    + "tag \"Negative\";";
  doTest(filterScript, null);
 }

 // Due to the negative value test, the filter execution is cancelled;
 // and the tag command should not be executed.
 @Test
 void testCountAddressNumericNegativeValue() {
  String filterScript = "require [\"tag\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if address :count \"le\" :comparator \"i;ascii-numeric\" "
    + "[\"to\", \"cc\"] [\"-1\"] { tag \"Priority\"; }";
  doTest(filterScript, null);
 }

 // Due to the negative value test, the filter execution is cancelled;
 // and the tag command should not be executed.
 @Test
 void testCountEnvelopeToNumericNegativeValue() {
  String filterScript = "require [\"tag\", \"relational\", \"comparator-i;ascii-numeric\"];\n"
    + "if envelope :count \"gt\" :comparator \"i;ascii-numeric\" "
    + "[\"to\"] [\"-1\"] { }"
    + "tag \"Priority\";";
  doTest(filterScript, null);
 }

    private void doTest(String filterScript, String expectedResult) {
        try {
            LmtpEnvelope env = setEnvelopeInfo();
            Account account = Provisioning.getInstance().getAccount(
                    MockProvisioning.DEFAULT_ACCOUNT_ID);
            RuleManager.clearCachedRules(account);
            Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

            account.setMailSieveScript(filterScript);
            List<ItemId> ids = RuleManager.applyRulesToIncomingMessage(
                    new OperationContext(mbox), mbox,
                    new ParsedMessage(sampleMsg.getBytes(), false), 0,
                    account.getName(), env,
                    new DeliveryContext(),
                    Mailbox.ID_FOLDER_INBOX, true);
            assertEquals(1, ids.size());
            Message msg = mbox.getMessageById(null, ids.get(0).getId());
            assertEquals(expectedResult, ArrayUtil.getFirstElement(msg.getTags()));
        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    private LmtpEnvelope setEnvelopeInfo() {
        LmtpEnvelope env = new LmtpEnvelope();
        LmtpAddress sender = new LmtpAddress("<abc@zimbra.com>", new String[] { "BODY", "SIZE" }, null);
        LmtpAddress recipient1 = new LmtpAddress("<xyz@zimbra.com>", null, null);
        LmtpAddress recipient2 = new LmtpAddress("<uvw@zimbra.com>", null, null);
        env.setSender(sender);
        env.addLocalRecipient(recipient1);
        env.addLocalRecipient(recipient2);
        return env;
    }
}
