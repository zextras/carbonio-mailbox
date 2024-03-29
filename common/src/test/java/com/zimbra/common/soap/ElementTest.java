// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element.ElementFactory;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.util.ZimbraLog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.dom4j.DocumentException;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.python.google.common.base.Joiner;
import org.xml.sax.SAXException;

/**
 */
@Disabled("add required xml files to run")
public class ElementTest {

    
    public String testName;

  private static final Logger LOG = LogManager.getLogger(ElementTest.class);
    private static final int maxiter = 50000;

    // Using null because XML files missing and class init fails
    //TODO: use original toBais(ElementTest.class.getResourceAsStream("GetInfoResponseSOAP.xml"));
    private static final ByteArrayInputStream getInfoRespBais = null;
    //TODO: use original toBais(ElementTest.class.getResourceAsStream("GetInfoRequestSOAP.xml"));
    private static final ByteArrayInputStream getInfoReqBais = null;


    static {
    Configurator.setRootLevel(Level.INFO);
    Configurator.setLevel(LOG, Level.INFO);
  }

    private void logInfo(String format, Object ... objects) {
        if (LOG.isInfoEnabled()) {
            LOG.info("[[[  " + testName + "  ]]] :" + String.format(format, objects));
        }
    }

    private void logNewAndLegacyElements(Element elem, Element legacyElem) {
        logInfo("\n\nElement toString() value:\n%1$s\n\nLegacy Element toString() value:\n%2$s",
                elem.toString(), legacyElem.toString());
    }

  @Test
  void prettyPrintSafeXml() {
    prettyPrintSafe(new Element.XMLElement("dummy"));
  }

  @Test
  void prettyPrintSafeJson() {
    prettyPrintSafe(new Element.JSONElement("dummy"));
  }

    private void prettyPrintSafe(Element element) {
        element.addNonUniqueElement("password").addText("secret");
        element.addNonUniqueElement("pfxPassword").addText("secret");
        element.addNonUniqueElement("a").addAttribute("n", "pfxPassword").addText("secret");
        element.addNonUniqueElement("a").addAttribute("n", "hostPwd").addText("secret");
        element.addNonUniqueElement("a").addAttribute("n", "webexZimlet_pwd1").addText("secret");
        element.addNonUniqueElement("dummy2")
               .addAttribute("password", "secret")
               .addAttribute("pass", "secret")
               .addAttribute("pwd", "secret");
        element.addNonUniqueElement("prop").addAttribute("name", "passwd").addText("secret");
        String elementStr = element.prettyPrint(true);
        assertFalse(elementStr.contains("secret"), "Sensitive values have not been masked\n" + elementStr);
    }

  @Test
  void jsonNamespace() throws Exception {
    Element json = Element.parseJSON("{ \"purge\": [{}] }");
    assertEquals("urn:zimbraSoap", json.getNamespaceURI(""), "default toplevel namespace");

    json = Element.parseJSON("{ \"purge\": [{}], \"_jsns\": \"urn:zimbraMail\" }");
    assertEquals("urn:zimbraMail", json.getNamespaceURI(""), "explicit toplevel namespace");

    json = Element.parseJSON("{ \"purge\": [{}], foo: { a: 1, \"_jsns\": \"urn:zimbraMail\" } }");
    assertEquals("urn:zimbraMail", json.getElement("foo").getNamespaceURI(""), "explicit child namespace");
  }

  @Test
  void getPathElementList() {
    Element e = XMLElement.mFactory.createElement("parent");
    e.addNonUniqueElement("child");
    assertEquals(1, e.getPathElementList(new String[]{"child"}).size());
    assertEquals(0, e.getPathElementList(new String[]{"bogus"}).size());
  }

    private static final String xmlContainingMixed = "<a><b foo=\"bar\">doo<c/>wop</b></a>";

  @Test
  void flatten() throws Exception {
    Element a = Element.parseXML(xmlContainingMixed);
    assertEquals("a", a.getName(), "toplevel is <a>");
    assertEquals(0, a.listAttributes().size(), "<a> has no attrs");
    assertEquals(1, a.listElements().size(), "<a> has 1 child");

    Element b = a.listElements().get(0);
    assertEquals("b", b.getName(), "child is <b>");
    assertEquals(1, b.listAttributes().size(), "<b> has 1 attr");
    assertEquals("bar", b.getAttribute("foo"), "<b> attr foo=bar");
    assertEquals(0, b.listElements().size(), "<b> has no children");
    assertEquals("doo<c/>wop", b.getText(), "<b>'s contents are flattened");
  }

    /** Cache one DocumentFactory per thread to avoid unnecessarily recreating
     *  them for every XML parse. */
    private static final ThreadLocal<org.dom4j.DocumentFactory> mDocumentFactory =
            new ThreadLocal<org.dom4j.DocumentFactory>() {
        @Override
        protected synchronized org.dom4j.DocumentFactory initialValue() {
            return new org.dom4j.DocumentFactory();
        }
    };

