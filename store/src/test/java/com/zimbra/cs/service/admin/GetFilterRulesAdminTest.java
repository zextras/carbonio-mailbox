// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.filter.RuleManager;
import com.zimbra.cs.filter.RuleManager.AdminFilterType;
import com.zimbra.cs.filter.RuleManager.FilterType;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.soap.mail.type.EditheaderTest;
import com.zimbra.soap.mail.type.FilterAction;
import com.zimbra.soap.mail.type.FilterRule;

public class GetFilterRulesAdminTest {
    private static final String ACCOUNTNAME = "test1_zcs273_"+System.currentTimeMillis()+"@zimbra.com";
    private static Provisioning prov = null;
    private Account account = null;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        prov = Provisioning.getInstance();

        Map<String, Object> attrs = Maps.newHashMap();
        prov.createAccount(ACCOUNTNAME, "secret", attrs);
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
        account = prov.getAccountByName(ACCOUNTNAME);
    }

 /**************addheader*/
 @Test
 void testSieveToSoapAddheaderActionWithoutLast() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "# rule1\n"
    + "require [\"editheader\"];\n"
    + "addheader \"X-My-Header\" \"Test Value\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule rule = filterRules.get(0);
  assertTrue(rule.isActive());
  assertEquals(rule.getName(), "rule1");
  assertEquals(rule.getActionCount(), 1);
  FilterAction filterAction = rule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.AddheaderAction);
  FilterAction.AddheaderAction action = (FilterAction.AddheaderAction) filterAction;
  assertEquals(action.getHeaderName(), "X-My-Header");
  assertEquals(action.getHeaderValue(), "Test Value");
  assertNull(action.getLast());
 }

 @Test
 void testSieveToSoapAddheaderActionWithLast() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "# rule2\n"
    + "require [\"editheader\"];\n"
    + "addheader :last \"X-My-Header\" \"Test Value\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule rule = filterRules.get(0);
  assertTrue(rule.isActive());
  assertEquals(rule.getName(), "rule2");
  assertEquals(rule.getActionCount(), 1);
  FilterAction filterAction = rule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.AddheaderAction);
  FilterAction.AddheaderAction action = (FilterAction.AddheaderAction) filterAction;
  assertEquals(action.getHeaderName(), "X-My-Header");
  assertEquals(action.getHeaderValue(), "Test Value");
  assertTrue(action.getLast());
 }

 // worng tag instead of last
 @Test
 void negativeTestSieveToSoapAddheaderAction1() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "addheader :abcd \"X-My-Header\" \"Test Value\";";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals(se.getMessage(), "Invalid argument :abcd received with addheader");
  }
 }

 // empty headerName
 @Test
 void negativeTestSieveToSoapAddheaderAction2() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "addheader \"\" \"Test Value\";";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals(se.getMessage(), "parse error: Invalid addheader action: Missing headerName or headerValue");
  }
 }

 // empty headerValue
 @Test
 void negativeTestSieveToSoapAddheaderAction3() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "addheader \"X-My-Header\" \"\";";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals(se.getMessage(), "parse error: Invalid addheader action: Missing headerName or headerValue");
  }
 }

 /**************deleteheader*/
 @Test
 void testSieveToSoapDeleteheaderActionBasic() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader \"X-My-Header\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.DeleteheaderAction);
  FilterAction.DeleteheaderAction action = (FilterAction.DeleteheaderAction) filterAction;
  assertNull(action.getLast());
  assertNull(action.getOffset());
  EditheaderTest test = action.getTest();
  assertNull(test.getComparator());
  assertNull(test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  assertNull(test.getHeaderValue());
 }

 @Test
 void testSieveToSoapDeleteheaderActionBasicWithValue() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader \"X-My-Header\" \"Test Value\";";// matchType and comparator should be added by default
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.DeleteheaderAction);
  FilterAction.DeleteheaderAction action = (FilterAction.DeleteheaderAction) filterAction;
  assertNull(action.getLast());
  assertNull(action.getOffset());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-casemap", test.getComparator());
  assertEquals("is", test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("Test Value", values.get(0));
 }

 @Test
 void testSieveToSoapDeleteheaderActionWithIndex() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :index 3 \"X-My-Header\" \"Test Value\";";// matchType and comparator should be added by default
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.DeleteheaderAction);
  FilterAction.DeleteheaderAction action = (FilterAction.DeleteheaderAction) filterAction;
  assertNull(action.getLast());
  assertEquals(3, action.getOffset().intValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-casemap", test.getComparator());
  assertEquals("is", test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("Test Value", values.get(0));
 }

 @Test
 void testSieveToSoapDeleteheaderActionWithIndexAndLast() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :last :index 3 \"X-My-Header\" \"Test Value\";";// matchType and comparator should be added by default
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.DeleteheaderAction);
  FilterAction.DeleteheaderAction action = (FilterAction.DeleteheaderAction) filterAction;
  assertTrue(action.getLast());
  assertEquals(3, action.getOffset().intValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-casemap", test.getComparator());
  assertEquals("is", test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("Test Value", values.get(0));
 }

 @Test
 void testSieveToSoapDeleteheaderActionWithIndexAndLastWithoutValue() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :last :index 3 \"X-My-Header\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.DeleteheaderAction);
  FilterAction.DeleteheaderAction action = (FilterAction.DeleteheaderAction) filterAction;
  assertTrue(action.getLast());
  assertEquals(3, action.getOffset().intValue());
  EditheaderTest test = action.getTest();
  assertNull(test.getComparator());
  assertNull(test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  assertNull(test.getHeaderValue());
 }

 @Test
 void testSieveToSoapDeleteheaderActionWithMultiValue() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :last :index 3 :comparator \"i;octet\" :contains \"X-My-Header\" [\"Value1\", \"Value2\"];";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.DeleteheaderAction);
  FilterAction.DeleteheaderAction action = (FilterAction.DeleteheaderAction) filterAction;
  assertTrue(action.getLast());
  assertEquals(3, action.getOffset().intValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;octet", test.getComparator());
  assertEquals("contains", test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(2, values.size());
  assertEquals("Value1", values.get(0));
  assertEquals("Value2", values.get(1));
 }

 @Test
 void testSieveToSoapDeleteheaderActionWithValueAndRelationalComparator() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :last :index 3 :value \"ge\" :comparator \"i;ascii-numeric\" \"X-My-Header\" \"2\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.DeleteheaderAction);
  FilterAction.DeleteheaderAction action = (FilterAction.DeleteheaderAction) filterAction;
  assertTrue(action.getLast());
  assertEquals(3, action.getOffset().intValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-numeric", test.getComparator());
  assertNull(test.getMatchType());
  assertEquals("ge", test.getRelationalComparator());
  assertNull(test.getCount());
  assertTrue(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("2", values.get(0));
 }

 @Test
 void testSieveToSoapDeleteheaderActionWithCountAndRelationalComparator() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :last :index 3 :count \"ge\" :comparator \"i;ascii-numeric\" \"X-My-Header\" \"2\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.DeleteheaderAction);
  FilterAction.DeleteheaderAction action = (FilterAction.DeleteheaderAction) filterAction;
  assertTrue(action.getLast());
  assertEquals(3, action.getOffset().intValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-numeric", test.getComparator());
  assertNull(test.getMatchType());
  assertEquals("ge", test.getRelationalComparator());
  assertTrue(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("2", values.get(0));
 }

 // invalid tag
 @Test
 void negativeTestSieveToSoapDeleteheaderAction1() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :last :index 3 :comparator \"i;octet\" :asdf \"X-My-Header\" [\"Value1\", \"Value2\"];";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals(se.getMessage(), "parse error: Invalid tag \":asdf\" received with deleteheader");
  }
 }

 // missing index number
 @Test
 void negativeTestSieveToSoapDeleteheaderAction2() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :last :index :comparator \"i;octet\" \"X-My-Header\" [\"Value1\", \"Value2\"];";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals(se.getMessage(), "parse error: Invalid value \":comparator\" received with \":index\"");
  }
 }

 // missing index tag
 @Test
 void negativeTestSieveToSoapDeleteheaderAction3() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader;";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals(se.getMessage(), "parse error: EditheaderTest : Missing headerName");
  }
 }

 // relational comparator and matchType together
 @Test
 void negativeTestSieveToSoapDeleteheaderAction4() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\"];\n"
    + "# rule1\n"
    + "deleteheader :last :index 3 :count \"ge\" :comparator \"i;ascii-numeric\" :is \"X-My-Header\" \"2\";";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals(se.getMessage(), "parse error: EditheaderTest : :count or :value can not be used with matchType");
  }
 }

 /**************replaceheader*/
 // TODO: start writing unit tests
 @Test
 void testSieveToSoapReplaceheaderActionBasic() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :newvalue \"[new] ${1}\" \"X-My-Header\" \"xyz\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.ReplaceheaderAction);
  FilterAction.ReplaceheaderAction action = (FilterAction.ReplaceheaderAction) filterAction;
  assertNull(action.getLast());
  assertNull(action.getOffset());
  assertNull(action.getNewName());
  assertEquals("[new] ${1}", action.getNewValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-casemap", test.getComparator());
  assertEquals("is", test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("xyz", values.get(0));
 }

 @Test
 void testSieveToSoapReplaceheaderActionBasicWithNewnameAndNewvaue() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" \"X-My-Header\" \"xyz\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.ReplaceheaderAction);
  FilterAction.ReplaceheaderAction action = (FilterAction.ReplaceheaderAction) filterAction;
  assertNull(action.getLast());
  assertNull(action.getOffset());
  assertEquals("X-My-Header2", action.getNewName());
  assertEquals("[new] ${1}", action.getNewValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-casemap", test.getComparator());
  assertEquals("is", test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("xyz", values.get(0));
 }

 @Test
 void testSieveToSoapReplaceheaderActionBasicWithComparator() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" :comparator \"i;octet\" \"X-My-Header\" \"xyz\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.ReplaceheaderAction);
  FilterAction.ReplaceheaderAction action = (FilterAction.ReplaceheaderAction) filterAction;
  assertNull(action.getLast());
  assertNull(action.getOffset());
  assertEquals("X-My-Header2", action.getNewName());
  assertEquals("[new] ${1}", action.getNewValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;octet", test.getComparator());
  assertEquals("is", test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("xyz", values.get(0));
 }

 @Test
 void testSieveToSoapReplaceheaderActionBasicWithComparatorAndMatchType() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" :comparator \"i;octet\" :contains \"X-My-Header\" \"xyz\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.ReplaceheaderAction);
  FilterAction.ReplaceheaderAction action = (FilterAction.ReplaceheaderAction) filterAction;
  assertNull(action.getLast());
  assertNull(action.getOffset());
  assertEquals("X-My-Header2", action.getNewName());
  assertEquals("[new] ${1}", action.getNewValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;octet", test.getComparator());
  assertEquals("contains", test.getMatchType());
  assertNull(test.getRelationalComparator());
  assertNull(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("xyz", values.get(0));
 }

 @Test
 void testSieveToSoapReplaceheaderActionBasicWithRelationalComparatorAndValue() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" :comparator \"i;ascii-numeric\" :value \"ge\" \"X-My-Header\" \"2\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.ReplaceheaderAction);
  FilterAction.ReplaceheaderAction action = (FilterAction.ReplaceheaderAction) filterAction;
  assertNull(action.getLast());
  assertNull(action.getOffset());
  assertEquals("X-My-Header2", action.getNewName());
  assertEquals("[new] ${1}", action.getNewValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-numeric", test.getComparator());
  assertNull(test.getMatchType());
  assertEquals("ge", test.getRelationalComparator());
  assertNull(test.getCount());
  assertTrue(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("2", values.get(0));
 }

 @Test
 void testSieveToSoapReplaceheaderActionBasicWithRelationalComparatorAndCount() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" :comparator \"i;ascii-numeric\" :count \"eq\" \"X-My-Header\" \"3\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.ReplaceheaderAction);
  FilterAction.ReplaceheaderAction action = (FilterAction.ReplaceheaderAction) filterAction;
  assertNull(action.getLast());
  assertNull(action.getOffset());
  assertEquals("X-My-Header2", action.getNewName());
  assertEquals("[new] ${1}", action.getNewValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-numeric", test.getComparator());
  assertNull(test.getMatchType());
  assertEquals("eq", test.getRelationalComparator());
  assertTrue(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("3", values.get(0));
 }

 @Test
 void testSieveToSoapReplaceheaderActionBasicWithIndex() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :index 4 :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" :comparator \"i;ascii-numeric\" :count \"eq\" \"X-My-Header\" \"3\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.ReplaceheaderAction);
  FilterAction.ReplaceheaderAction action = (FilterAction.ReplaceheaderAction) filterAction;
  assertNull(action.getLast());
  assertEquals(4, action.getOffset().intValue());
  assertEquals("X-My-Header2", action.getNewName());
  assertEquals("[new] ${1}", action.getNewValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-numeric", test.getComparator());
  assertNull(test.getMatchType());
  assertEquals("eq", test.getRelationalComparator());
  assertTrue(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("3", values.get(0));
 }

 @Test
 void testSieveToSoapReplaceheaderActionBasicWithLastAndIndex() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :last :index 2 :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" :comparator \"i;ascii-numeric\" :count \"eq\" \"X-My-Header\" \"3\";";
  account.setAdminSieveScriptBefore(script);

  List<FilterRule> filterRules = RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  assertEquals(filterRules.size(), 1);
  FilterRule filterRule = filterRules.get(0);
  assertEquals("rule1", filterRule.getName());
  assertTrue(filterRule.isActive());
  assertEquals(1, filterRule.getFilterActions().size());
  FilterAction filterAction = filterRule.getFilterActions().get(0);
  assertTrue(filterAction instanceof FilterAction.ReplaceheaderAction);
  FilterAction.ReplaceheaderAction action = (FilterAction.ReplaceheaderAction) filterAction;
  assertTrue(action.getLast());
  assertEquals(2, action.getOffset().intValue());
  assertEquals("X-My-Header2", action.getNewName());
  assertEquals("[new] ${1}", action.getNewValue());
  EditheaderTest test = action.getTest();
  assertEquals("i;ascii-numeric", test.getComparator());
  assertNull(test.getMatchType());
  assertEquals("eq", test.getRelationalComparator());
  assertTrue(test.getCount());
  assertNull(test.getValue());
  assertEquals("X-My-Header", test.getHeaderName());
  List<String> values = test.getHeaderValue();
  assertEquals(1, values.size());
  assertEquals("3", values.get(0));
 }

 // with last without index
 @Test
 void negativeTestSieveToSoapReplaceheaderAction() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :last :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" :comparator \"i;ascii-numeric\" :count \"eq\" \"X-My-Header\" \"3\";";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals("parse error: :index <offset> must be provided with :last", se.getMessage());
  }
 }

 // with last, index and without number
 @Test
 void negativeTestSieveToSoapReplaceheaderAction2() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :last :index :newname \"X-My-Header2\" :newvalue \"[new] ${1}\" :comparator \"i;ascii-numeric\" :count \"eq\" \"X-My-Header\" \"3\";";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals("parse error: Invalid value \":newname\" received with \":index\"", se.getMessage());
  }
 }

 // missing :newname and :newvalue
 @Test
 void negativeTestSieveToSoapReplaceheaderAction3() throws ServiceException {
  RuleManager.clearCachedRules(account);
  String script = "require [\"editheader\", \"variables\"];\n"
    + "# rule1\n"
    + "replaceheader :last :index 2 :comparator \"i;ascii-numeric\" :count \"eq\" \"X-My-Header\" \"3\";";
  account.setAdminSieveScriptBefore(script);

  try {
   RuleManager.getAdminRulesAsXML(account, FilterType.INCOMING, AdminFilterType.BEFORE);
  } catch (ServiceException se) {
   assertEquals("parse error: :newname or :newvalue must be provided with replaceHeader", se.getMessage());
  }
 }
}