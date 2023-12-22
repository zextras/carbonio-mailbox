// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

import static org.junit.jupiter.api.Assertions.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.junit.jupiter.api.Test;

import com.zimbra.common.util.Constants;

public class XsdCleanerTest {
  @Test
  void testMakeDocumentBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory dbf = XsdCleaner.makeDocumentBuilderFactory();
    assertTrue(dbf.isNamespaceAware());
    assertTrue(dbf.isIgnoringComments());
    assertFalse(dbf.isXIncludeAware());
    assertFalse(dbf.isExpandEntityReferences());
    assertTrue(dbf.getFeature(Constants.DISALLOW_DOCTYPE_DECL));
    assertFalse(dbf.getFeature(Constants.EXTERNAL_GENERAL_ENTITIES));
    assertFalse(dbf.getFeature(Constants.EXTERNAL_PARAMETER_ENTITIES));
    assertFalse(dbf.getFeature(Constants.LOAD_EXTERNAL_DTD));
  }

  @Test
  void testMakeTransformerFactory() {
    TransformerFactory factory = XsdCleaner.makeTransformerFactory();
    assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
    assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
  }
}