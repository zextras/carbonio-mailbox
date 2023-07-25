package com.zimbra.cs.html;

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.zimbra.common.soap.XmlParseException;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.util.Constants;

public class XHtmlDefangTest {
 @Test
 void testMakeSAXParserFactory() throws XmlParseException, SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException {
  SAXParserFactory sf = XHtmlDefang.makeSAXParserFactory();
  assertTrue(sf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
  assertTrue(sf.getFeature(Constants.DISALLOW_DOCTYPE_DECL));
  assertFalse(sf.getFeature(Constants.EXTERNAL_GENERAL_ENTITIES));
  assertFalse(sf.getFeature(Constants.EXTERNAL_PARAMETER_ENTITIES));
  assertFalse(sf.getFeature(Constants.LOAD_EXTERNAL_DTD));
 }

 @Test
 void testDefang() {
  XHtmlDefang defang = new XHtmlDefang();
  String text = "<?xml version='1.0'\n" +
    "encoding='UTF-8'?><svg xmlns=\"http://www.w3.org/2000/svg\"\n" +
    "onload=\"alert('XSS in the attacchment')\"></svg>";
  StringReader reader = new StringReader(text);

  try {
   String sanitizedText = defang.defang(reader, true);
   assertEquals(sanitizedText.indexOf("onload"), -1, "Does not contain onload attribute.");
  } catch (IOException e) {
   fail("No Exception should be thrown");
  }
 }

}
