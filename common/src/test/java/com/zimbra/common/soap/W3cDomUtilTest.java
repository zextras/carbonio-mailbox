// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.common.soap;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;

import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.google.common.io.ByteStreams;
import com.zimbra.common.util.Constants;

public class W3cDomUtilTest {
  @Test
  void testMakeDocumentBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory dbf = W3cDomUtil.makeDocumentBuilderFactory();
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
  void testMakeSAXParserFactory() throws XmlParseException, SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException {
    SAXParserFactory sf = W3cDomUtil.makeSAXParserFactory();
    assertTrue(sf.isNamespaceAware());
    assertFalse(sf.isXIncludeAware());
    assertFalse(sf.isValidating());
    assertTrue(sf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    assertTrue(sf.getFeature(Constants.DISALLOW_DOCTYPE_DECL));
    assertFalse(sf.getFeature(Constants.EXTERNAL_GENERAL_ENTITIES));
    assertFalse(sf.getFeature(Constants.EXTERNAL_PARAMETER_ENTITIES));
    assertFalse(sf.getFeature(Constants.LOAD_EXTERNAL_DTD));
  }

  @Test
  void testGetDom4jSAXReaderWhichUsesSecureProcessing() throws XmlParseException, SAXException {
    SAXReader reader = W3cDomUtil.getDom4jSAXReaderWhichUsesSecureProcessing();
    assertTrue(reader.getXMLReader().getFeature(Constants.DISALLOW_DOCTYPE_DECL));
    assertFalse(reader.getXMLReader().getFeature(Constants.EXTERNAL_GENERAL_ENTITIES));
    assertFalse(reader.getXMLReader().getFeature(Constants.EXTERNAL_PARAMETER_ENTITIES));
  }

  @Test
  void testMakeTransformerFactory() {
    TransformerFactory factory = W3cDomUtil.makeTransformerFactory();
    assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
    assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
  }

  @Test
  void testRemoveInvalidChars() throws IOException {
    String invalidCharsXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "\n"
        + "<backupMetadata xmlns=\"urn:zimbraBackupMeta\" version=\"7.1\">\n"
        + "  <accountLdap zimbraId=\"3f38c5be-1b08-463c-8326-d7e26b2a515a\" email=\"account1@zcs804.zimbra.com\">\n"
        + "    <attributes>\n" + "      <attribute name=\"zimbraMailSieveScript\">\n"
        + "        <value>require [\"fileinto\", \"reject\", \"tag\", \"flag\"];\n" + "\n" + "# filter1\n"
        + "if allof (address :all :contains :comparator \"i;ascii-casemap\" [\"from\"] \"foo&#7;bar &lt;acco&#7;unt2@zcs804.zimbra.com&gt;\",\n"
        + "  header :is [\"subject\"] \"foobar &lt;account2@zcs804.zimbra.com&gt;\") {\n" + "    keep;\n"
        + "    stop;\n" + "}\n" + "</value>\n" + "      </attribute>\n" + "    </attributes>\n"
        + "  </accountLdap>\n" + "</backupMetadata>";

    String validCharsXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "\n"
        + "<backupMetadata xmlns=\"urn:zimbraBackupMeta\" version=\"7.1\">\n"
        + "  <accountLdap zimbraId=\"3f38c5be-1b08-463c-8326-d7e26b2a515a\" email=\"account1@zcs804.zimbra.com\">\n"
        + "    <attributes>\n" + "      <attribute name=\"zimbraMailSieveScript\">\n"
        + "        <value>require [\"fileinto\", \"reject\", \"tag\", \"flag\"];\n" + "\n" + "# filter1\n"
        + "if allof (address :all :contains :comparator \"i;ascii-casemap\" [\"from\"] \"foobar &lt;account2@zcs804.zimbra.com&gt;\",\n"
        + "  header :is [\"subject\"] \"foobar &lt;account2@zcs804.zimbra.com&gt;\") {\n" + "    keep;\n"
        + "    stop;\n" + "}\n" + "</value>\n" + "      </attribute>\n" + "    </attributes>\n"
        + "  </accountLdap>\n" + "</backupMetadata>";

    InputStream clean_xml_is = W3cDomUtil.removeInvalidXMLChars(invalidCharsXML.getBytes());
    byte[] bytes = ByteStreams.toByteArray(clean_xml_is);
    clean_xml_is.close();
    String clean_xml = new String(bytes);
    assertEquals(clean_xml, validCharsXML);
  }
}