    /**
     * Safer version of what used to be {@code Element.parseXML (InputStream is, ElementFactory factory)}
     * @throws SAXException
     * @throws XmlParseException
     */
    public static Element parseXMLusingDom4j(InputStream is, ElementFactory factory)
    throws org.dom4j.DocumentException, XmlParseException, SAXException {
        return Element.convertDOM(W3cDomUtil.getDom4jSAXReaderWhichUsesSecureProcessing(
                mDocumentFactory.get()).read(is).getRootElement(), factory);
    }

    private static String provisioningXMLTemplate =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "##DOCTYPE##" +
            "<ZCSImport>\n" +
            "<ImportUsers>\n" +
            "<User>\n" +
            "<sn>Test</sn>\n" +
            "<givenName>Account</givenName>\n" +
            "<displayName>Test Account##ENTITY_REF##</displayName>\n" +
            "<RemoteEmailAddress>taccount@example.com</RemoteEmailAddress>\n" +
            "<password>test123</password>\n" +
            "<zimbraPasswordMustChange>TRUE</zimbraPasswordMustChange>\n" +
            "</User>\n" +
            "</ImportUsers>\n" +
            "</ZCSImport>";

    private static String provisioningXMLWithNoEntityRefNoDocType =
            provisioningXMLTemplate.replace("##DOCTYPE##", "").replace("##ENTITY_REF##", "");

    private static String provisioningXMLWithEntityRefButNoDecl =
            provisioningXMLTemplate.replace("##DOCTYPE##", "").replace("##ENTITY_REF##", "&xxe;");

    private static String provisioningXMLWithEntityRefAndDecl =
            provisioningXMLTemplate.replace("##DOCTYPE##",
                                            "<!DOCTYPE foo [ \n" +
                                            "   <!ELEMENT foo ANY >\n" +
                                            "   <!ENTITY xxe SYSTEM \"file:///etc/hosts\" >]>\n")
                                   .replace("##ENTITY_REF##", "&xxe;");

  @Test
  void dom4jSAXReaderWhichUsesSecureProcessing()
      throws XmlParseException, SAXException, DocumentException {
    org.dom4j.io.SAXReader dom4jSAXReader;
    org.dom4j.Document doc;

    dom4jSAXReader = W3cDomUtil.getDom4jSAXReaderWhichUsesSecureProcessing();
    doc = dom4jSAXReader.read(
        new ByteArrayInputStream(provisioningXMLWithNoEntityRefNoDocType.getBytes(StandardCharsets.UTF_8)));
    assertNotNull(doc, "org.dom4j.Document object should not be null");
    assertEquals(provisioningXMLWithNoEntityRefNoDocType, doc.asXML(), "Round tripped XML");

    dom4jSAXReader = W3cDomUtil.getDom4jSAXReaderWhichUsesSecureProcessing();
    try {
      doc = dom4jSAXReader.read(
          new ByteArrayInputStream(provisioningXMLWithEntityRefButNoDecl.getBytes(StandardCharsets.UTF_8)));
      assertNotNull(doc, "org.dom4j.Document object should not be null");
      fail("DocumentException should have been thrown");
    } catch (DocumentException de) {
      assertTrue(de.getMessage().contains("The entity \"xxe\" was referenced, but not declared."));
    }

    dom4jSAXReader = W3cDomUtil.getDom4jSAXReaderWhichUsesSecureProcessing();
    try {
      doc = dom4jSAXReader.read(
          new ByteArrayInputStream(provisioningXMLWithEntityRefAndDecl.getBytes(StandardCharsets.UTF_8)));
    } catch (DocumentException de) {
      assertTrue(de.getMessage().contains("DOCTYPE is disallowed when the feature " +
          "\"http://apache.org/xml/features/disallow-doctype-decl\" set to true"));
    }
  }

    private static final String testXml =
            "<xml xmlns=\"urn:zimbra\">\n<a fred=\"woof y&lt;6\"></a>\n<b/><b/>\n<text>R &amp; B</text></xml>";

