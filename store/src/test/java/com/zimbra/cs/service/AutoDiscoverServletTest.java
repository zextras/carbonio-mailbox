// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.common.util.Constants;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author zimbra
 *
 */
public class AutoDiscoverServletTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
    }

 /**
  * Test method for {@link com.zimbra.cs.service.AutoDiscoverServlet#isEwsClient(java.lang.String)}.
  */
 @Test
 void testIsEwsClient() {
  // Active Sync
  boolean result = AutoDiscoverServlet.isEwsClient("http://schemas.microsoft.com/exchange/autodiscover/mobilesync/responseschema/2006");
  assertFalse(result);

  // Ews CLient
  result = AutoDiscoverServlet.isEwsClient("http://schemas.microsoft.com/exchange/autodiscover/outlook/responseschema/2006a");
  assertTrue(result);
 }

 @Test
 void testMakeDocumentBuilderFactory() throws ParserConfigurationException {
  DocumentBuilderFactory dbf = AutoDiscoverServlet.makeDocumentBuilderFactory();
  assertTrue(dbf.isNamespaceAware());
  assertTrue(dbf.getFeature(Constants.DISALLOW_DOCTYPE_DECL));
  assertFalse(dbf.getFeature(Constants.EXTERNAL_GENERAL_ENTITIES));
  assertFalse(dbf.getFeature(Constants.EXTERNAL_PARAMETER_ENTITIES));
  assertFalse(dbf.getFeature(Constants.LOAD_EXTERNAL_DTD));
 }

 @Test
 void testMakeTransformerFactory() {
  TransformerFactory factory = AutoDiscoverServlet.makeTransformerFactory();
  assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
  assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
 }
}
