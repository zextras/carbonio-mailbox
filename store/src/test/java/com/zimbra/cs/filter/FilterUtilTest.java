// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static com.zimbra.cs.filter.JsieveConfigMapHandler.CAPABILITY_VARIABLES;
import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FilterUtil}.
 */
public class FilterUtilTest {
  private static Account account;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
        Server server = Provisioning.getInstance().getServer(account);
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void truncateBody() throws Exception {
  // truncate a body containing a multi-byte char
  String body = StringUtil.truncateIfRequired("Andr\u00e9", 5);

  assertEquals("Andr", body, "truncated body should not have a partial char at the end");
 }

 @Test
 void noBody() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  String content =
    "From: user1@example.com\r\n"
      + "To: user2@example.com\r\n"
      + "Subject: test\r\n"
      + "Content-Type: application/octet-stream;name=\"test.pdf\"\r\n"
      + "Content-Transfer-Encoding: base64\r\n\r\n"
      + "R0a1231312ad124svsdsal=="; //obviously not a real pdf
  ParsedMessage parsedMessage = new ParsedMessage(content.getBytes(), false);
  Map<String, String> vars = FilterUtil.getVarsMap(mbox, parsedMessage, parsedMessage.getMimeMessage());
 }

 @Test
 void noHeaders() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  String content = "just some content";
  ParsedMessage parsedMessage = new ParsedMessage(content.getBytes(), false);
  Map<String, String> vars = FilterUtil.getVarsMap(mbox, parsedMessage, parsedMessage.getMimeMessage());

 }

    /*
     * Create and initialize the ZimbraMailAdapter object 
     */
    private ZimbraMailAdapter initZimbraMailAdapter() throws ServiceException {
        RuleManager.clearCachedRules(account);
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);

        IncomingMessageHandler handler = new IncomingMessageHandler(
                new OperationContext(mbox), new DeliveryContext(),
                mbox, "test@zimbra.com",
                new ParsedMessage("From: test1@zimbra.com".getBytes(), false),
                0, Mailbox.ID_FOLDER_INBOX, true);
        ZimbraMailAdapter mailAdapter = new ZimbraMailAdapter(mbox, handler);
        
        // Set various variables
        mailAdapter.addVariable("var", "hello");
        List<String> matchedValues = new ArrayList<String>();
        matchedValues.add("test1");
        matchedValues.add("test2");
        mailAdapter.setMatchedValues(matchedValues);

        return mailAdapter;
    }

 @Test
 void testVariableReplacementVariableOn() {
  try {
   ZimbraMailAdapter mailAdapter = initZimbraMailAdapter();

   // Variable feature: ON
   mailAdapter.setVariablesExtAvailable(ZimbraMailAdapter.VARIABLEFEATURETYPE.AVAILABLE);
   mailAdapter.addCapabilities(CAPABILITY_VARIABLES);

   String varValue = FilterUtil.replaceVariables(mailAdapter, "${var}");
   assertEquals("hello", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${0}");
   assertEquals("test1", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${var!}");
   assertEquals("${var!}", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${var2}");
   assertEquals("", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${test${var}");
   assertEquals("${testhello", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${test${var}");
   assertEquals("${testhello", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "\\\\${President, ${var} Inc.}");
   assertEquals("\\\\${President, hello Inc.}", varValue);

   // set "company" "ACME";
   // set "a.b" "おしらせ"; (or any non-ascii characters)
   // set "c_d" "C";
   // set "1" "One"; ==> Should be ignored or error [Note 1]
   // set "23" "twenty three"; ==> Should be ignored or error [Note 1]
   // set "combination" "Hello ${company}!!";
   mailAdapter.addVariable("var", "hello");

   mailAdapter.addVariable("company", "ACME");
   mailAdapter.addVariable("a_b", "\u304a\u3057\u3089\u305b");
   mailAdapter.addVariable("c_d", "C");
   mailAdapter.addVariable("1", "One");
   mailAdapter.addVariable("23", "twenty three");
   mailAdapter.addVariable("combination", "Hello ACME!!");

   varValue = FilterUtil.replaceVariables(mailAdapter, "${full}");
   assertEquals("", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${company}");
   assertEquals("ACME", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${BAD${Company}");
   assertEquals("${BADACME", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${company");
   assertEquals("${company", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${${COMpANY}}");
   assertEquals("${ACME}", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${a_b}}");
   assertEquals("\u304a\u3057\u3089\u305b}", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "$c_d}}");
   assertEquals("$c_d}}", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "You've got a mail. ${a_b} ${combination} ${c_d}hao!");
   assertEquals("You've got a mail. \u304a\u3057\u3089\u305b Hello ACME!! Chao!", varValue);
  } catch (Exception e) {
   fail("No exception should be thrown: " + e);
  }
 }

 @Test
 void testVariableReplacementQutdAndEncoded() {
  try {
   ZimbraMailAdapter mailAdapter = initZimbraMailAdapter();
   mailAdapter.setVariablesExtAvailable(ZimbraMailAdapter.VARIABLEFEATURETYPE.AVAILABLE);
   mailAdapter.addCapabilities(CAPABILITY_VARIABLES);

   String varValue = FilterUtil.replaceVariables(mailAdapter, "${va\\r}");
   assertEquals("hello", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "${}");
   assertEquals("${}", varValue);

   mailAdapter.addVariable("var", "hel\\*lo");
   varValue = FilterUtil.replaceVariables(mailAdapter, "${var}");
   assertEquals("hel\\*lo", varValue);

   varValue = FilterUtil.replaceVariables(mailAdapter, "hello${test}");
   assertEquals("hello", varValue);
  } catch (Exception e) {
   fail("No exception should be thrown: " + e);
  }
 }

 @Test
 void testToJavaRegex() {
  String regex = FilterUtil.sieveToJavaRegex("coyote@**.com");
  assertEquals("coyote@(.*?)(.*)\\.com", regex);
 }
}