  @Test
  void parseTestXml() throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(testXml.getBytes());
    Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
    bais.reset();
    Element elem = Element.parseXML(bais);
    logNewAndLegacyElements(elem, legacyElem);
    assertEquals(legacyElem.toString(), elem.toString(), "toString value unchanged");
  }

    private static final String nsTestXml =
            "<xml xmlns=\"urn:zimbra\" xmlns:admin=\"urn:zimbraAdmin\"><e attr=\"aVal\">text</e><admin:b/></xml>";

  @Test
  void parseNamespaceTestXml() throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(nsTestXml.getBytes());
    Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
    bais.reset();
    Element elem = Element.parseXML(bais);
    logNewAndLegacyElements(elem, legacyElem);
    assertEquals(legacyElem.toString(), elem.toString(), "element toString value");
  }

    private static final String nsUnusedTestXml =
            "<xml xmlns=\"urn:zimbra\" xmlns:admin=\"urn:zimbraAdmin\"><e attr=\"aVal\">text</e></xml>";

  @Test
  void parseUnusedNamespaceTestXml() throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(nsUnusedTestXml.getBytes());
    Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
    bais.reset();
    Element elem = Element.parseXML(bais);
    logNewAndLegacyElements(elem, legacyElem);
    assertEquals(legacyElem.toString(), elem.toString(), "element toString value");
    elem = Element.parseXML(elem.toString());  // Testing that re-parse succeeds
  }

    private static final String[] getAcctReqXml = {
                "<ns7:GetAccountRequest",
                "   xmlns:ns7=\"urn:zimbraAdmin\"",
                "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
                "   xsi:type=\"ns7:getAccountRequest\"",
                "   applyCos=\"false\">",
                "  <ns7:account by=\"name\">user1@coco.local</ns7:account>",
                "</ns7:GetAccountRequest>"} ;

    private static final String parsedGetAcctReq = "<ns7:GetAccountRequest xsi:type=\"ns7:getAccountRequest\" " +
        "applyCos=\"false\" xmlns:ns7=\"urn:zimbraAdmin\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
        "<ns7:account by=\"name\">user1@coco.local</ns7:account></ns7:GetAccountRequest>";

  /**
   * Bug 81490 xmlns:xsi namespace getting lost, resulting in parse problems if "Element" based XML is re-parsed.
   */
  @Test
  void nonZimbraAttributeNamespaceHandling() throws Exception {
    String xmlString = Joiner.on("\n").join(getAcctReqXml);
    ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes());
    Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
    // Note: this is missing : xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance and is therefore corrupted.
    //       <ns7:GetAccountRequest applyCos="false" xsi:type="ns7:getAccountRequest" xmlns:ns7="urn:zimbraAdmin">
    //                <ns7:account by="name">user1@coco.local</ns7:account></ns7:GetAccountRequest>
    logInfo("Parsed to legacy element\n%s", legacyElem.toString());
    Element elem = Element.parseXML(xmlString);
    logInfo("Parsed to element\n%s", elem.toString());
    assertEquals(parsedGetAcctReq, elem.toString(), "elem toString value");
    elem = Element.parseXML(elem.toString());  // Testing that re-parse succeeds
  }

    // Test string that has namespace definition in the envelope that is only used in a descendant's name
    // and another namespace definition in the envelope that is only used in an attribute name in a descendant
    private static final String[] attrNSonTopLevelXml = {
        "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"",
        "               xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
        "               xmlns:fun=\"urn:fun\">",
        "    <soap:Body>",
        "        <ns7:GetAccountRequest xmlns:ns7=\"urn:zimbraAdmin\"",
        "                               xsi:type=\"ns7:getAccountRequest\" applyCos=\"false\">",
        "            <ns7:account by=\"name\">acct.that.exists@my.dom.loc</ns7:account>",
        "            <fun:invented/>",
        "        </ns7:GetAccountRequest>",
        "    </soap:Body>",
        "</soap:Envelope>"} ;

  /**
   * Bug 81620 xmlns:xsi namespace defined on Envelope element but only used on attribute of GetAccountRequest.
   *           xmlns:xsi namespace definition was getting lost when GetAccountRequest was detached
   */
  @Test
  void attribNamespaceLostIfDefinedHigher() throws Exception {
    String xmlString = Joiner.on("\n").join(attrNSonTopLevelXml);
    Element envelope = Element.parseXML(xmlString);
    logInfo("Envelope Parsed to element\n%s", envelope.toString());
    Element elem = envelope.getElement("Body").getElement("GetAccountRequest");
    logInfo("GetAccountRequest Element\n%s", elem.toString());
    elem.detach();
    logInfo("GetAccountRequest Element detached\n%s", elem.toString());
    Element.parseXML(envelope.toString());  // Testing that re-parse succeeds
  }

    private static final String brokenXml = "<xml xmlns=\"urn:zimbra\">\n<a fred=\"woof\"></a>\n<b/>\n</xmlbroken>";

  @Test
  void parseBrokenXmlUsingJAXP() {
    ByteArrayInputStream bais = new ByteArrayInputStream(brokenXml.getBytes());
    try {
      Element.parseXML(bais);
      fail("XmlParseException should have been thrown");
    } catch (Exception e) {
      // Expecting XmlParseException with mesage something like :
      //     "parse error: Fatal Error: Problem on line 4 of document :
      //         The end-tag for element type \"xml\" must end with a '>' delimiter.",
      assertTrue(e instanceof XmlParseException, "XmlParseException should have been thrown");
    }
  }

  /**
   * Validate that the new {@code Element.parseXML} produces the same Element structure as the old one
   * @throws SAXException
     */
  @Test
  void getInfoRequestSOAP()
      throws DocumentException, XmlParseException, SAXException {
    getInfoReqBais.reset();
    Element legacyElem = parseXMLusingDom4j(getInfoReqBais, Element.XMLElement.mFactory);
    getInfoReqBais.reset();
    Element elem = Element.parseXML(getInfoReqBais);
    logNewAndLegacyElements(elem, legacyElem);
    assertEquals(legacyElem.toString(), elem.toString(), "toString value unchanged");
  }

  /**
   * Validate that the new {@code Element.parseXML} produces the same Element structure as the old one
   * @throws SAXException
     */
  @Test
  void getInfoResponseSOAP()
      throws DocumentException, XmlParseException, SAXException {
    getInfoRespBais.reset();
    Element legacyElem = parseXMLusingDom4j(getInfoRespBais, Element.XMLElement.mFactory);
    getInfoRespBais.reset();
    Element elem = Element.parseXML(getInfoRespBais);
    logNewAndLegacyElements(elem, legacyElem);
    assertEquals(legacyElem.toString(), elem.toString(), "toString value unchanged");
  }

  @Test
  void parseTestXmlFromFile()
      throws URISyntaxException, DocumentException, FileNotFoundException, ServiceException, SAXException {
    getInfoRespBais.reset();
    Element legacyElem = parseXMLusingDom4j(getInfoRespBais, Element.XMLElement.mFactory);
    URL xmlUrl = ElementTest.class.getResource("GetInfoResponseSOAP.xml");
    File xmlFile = new File(xmlUrl.toURI());
    Element elem = W3cDomUtil.parseXML(xmlFile);
    logNewAndLegacyElements(elem, legacyElem);
    assertEquals(legacyElem.toString(), elem.toString(), "toString value unchanged");
  }

    private static String xhtmlString =
            "<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" " +
                    "xmlns:xml=\"http://www.w3.org/XML/1998/namespace\">\n" +
            "&lt;head xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "    &lt;title>Fun&lt;/title>\n" +
            "&lt;/head>\n" +
            "&lt;body xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "&lt;h1>Header 1&lt;/h1>\n" +
            "Simple test\n" +
            "&lt;/body>\n" +
            "</html>";

  /**
   * Validate that the new {@code Element.parseXML} produces the same Element structure as the old one
   * when the input text is XHTML
   * @throws SAXException
     */
  @Test
  void xhtml()
      throws DocumentException, XmlParseException, SAXException {
    ByteArrayInputStream bais = toBais(ElementTest.class.getResourceAsStream("xhtml.html"));
    Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
    bais.reset();
    Element elem = Element.parseXML(bais);
    logNewAndLegacyElements(elem, legacyElem);
    /* The file xhtml.html does not contain the namespace definition for "xml" in here.  The fix for
     * bug 81620 introduces it.  If xhtml.html with this modification made is pasted into
     * http://validator.w3.org/check it passes with 3 warnings, none of which relate to the added definition
     */
    assertEquals(xhtmlString, elem.toString(), "toString value");

    assertEquals("html", elem.getName(), "top node name");
    assertEquals(0, elem.listElements().size(), "Number of sub elements");

    assertEquals("html", legacyElem.getName(), "Legacy top node name");
    assertEquals(0, legacyElem.listElements().size(), "Legacy Number of sub elements");
  }

  /**
   * Validate that the new {@code Element.parseXML} produces the same Element structure as the old one
   * when the input text contains a sub-element which is xhtml and that in turn contains CDATA
   *
   * Note: Original test data contained attributes xml:lang="en" and lang="en" but the new code and
   * the old code swapped the order of the attributes.  This should not make any effective difference
   * and DOM does not make any guarantees with regard to attribute order, so xml:lang attribute removed to keep test
   * for success simple.
   */
  @Test
  void wrappedXhtmlWithCdata()
      throws DocumentException, XmlParseException {
    ByteArrayInputStream bais = toBais(ElementTest.class.getResourceAsStream("wrappedXhtml.xml"));
    Element elem = Element.parseXML(bais);
    logInfo("\n\nElement toString() value:\n%1$s", elem.toString());

    assertEquals("xml", elem.getName(), "top node name");
    assertEquals(2, elem.listElements().size(), "Number of sub elements");
    assertEquals(1, elem.listElements("fun").size(), "Number of 'fun' sub elements");
    assertEquals(1, elem.listElements("wrapper").size(), "Number of 'wrapper' sub elements");
    Element wrapperElem = elem.listElements("wrapper").get(0);
    assertEquals(1, wrapperElem.listElements().size(), "Number of sub elements for wrapper");
    // top level element of xhtml is an Element
    Element htmlElem = wrapperElem.listElements("html").get(0);
    // Zero sub-elements - i.e. all children flattened into the text
    assertEquals(0, htmlElem.listElements().size(), "Number of sub elements for html");
  }

    /**
     * Validate that the new {@code Element.parseXML} produces the same Element structure as the old one
     * when the input text contains a sub-element which is xhtml and that in turn contains CDATA
     *
     * Note: Original test data contained attributes xml:lang="en" and lang="en" but the new code and
     * the old code swapped the order of the attributes.  This should not make any effective difference
     * and DOM does not make any guarantees with regard to attribute order, so xml:lang attribute removed to keep test
     * for success simple.
     * @throws SAXException
     * @throws XmlParseException
     */
    // Enable for comparison @Test
    public void legacyWrappedXhtmlWithCdata()
    throws DocumentException, XmlParseException, SAXException {
        ByteArrayInputStream bais = toBais(ElementTest.class.getResourceAsStream("wrappedXhtml.xml"));
        Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
        assertEquals("xml", legacyElem.getName(), "Legacy top node name");
        assertEquals(2, legacyElem.listElements().size(), "Legacy Number of sub elements");
        assertEquals(1, legacyElem.listElements("fun").size(), "Legacy Number of 'fun' sub elements");
        assertEquals(1, legacyElem.listElements("wrapper").size(), "Legacy Number of 'wrapper' sub elements");
        Element lWrapperElem = legacyElem.listElements("wrapper").get(0);
        // Zero sub-elements - i.e. all children flattened into the text - Compare with newer code which goes
        // one level deeper
        assertEquals(0, lWrapperElem.listElements().size(), "Legacy Number of sub elements for wrapper");
    }

    private static final String wrappedXhtmlSingleElem = "<xml>\n" +
                                "<fun/>\n" +
                                "<wrapper>\n" +
                                "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">" +
                                "<body></body>" +
                                "</html>\n" +
                                "</wrapper>\n" +
                                "<work/>\n" +
                                "</xml>\n";

  /**
   * input text contains xhtml which only has one sub-element - i.e. it isn't mixed text
   * @throws SAXException
     */
  @Test
  void wrappedXhtmlWithSingleElem()
      throws DocumentException, XmlParseException, SAXException {
    logInfo("Source TEXT:\n%1$s", wrappedXhtmlSingleElem);
    ByteArrayInputStream bais = new ByteArrayInputStream(wrappedXhtmlSingleElem.getBytes());
    Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
    bais.reset();
    Element elem = Element.parseXML(bais);
    logNewAndLegacyElements(elem, legacyElem);

    assertEquals("xml", elem.getName(), "top node name");
    assertEquals(3, elem.listElements().size(), "Number of sub elements");
    assertEquals(1, elem.listElements("fun").size(), "Number of 'fun' sub elements");
    assertEquals(1, elem.listElements("wrapper").size(), "Number of 'wrapper' sub elements");
    assertEquals(1, elem.listElements("work").size(), "Number of 'work' sub elements");
    Element wrapperElem = elem.listElements("wrapper").get(0);
    assertEquals(1, wrapperElem.listElements().size(), "Number of sub elements for wrapper");
    // top level element of xhtml is an Element
    Element htmlElem = wrapperElem.listElements("html").get(0);
    // Zero sub-elements - i.e. all children flattened into the text
    assertEquals(0, htmlElem.listElements().size(), "Number of sub elements for html");
  }

    /**
     * Old behavior when the input text contains xhtml which only has one sub-element - i.e. it isn't mixed text
     * @throws SAXException
     * @throws XmlParseException
     */
    // Enable for comparison @Test
    public void legacyWrappedXhtmlWithSingleElem()
    throws DocumentException, XmlParseException, SAXException {
        logInfo("Source TEXT:\n%1$s", wrappedXhtmlSingleElem);
        ByteArrayInputStream bais = new ByteArrayInputStream(wrappedXhtmlSingleElem.getBytes());
        Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);

        assertEquals("xml", legacyElem.getName(), "top node name");
        assertEquals(3, legacyElem.listElements().size(), "Number of sub elements");
        assertEquals(1, legacyElem.listElements("fun").size(), "Number of 'fun' sub elements");
        assertEquals(1, legacyElem.listElements("wrapper").size(), "Number of 'wrapper' sub elements");
        assertEquals(1, legacyElem.listElements("work").size(), "Number of 'work' sub elements");
        Element lWrapperElem = legacyElem.listElements("wrapper").get(0);
        // Zero sub-elements - i.e. all children flattened into the text - Compare with newer code which goes
        // one level deeper
        assertEquals(0, lWrapperElem.listElements().size(), "Number of sub elements for wrapper");
    }

    private static final String wrappedXhtmlTextContentOnly = "<xml>\n" +
                                "<fun/>\n" +
                                "<wrapper>\n" +
                                "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">" +
                                "Text only" +
                                "</html>\n" +
                                "</wrapper>\n" +
                                "</xml>\n";

  /**
   * Validate that the new {@code Element.parseXML} produces the same Element structure as the old one
   * when the input text contains xhtml which only contains text
   * @throws SAXException
     */
  @Test
  void wrappedXhtmlWithTextContentOnly()
      throws DocumentException, XmlParseException, SAXException {
    logInfo("Source TEXT:\n%1$s", wrappedXhtmlTextContentOnly);
    ByteArrayInputStream bais = new ByteArrayInputStream(wrappedXhtmlTextContentOnly.getBytes());
    Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
    bais.reset();
    Element elem = Element.parseXML(bais);
    logNewAndLegacyElements(elem, legacyElem);
    assertEquals(legacyElem.toString(), elem.toString(), "toString value unchanged");

    assertEquals("xml", elem.getName(), "top node name");
    assertEquals(2, elem.listElements().size(), "Number of sub elements");
    assertEquals(1, elem.listElements("fun").size(), "Number of 'fun' sub elements");
    assertEquals(1, elem.listElements("wrapper").size(), "Number of 'wrapper' sub elements");
    Element wrapperElem = elem.listElements("wrapper").get(0);
    // XHTML contains only text, so we treat it as an element
    assertEquals(1, wrapperElem.listElements().size(), "Number of sub elements for wrapper");

    assertEquals("xml", legacyElem.getName(), "top node name");
    assertEquals(2, legacyElem.listElements().size(), "Number of sub elements");
    assertEquals(1, legacyElem.listElements("fun").size(), "Number of 'fun' sub elements");
    assertEquals(1, legacyElem.listElements("wrapper").size(), "Number of 'wrapper' sub elements");
    Element lWrapperElem = legacyElem.listElements("wrapper").get(0);
    // XHTML contains only text, so we treat it as an element
    assertEquals(1, lWrapperElem.listElements().size(), "Number of sub elements for wrapper");
  }

  /**
   * Validate entity references are not expanded. (security issue)
   */
  @Test
  void parseXmlWithEntityReference() {
    ByteArrayInputStream bais = toBais(ElementTest.class.getResourceAsStream("entityRef.xml"));
    Element elem;
    try {
      elem = Element.parseXML(bais);
      // Expect :    <root>&lt;i/>text</root>
      logInfo("       Element value:\n%1$s", elem.toString());
      assertEquals("root", elem.getName(), "root elem name");
      // Assert.assertEquals("root elem content", "<i/>text", elem.getText());  // this is the case if entity ref expansion was allowed
      assertEquals("", elem.getText(), "root elem content");
    } catch (XmlParseException e) {
      if (-1 == e.getMessage().indexOf("Document parse failed")) {
        fail("Unexpected exception thrown." + e.getMessage());
      }
    } finally {
      Closeables.closeQuietly(bais);
    }
  }

  /**
   * Validate entity references are not evaluated. (security issue)
   */
  @Test
  void parseXmlWithEntityReferenceToNonExistentFile() {
    ByteArrayInputStream bais = toBais(ElementTest.class.getResourceAsStream("refNonExistentFileInEntity.xml"));
    try {
      Element elem = Element.parseXML(bais);
      logInfo("       Element value:\n%1$s", elem.toString());
      fail("Should have failed - 'DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.");
    } catch (XmlParseException e) {
      // Before fix to Bug 79719 would get an error like:
      //    parse error: /tmp/not/there/non-existent.xml (No such file or directory)
      if (-1 == e.getMessage().indexOf("Document parse failed")) {
        fail("Unexpected exception thrown." + e.getMessage());
      }
    } finally {
      Closeables.closeQuietly(bais);
    }
  }

  /**
   * Validate that entity expansion is not taken to extreme. (denial of service security issue)
   */
  @Test
  void parseXmlWithExcessiveEntityExpansion() {
    ByteArrayInputStream bais = toBais(ElementTest.class.getResourceAsStream("recursiveEntity.xml"));
    try {
      Element.parseXML(bais);
      fail("Should have failed as there are too many recursive entity expansions");
    } catch (XmlParseException e) {
      // Should be an error like:
      //    parse error: Fatal Error: Problem on line 19 of document : The parser has encountered more than
      //    "100,000" entity expansions in this document; this is the limit imposed by the application.
    } finally {
      Closeables.closeQuietly(bais);
    }
  }

  /**
   * Exercise different ways of adding something to Element and how it affects the resulting XML or JSON
   * ZimbraSoap has other tests which explore the idiosyncrasies of our SOAP API.
   */
  @Test
  void elementsAndAttributes() {
    Element e = XMLElement.mFactory.createElement("parent");
    e.addAttribute(MailConstants.A_SUBJECT, "subject", Element.Disposition.CONTENT);
    assertEquals("<parent><su>subject</su></parent>", e.toString(), "XML with content attribute");
    e = Element.JSONElement.mFactory.createElement("parent");
    e.addAttribute(MailConstants.A_SUBJECT, "subject", Element.Disposition.CONTENT);
    assertEquals("{\"su\":\"subject\"}", e.toString(), "JSON with content attribute");
    e = XMLElement.mFactory.createElement("parent");
    e.addAttribute(MailConstants.A_SUBJECT, "subject");
    assertEquals("<parent su=\"subject\"/>", e.toString(), "XML with normal attribute");
    e = Element.JSONElement.mFactory.createElement("parent");
    e.addAttribute(MailConstants.A_SUBJECT, "subject");
    assertEquals("{\"su\":\"subject\"}", e.toString(), "JSON with normal attribute");
    e = XMLElement.mFactory.createElement("parent");
    e.addUniqueElement(MailConstants.A_SUBJECT).addText("subject");
    assertEquals("<parent><su>subject</su></parent>", e.toString(), "XML with unique element");
    e = Element.JSONElement.mFactory.createElement("parent");
    e.addUniqueElement(MailConstants.A_SUBJECT).addText("subject");
    assertEquals("{\"su\":{\"_content\":\"subject\"}}", e.toString(), "JSON with unique element");
    e = XMLElement.mFactory.createElement("parent");
    e.addNonUniqueElement(MailConstants.A_SUBJECT).addText("subject");
    assertEquals("<parent><su>subject</su></parent>", e.toString(), "XML with non-unique element");
    e = Element.JSONElement.mFactory.createElement("parent");
    e.addNonUniqueElement(MailConstants.A_SUBJECT).addText("subject");
    assertEquals("{\"su\":[{\"_content\":\"subject\"}]}", e.toString(), "JSON with non-unique element");
  }

  @Test
  void reorderChildren() {
    final String expected =
        "<top attr=\"val\"><z/><a>1</a><a>2</a><b/><c/><d><kid/></d><f>W</f><h2/><h3/><h4/><j/><p/><q/></top>";
    Element top = new Element.XMLElement("top");
    top.addAttribute("attr", "val");
    top.addNonUniqueElement("b");
    top.addNonUniqueElement("c");
    top.addNonUniqueElement("p");
    top.addNonUniqueElement("f").addText("W");
    top.addNonUniqueElement("a").addText("1");
    top.addNonUniqueElement("z");
    top.addNonUniqueElement("j");
    top.addNonUniqueElement("a").addText("2");
    top.addNonUniqueElement("h2");
    top.addNonUniqueElement("h3");
    top.addNonUniqueElement("h4");
    top.addNonUniqueElement("d").addNonUniqueElement("kid");
    top.addNonUniqueElement("q");
    List<List<QName>> order = Lists.newArrayList();
    order.add(Lists.newArrayList(new QName("z")));
    order.add(Lists.newArrayList(new QName("a")));
    order.add(Lists.newArrayList(new QName("g")));
    order.add(Lists.newArrayList(new QName("b")));
    order.add(Lists.newArrayList(new QName("c")));
    order.add(Lists.newArrayList(new QName("d")));
    order.add(Lists.newArrayList(new QName("e")));
    order.add(Lists.newArrayList(new QName("f")));
    order.add(Lists.newArrayList(new QName("h4"), new QName("h3"), new QName("h2"), new QName("h1")));
    order.add(Lists.newArrayList(new QName("i")));
    order.add(Lists.newArrayList(new QName("j")));
    order.add(Lists.newArrayList(new QName("k")));
    top = Element.reorderChildElements(top, order);
    logInfo("       Element value:\n%1$s", top.toString());
    assertEquals(expected, top.toString(), "Reordered element");
  }

    private static final String xmlCdata = "<xml>\n" +
            "<cdatawrap><![CDATA[if (a>b) a++;\n]]>\n</cdatawrap>\n" +
            "<elem/>\n" +
            "</xml>";

  /**
   * Validate that the new {@code Element.parseXML} produces the same Element structure as the old one
   * when the input text contains CDATA
   * @throws SAXException
     */
  @Test
  void cdata()
      throws DocumentException, XmlParseException, SAXException {
    ByteArrayInputStream bais = new ByteArrayInputStream(xmlCdata.getBytes());
    Element legacyElem = parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
    bais.reset();
    Element elem = Element.parseXML(bais);
    logNewAndLegacyElements(elem, legacyElem);
    assertEquals(legacyElem.toString(), elem.toString(), "toString value unchanged");
  }

  /**
   * Validates that XML has valid set of characters.
   * Supported Character range in XML : #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
   * Surrogate characters 0xFFFE and 0xFFFF are not supported.
   */
  @Test
  void checkForValidCharactersInXML() {
    //Greek small letter Beta
    assertTrue(XMLElement.isValidXmlCharacter('\u03B2'));

    //Cyrillic Capital Letter Zhe
    assertTrue(XMLElement.isValidXmlCharacter('\u0416'));

    //Japanese KATAKANA LETTER GA
    assertTrue(XMLElement.isValidXmlCharacter('\u30AC'));

    //Following are some examples of Invalid characters in XML
    assertFalse(XMLElement.isValidXmlCharacter('\u001A'));
    assertFalse(XMLElement.isValidXmlCharacter('\u001B'));
    assertFalse(XMLElement.isValidXmlCharacter('\u000C'));
    assertFalse(XMLElement.isValidXmlCharacter('\uFFFF'));
    assertFalse(XMLElement.isValidXmlCharacter('\uFFFE'));
  }

  @Test
  void checkForUnicodeSupplementaryChars() {
    //Unicode Han Character 'U+2000B' ("\uD840\uDC0B")
    String str = "\uD840\uDC0B";
    assertTrue(XMLElement.isSupplementaryCharacter(str.charAt(0), str.charAt(1)));
  }

    /** Ensure that we can reset the input stream */
    private static ByteArrayInputStream toBais(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int cnt;
        byte[] data = new byte[2048];
        try {
            while ((cnt = is.read(data, 0, data.length)) != -1) {
              buffer.write(data, 0, cnt);
            }
            buffer.flush();
        } catch (IOException e) {
            return null;
        }
        return new ByteArrayInputStream(buffer.toByteArray());
    }

    /*   Speed tests */
    // Enable for performance comparison @Test
    public void iterNamespaceTest_OLD() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(nsTestXml.getBytes());
        for (int i = 0;i< maxiter; i++) {
            bais.reset();
            parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
        }
    }

    // Enable for performance comparison @Test
    public void iterNamespaceTest_NEW() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(nsTestXml.getBytes());
        for (int i = 0;i< maxiter; i++) {
            bais.reset();
            Element.parseXML(bais);
        }
    }

    // Enable for performance comparison @Test
    public void iterGetInfoResponseSOAP_OLD()
    throws DocumentException, SAXException, IOException, XmlParseException {
        for (int i = 0;i< maxiter; i++) {
            getInfoRespBais.reset();
            parseXMLusingDom4j(getInfoRespBais, Element.XMLElement.mFactory);
        }
    }

    // Enable for performance comparison @Test
    public void iterGetInfoResponseSOAP_NEW()
    throws XmlParseException {
        for (int i = 0;i< maxiter; i++) {
            getInfoRespBais.reset();
            Element.parseXML(getInfoRespBais);
        }
    }

    // Enable for performance comparison @Test
    public void iterFlatten_OLD() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlContainingMixed.getBytes());
        for (int i = 0;i< maxiter; i++) {
            bais.reset();
            parseXMLusingDom4j(bais, Element.XMLElement.mFactory);
        }
    }

    // Enable for performance comparison @Test
    public void iterFlatten_NEW() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlContainingMixed.getBytes());
        for (int i = 0;i< maxiter; i++) {
            bais.reset();
            Element.parseXML(bais);
        }
    }

    /*
     * Bug 103996.  Replaced code which used to do XML parsing using a mechanism similar to
     * how oldUnsafeSAXParsingPerformance does it (some code used a slightly safer variant of SAXReader
     * which used to be in Element) Most new code uses code similar to
     * parseXMLToDom4jDocUsingSecureProcessingPerformance, which is significantly faster (because was doing
     * SAX parsing when creating a DOM - using the info that the target is a DOM clearly allows for faster
     * processing) SoapHttpTransport.SAXResponseHandler is the only exception, which now uses code more
     * similar to dom4jSAXReaderWhichUsesSecureProcessingPerformance.  This appears to only be used in
     * ZimbraOffline.  The code is slower here (although other runs had a smaller gap in difference).
     * Submitted Bugzilla Bug 104175 suggesting investigating replacing use of this code with more modern,
     * higher performing code.
     * Run times:
     *     dom4jSAXReaderWhichUsesSecureProcessingPerformance #ELAPSED_TIME=42897ms (15:39:47.656-15:40:30.553)
     *     oldUnsafeSAXParsingPerformance #ELAPSED_TIME=25847ms (15:40:30.554-15:40:56.401)
     *     parseXMLToDom4jDocUsingSecureProcessingPerformance #ELAPSED_TIME=1843ms (15:40:56.403-15:40:58.246)
     */
    // Enable for performance comparison @Test
    public void dom4jSAXReaderWhichUsesSecureProcessingPerformance()
    throws XmlParseException, SAXException, DocumentException {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                provisioningXMLWithNoEntityRefNoDocType.getBytes(StandardCharsets.UTF_8));
        org.dom4j.io.SAXReader dom4jSAXReader;
        org.dom4j.Document doc = null;
        long start = System.currentTimeMillis();
        for (int i = 0;i< maxiter; i++) {
            bais.reset();
            dom4jSAXReader = W3cDomUtil.getDom4jSAXReaderWhichUsesSecureProcessing();
            doc = dom4jSAXReader.read(bais);
            assertEquals(provisioningXMLWithNoEntityRefNoDocType, doc.asXML(), "Round tripped XML");
        }
        ZimbraLog.test.info("dom4jSAXReaderWhichUsesSecureProcessingPerformance %s",
                ZimbraLog.elapsedTime(start, System.currentTimeMillis()));
    }

    // Enable for performance comparison @Test
    public void oldUnsafeSAXParsingPerformance() throws XmlParseException, SAXException, DocumentException {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                provisioningXMLWithNoEntityRefNoDocType.getBytes(StandardCharsets.UTF_8));
        org.dom4j.Document doc;
        long start = System.currentTimeMillis();
        for (int i = 0;i< maxiter; i++) {
            bais.reset();
            SAXReader reader = new SAXReader();
            doc = reader.read(bais);
            assertEquals(provisioningXMLWithNoEntityRefNoDocType, doc.asXML(), "Round tripped XML");
        }
        ZimbraLog.test.info("oldUnsafeSAXParsingPerformance %s",
                ZimbraLog.elapsedTime(start, System.currentTimeMillis()));
    }

    // Enable for performance comparison @Test
    public void parseXMLToDom4jDocUsingSecureProcessingPerformance()
    throws XmlParseException, SAXException, DocumentException {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                provisioningXMLWithNoEntityRefNoDocType.getBytes(StandardCharsets.UTF_8));
        org.dom4j.Document doc = null;
        long start = System.currentTimeMillis();
        for (int i = 0;i< maxiter; i++) {
            bais.reset();
            doc = W3cDomUtil.parseXMLToDom4jDocUsingSecureProcessing(bais);
            assertEquals(provisioningXMLWithNoEntityRefNoDocType, doc.asXML(), "Round tripped XML");
        }
        ZimbraLog.test.info("parseXMLToDom4jDocUsingSecureProcessingPerformance %s",
                ZimbraLog.elapsedTime(start, System.currentTimeMillis()));
    }

  @BeforeEach
  public void setup(TestInfo testInfo) {
    Optional<Method> testMethod = testInfo.getTestMethod();
    if (testMethod.isPresent()) {
      this.testName = testMethod.get().getName();
    }
  }

}
