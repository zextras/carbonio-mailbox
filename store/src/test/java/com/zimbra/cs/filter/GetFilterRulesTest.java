// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.mail.GetFilterRules;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.util.XMLDiffChecker;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GetFilterRulesTest extends MailboxTestSuite {

  private static Account account;

  @BeforeAll
  public static void init() throws Exception {
    Provisioning prov = Provisioning.getInstance();
    prov.createDomain("zimbra.com", new HashMap<>());
    account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @Test
  void testIfWithoutAllof() {
    try {
      // - no 'allof' 'anyof' tests
      String filterScript =
          "require \"fileinto\";"
              + "if header :comparator \"i;ascii-casemap\" :matches \"Subject\" \"*\" {"
              + "  fileinto \"if-block\";"
              + "}";
      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"Subject\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }

  @Test
  void testIfWithAllofAnyOf() {
    try {
      // - 'allof' and 'anyof' tests
      String filterScript =
          "require \"fileinto\";"
              + "if allof (header :comparator \"i;ascii-casemap\" :matches \"Subject\" \"*\") {"
              + "  fileinto \"if-block1\";"
              + "}"
              + "if anyof (header :comparator \"i;ascii-casemap\" :matches \"X-Header\" \"*\") {"
              + "  fileinto \"if-block2\";"
              + "}";
      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests condition=\"allof\">"
              + "<headerTest stringComparison=\"matches\" header=\"Subject\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"if-block1\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "<filterRule active=\"1\">"
              + "<filterTests condition=\"anyof\">"
              + "<headerTest stringComparison=\"matches\" header=\"X-Header\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"if-block2\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }

  @Test
  void testNestedIfWithoutAllof() {
    try {
      // - no 'allof' 'anyof' tests
      // - nested if
      String filterScript =
          "require \"fileinto\";"
              + "if header :comparator \"i;ascii-casemap\" :matches \"Subject\" \"*\" {"
              + "  if header :matches \"From\" \"*\" {"
              + "    fileinto \"nested-if-block\";"
              + "  }"
              + "}";
      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"Subject\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<nestedRule>"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"From\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"nested-if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</nestedRule>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }

  @Test
  void testNestedIfAllofAnyof() {
    try {
      // - nested if
      // - no 'allof' and 'anyof' test for the outer if block
      // - 'anyof' test for the inner if block
      String filterScript =
          "require \"fileinto\";"
              + "if header :comparator \"i;ascii-casemap\" :matches \"Subject\" \"*\" {"
              + "  if anyof (header :matches \"From\" \"*\","
              + "            header :matches \"To\"   \"*\") {"
              + "    fileinto \"nested-if-block\";"
              + "  }"
              + "}";

      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"Subject\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<nestedRule>"
              + "<filterTests condition=\"anyof\">"
              + "<headerTest stringComparison=\"matches\" header=\"From\" index=\"0\" value=\"*\"/>"
              + "<headerTest stringComparison=\"matches\" header=\"To\"   index=\"1\" value=\"*\"/>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"nested-if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</nestedRule>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }

  @Test
  void testWithoutIfBlock1() {
    try {
      // - no if block
      String filterScript = "require \"fileinto\";" + "fileinto \"no-if-block\";";

      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"no-if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }

  @Test
  void testWithoutIfBlock2() {
    try {
      // - multiple no if block
      String filterScript =
          "require \"fileinto\";" + "fileinto \"no-if-block1\";" + "fileinto \"no-if-block2\";";

      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"no-if-block1\" index=\"0\" copy=\"0\"/>"
              + "<actionFileInto folderPath=\"no-if-block2\" index=\"1\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }

  @Test
  void testWithoutIfBlock3() {
    try {
      // - no if block, then nested if without allof/anyof
      String filterScript =
          "require \"fileinto\";"
              + "fileinto \"no-if-block\";"
              + "if header :comparator \"i;ascii-casemap\" :matches \"Subject\" \"*\" {"
              + "  if header :matches \"From\" \"*\" {"
              + "    fileinto \"nested-if-block\";"
              + "  }"
              + "}";

      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"no-if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"Subject\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<nestedRule>"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"From\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"nested-if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</nestedRule>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }

  @Test
  void testWithoutIfBlock4() {
    try {
      // - nested if without allof/anyof, then no if block
      String filterScript =
          "require \"fileinto\";"
              + "if header :comparator \"i;ascii-casemap\" :matches \"Subject\" \"*\" {"
              + "  if header :matches \"From\" \"*\" {"
              + "    fileinto \"nested-if-block\";"
              + "  }"
              + "}"
              + "fileinto \"no-if-block\";";

      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"Subject\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<nestedRule>"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"From\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"nested-if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</nestedRule>"
              + "</filterRule>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"no-if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }

  @Test
  void testWithoutIfBlock5() {
    try {
      // - combination of nested if without allof/anyof, and no if block
      String filterScript =
          "require \"fileinto\";"
              + "fileinto \"no-if-block1\";"
              + "if header :comparator \"i;ascii-casemap\" :matches \"Subject\" \"*\" {"
              + "  if header :matches \"From\" \"*\" {"
              + "    fileinto \"nested-if-block\";"
              + "  }"
              + "}"
              + "fileinto \"no-if-block2\";";

      RuleManager.clearCachedRules(account);
      account.setMailSieveScript(filterScript);

      Element request = new Element.XMLElement(MailConstants.GET_FILTER_RULES_REQUEST);
      Element response =
          new GetFilterRules().handle(request, ServiceTestUtil.getRequestContext(account));

      String expectedSoapResponse =
          "<GetFilterRulesResponse xmlns=\"urn:zimbraMail\">"
              + "<filterRules>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"no-if-block1\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"Subject\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<nestedRule>"
              + "<filterTests>"
              + "<headerTest stringComparison=\"matches\" header=\"From\" index=\"0\" value=\"*\"/>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"nested-if-block\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</nestedRule>"
              + "</filterRule>"
              + "<filterRule active=\"1\">"
              + "<filterTests>"
              + "</filterTests>"
              + "<filterActions>"
              + "<actionFileInto folderPath=\"no-if-block2\" index=\"0\" copy=\"0\"/>"
              + "</filterActions>"
              + "</filterRule>"
              + "</filterRules>"
              + "</GetFilterRulesResponse>";
      XMLDiffChecker.assertXMLEquals(expectedSoapResponse, response.prettyPrint());
    } catch (Exception e) {
      fail("No exception should be thrown" + e);
    }
  }
}
