// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.mail.GetFilterRules;
import com.zimbra.cs.service.mail.ModifyFilterRules;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.util.XMLDiffChecker;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValueComparisonTest {

 private static Account account;

 @BeforeAll
 public static void init() throws Exception {
  MailboxTestUtil.initServer();
  Provisioning prov = Provisioning.getInstance();
  account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
 }

 @BeforeEach
 public void setUp() throws Exception {
  MailboxTestUtil.clearData();
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for header test having caseSensitive attribute with
  * string comparison
  * @throws Exception
     */
 @Test
 void testHeaderTestStringComparisonCaseSensitivity() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element headerTest = filterTests.addNonUniqueElement(MailConstants.E_HEADER_TEST);
  headerTest.addAttribute(MailConstants.A_HEADER, "subject");
  headerTest.addAttribute(MailConstants.A_STRING_COMPARISON, "contains");
  headerTest.addAttribute(MailConstants.A_CASE_SENSITIVE, "true");
  headerTest.addAttribute(MailConstants.A_VALUE, "Important");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (header :contains :comparator \"i;octet\" [\"subject\"] \"Important\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for address test having caseSensitive attribute with
  * string comparison
  * @throws Exception
     */
 @Test
 void testAddressTestStringComparisonCaseSensitivity() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element addressTest = filterTests.addNonUniqueElement(MailConstants.E_ADDRESS_TEST);
  addressTest.addAttribute(MailConstants.A_HEADER, "from");
  addressTest.addAttribute(MailConstants.A_STRING_COMPARISON, "contains");
  addressTest.addAttribute(MailConstants.A_CASE_SENSITIVE, "true");
  addressTest.addAttribute(MailConstants.A_VALUE, "abCD");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (address :all :contains :comparator \"i;octet\" [\"from\"] \"abCD\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for envelope test having caseSensitive attribute with
  * string comparison
  * @throws Exception
     */
 @Test
 void testEnvelopeTestStringComparisonCaseSensitivity() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element envelopeTest = filterTests.addNonUniqueElement(MailConstants.E_ENVELOPE_TEST);
  envelopeTest.addAttribute(MailConstants.A_HEADER, "from");
  envelopeTest.addAttribute(MailConstants.A_STRING_COMPARISON, "contains");
  envelopeTest.addAttribute(MailConstants.A_CASE_SENSITIVE, "true");
  envelopeTest.addAttribute(MailConstants.A_VALUE, "abCD");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (envelope :all :contains :comparator \"i;octet\" [\"from\"] \"abCD\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for header test having caseSensitive attribute with
  * value comparison
  * @throws Exception
     */
 @Test
 void testHeaderTestValueComparisonCaseSensitivity() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element headerTest = filterTests.addNonUniqueElement(MailConstants.E_HEADER_TEST);
  headerTest.addAttribute(MailConstants.A_HEADER, "subject");
  headerTest.addAttribute(MailConstants.A_VALUE_COMPARISON, "eq");
  headerTest.addAttribute(MailConstants.A_CASE_SENSITIVE, "true");
  headerTest.addAttribute(MailConstants.A_VALUE, "Important");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (header :value \"eq\" :comparator \"i;octet\" [\"subject\"] \"Important\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for address test having caseSensitive attribute with
  * value comparison
  * @throws Exception
     */
 @Test
 void testAddressTestValueComparisonCaseSensitivity() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element addressTest = filterTests.addNonUniqueElement(MailConstants.E_ADDRESS_TEST);
  addressTest.addAttribute(MailConstants.A_HEADER, "from");
  addressTest.addAttribute(MailConstants.A_VALUE_COMPARISON, "eq");
  addressTest.addAttribute(MailConstants.A_CASE_SENSITIVE, "true");
  addressTest.addAttribute(MailConstants.A_VALUE, "abCD");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (address :value \"eq\" :all :comparator \"i;octet\" [\"from\"] \"abCD\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for envelope test having caseSensitive attribute with
  * value comparison
  * @throws Exception
     */
 @Test
 void testEnvelopeTestValueComparisonCaseSensitivity() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element envelopeTest = filterTests.addNonUniqueElement(MailConstants.E_ENVELOPE_TEST);
  envelopeTest.addAttribute(MailConstants.A_HEADER, "from");
  envelopeTest.addAttribute(MailConstants.A_VALUE_COMPARISON, "eq");
  envelopeTest.addAttribute(MailConstants.A_CASE_SENSITIVE, "true");
  envelopeTest.addAttribute(MailConstants.A_VALUE, "abCD");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (envelope :value \"eq\" :all :comparator \"i;octet\" [\"from\"] \"abCD\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for header test having value comparison with 
  * valueComparisonComparator
  * @throws Exception
     */
 @Test
 void testHeaderTestValueComparisonComparator() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element headerTest = filterTests.addNonUniqueElement(MailConstants.E_HEADER_TEST);
  headerTest.addAttribute(MailConstants.A_HEADER, "subject");
  headerTest.addAttribute(MailConstants.A_VALUE_COMPARISON, "eq");
  headerTest.addAttribute(MailConstants.A_VALUE_COMPARISON_COMPARATOR, "i;octet");
  headerTest.addAttribute(MailConstants.A_VALUE, "Important");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (header :value \"eq\" :comparator \"i;octet\" [\"subject\"] \"Important\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for address test having value comparison with 
  * valueComparisonComparator
  * @throws Exception
     */
 @Test
 void testAddressTestValueComparisonComparator() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element addressTest = filterTests.addNonUniqueElement(MailConstants.E_ADDRESS_TEST);
  addressTest.addAttribute(MailConstants.A_HEADER, "from");
  addressTest.addAttribute(MailConstants.A_VALUE_COMPARISON, "eq");
  addressTest.addAttribute(MailConstants.A_VALUE_COMPARISON_COMPARATOR, "i;octet");
  addressTest.addAttribute(MailConstants.A_VALUE, "abCD");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (address :value \"eq\" :all :comparator \"i;octet\" [\"from\"] \"abCD\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests rule creation through ModifyFilterRulesRequest for envelope test having value comparison with 
  * valueComparisonComparator
  * @throws Exception
     */
 @Test
 void testEnvelopeTestValueComparisonComparator() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.MODIFY_FILTER_RULES_REQUEST);

  Element rules = request.addNonUniqueElement(MailConstants.E_FILTER_RULES);
  Element rule = rules.addNonUniqueElement(MailConstants.E_FILTER_RULE);
  rule.addAttribute(MailConstants.A_ACTIVE, true);
  rule.addAttribute(MailConstants.A_NAME, "Test1");
  Element filteraction = rule.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
  Element actionInto = filteraction.addNonUniqueElement(MailConstants.E_ACTION_FILE_INTO);
  actionInto.addAttribute(MailConstants.A_FOLDER_PATH, "Junk");
  filteraction.addNonUniqueElement(MailConstants.E_ACTION_STOP);
  Element filterTests = rule.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
  filterTests.addAttribute(MailConstants.A_CONDITION, "anyof");
  Element envelopeTest = filterTests.addNonUniqueElement(MailConstants.E_ENVELOPE_TEST);
  envelopeTest.addAttribute(MailConstants.A_HEADER, "from");
  envelopeTest.addAttribute(MailConstants.A_VALUE_COMPARISON, "eq");
  envelopeTest.addAttribute(MailConstants.A_VALUE_COMPARISON_COMPARATOR, "i;octet");
  envelopeTest.addAttribute(MailConstants.A_VALUE, "abCD");

  try {
   new ModifyFilterRules().handle(request, ServiceTestUtil.getRequestContext(acct));
  } catch (ServiceException e) {
   fail("This test is expected not to throw exception. ");
  }

  String expectedScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
  expectedScript += "# Test1\n";
  expectedScript += "if anyof (envelope :value \"eq\" :all :comparator \"i;octet\" [\"from\"] \"abCD\") {\n";
  expectedScript += "    fileinto \"Junk\";\n";
  expectedScript += "    stop;\n";
  expectedScript += "}\n";

  ZimbraLog.filter.info(acct.getMailSieveScript());
  ZimbraLog.filter.info(expectedScript);
  assertEquals(expectedScript, acct.getMailSieveScript());
 }

 /**
  * Tests GetFilterRulesRequest for rule containing header test and caseSensitive attribute with
  * value comparison
  * @throws Exception
     */
 @Test
 void testGetRuleHeaderTestValueComparisonCaseSensitivity() throws Exception {
  try {
   String filterScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
   filterScript += "if anyof (header :value \"eq\" :comparator \"i;octet\" [\"subject\"] \"Important\") {\n";
   filterScript += "    fileinto \"Junk\";\n";
   filterScript += "    stop;\n";
   filterScript += "}\n";

   ZimbraLog.filter.info(filterScript);

   RuleManager.clearCachedRules(account);
   account.setMailSieveScript(filterScript);

   Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
   Element response = new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

   String expectedSoapResponse =
     "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\"> \n"
       + "<filterRules> \n"
       +  "<filterRule active=\"1\"> \n"
       + "<filterTests condition=\"anyof\"> \n"
       + "<headerTest valueComparison=\"eq\" caseSensitive=\"1\" header=\"subject\" index=\"0\" valueComparisonComparator=\"i;octet\" value=\"Important\"/> \n"
       + "</filterTests> \n"
       + "<filterActions> \n"
       + " <actionFileInto folderPath=\"Junk\" index=\"0\" copy=\"0\"/> \n"
       + "  <actionStop index=\"1\"/> \n"
       + "</filterActions> \n"
       + "</filterRule> \n"
       + "</filterRules> \n"
       + "</GetFilterRulesResponse>";
   ZimbraLog.filter.info(response.prettyPrint());
   XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 /**
  * Tests GetFilterRulesRequest for rule containing address test and caseSensitive attribute with
  * value comparison
  * @throws Exception
     */
 @Test
 void testGetRuleAddressTestValueComparisonCaseSensitivity() throws Exception {
  try {
   String filterScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
   filterScript += "if anyof (address :value \"eq\" :all :comparator \"i;octet\" [\"from\"] \"abCD\") {\n";
   filterScript += "    fileinto \"Junk\";\n";
   filterScript += "    stop;\n";
   filterScript += "}\n";

   ZimbraLog.filter.info(filterScript);

   RuleManager.clearCachedRules(account);
   account.setMailSieveScript(filterScript);

   Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
   Element response = new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

   String expectedSoapResponse =
     "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\"> \n"
       + "<filterRules> \n"
       +  "<filterRule active=\"1\"> \n"
       + "<filterTests condition=\"anyof\"> \n"
       + "<addressTest valueComparison=\"eq\" caseSensitive=\"1\" part=\"all\" header=\"from\" index=\"0\" valueComparisonComparator=\"i;octet\" value=\"abCD\"/> \n"
       + "</filterTests> \n"
       + "<filterActions> \n"
       + " <actionFileInto folderPath=\"Junk\" index=\"0\" copy=\"0\"/> \n"
       + "  <actionStop index=\"1\"/> \n"
       + "</filterActions> \n"
       + "</filterRule> \n"
       + "</filterRules> \n"
       + "</GetFilterRulesResponse>";
   ZimbraLog.filter.info(response.prettyPrint());
   XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }

 /**
  * Tests GetFilterRulesRequest for rule containing envelope test and caseSensitive attribute with
  * value comparison
  * @throws Exception
     */
 @Test
 void testGetRuleEnvelopeTestValueComparisonCaseSensitivity() throws Exception {
  try {
   String filterScript = "require [\"fileinto\", \"copy\", \"reject\", \"tag\", \"flag\", \"variables\", \"log\", \"enotify\", \"envelope\", \"body\", \"ereject\", \"reject\", \"relational\", \"comparator-i;ascii-numeric\"];\n\n";
   filterScript += "if anyof (envelope :value \"eq\" :all :comparator \"i;octet\" [\"from\"] \"abCD\") {\n";
   filterScript += "    fileinto \"Junk\";\n";
   filterScript += "    stop;\n";
   filterScript += "}\n";

   ZimbraLog.filter.info(filterScript);

   RuleManager.clearCachedRules(account);
   account.setMailSieveScript(filterScript);

   Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
   Element response = new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

   String expectedSoapResponse =
     "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\"> \n"
       + "<filterRules> \n"
       +  "<filterRule active=\"1\"> \n"
       + "<filterTests condition=\"anyof\"> \n"
       + "<envelopeTest valueComparison=\"eq\" caseSensitive=\"1\" part=\"all\" header=\"from\" index=\"0\" valueComparisonComparator=\"i;octet\" value=\"abCD\"/> \n"
       + "</filterTests> \n"
       + "<filterActions> \n"
       + " <actionFileInto folderPath=\"Junk\" index=\"0\" copy=\"0\"/> \n"
       + "  <actionStop index=\"1\"/> \n"
       + "</filterActions> \n"
       + "</filterRule> \n"
       + "</filterRules> \n"
       + "</GetFilterRulesResponse>";
   ZimbraLog.filter.info(response.prettyPrint());
   XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
  } catch (Exception e) {
   fail("No exception should be thrown" + e);
  }
 }
}
