// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.QName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.HeaderConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapParseException;
import com.zimbra.common.soap.W3cDomUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.AuthRequest;
import com.zimbra.soap.account.message.CreateIdentityRequest;
import com.zimbra.soap.account.message.GetInfoResponse;
import com.zimbra.soap.account.message.GetWhiteBlackListResponse;
import com.zimbra.soap.account.message.ModifyPrefsRequest;
import com.zimbra.soap.account.type.Pref;
import com.zimbra.soap.account.type.Session;
import com.zimbra.soap.admin.message.CreateAccountRequest;
import com.zimbra.soap.admin.message.CreateXMbxSearchRequest;
import com.zimbra.soap.admin.message.MailQueueActionRequest;
import com.zimbra.soap.admin.message.SearchAutoProvDirectoryResponse;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.admin.type.AutoProvDirectoryEntry;
import com.zimbra.soap.admin.type.MailQueueAction;
import com.zimbra.soap.admin.type.MailQueueWithAction;
import com.zimbra.soap.admin.type.QueueQuery;
import com.zimbra.soap.admin.type.QueueQueryField;
import com.zimbra.soap.admin.type.ServerWithQueueAction;
import com.zimbra.soap.admin.type.ValueAttrib;
import com.zimbra.soap.json.JacksonUtil;
import com.zimbra.soap.mail.message.ConvActionRequest;
import com.zimbra.soap.mail.message.DeleteDataSourceRequest;
import com.zimbra.soap.mail.message.GetContactsRequest;
import com.zimbra.soap.mail.message.ImportContactsRequest;
import com.zimbra.soap.mail.message.SearchConvRequest;
import com.zimbra.soap.mail.message.WaitSetRequest;
import com.zimbra.soap.mail.type.ConvActionSelector;
import com.zimbra.soap.mail.type.ImapDataSourceNameOrId;
import com.zimbra.soap.mail.type.MessageHitInfo;
import com.zimbra.soap.mail.type.ModifyGroupMemberOperation;
import com.zimbra.soap.mail.type.Pop3DataSourceNameOrId;
import com.zimbra.soap.mail.type.RetentionPolicy;
import com.zimbra.soap.type.KeyValuePair;
import com.zimbra.soap.type.WaitSetAddSpec;
import com.zimbra.soap.type.WantRecipsSetting;
import com.zimbra.soap.util.JaxbElementInfo;
import com.zimbra.soap.util.JaxbInfo;
import com.zimbra.soap.util.JaxbNodeInfo;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/**
 * Unit test for {@link GetInfoResponse} which exercises
 * translation to and from Element
 *
 * @author Gren Elliot
 */
@Disabled("add required xml files to run")
public class JaxbToElementTest {
    private static String getInfoResponseXMLfileName = "GetInfoResponse.xml";
    private static Unmarshaller unmarshaller;
    // one run with iterationNum = 80000:
    //     elementToJaxbTest time="30.013" (using w3c dom document)
    //     elementToJaxbUsingDom4jTest time="41.165"
    //     elementToJaxbUsingByteArrayTest time="122.265"
    private static int iterationNum = 2;
    static GetInfoResponse getInfoRespJaxb;
    static String getInfoResponseXml;
    static String getInfoResponseJSON;
    static String getInfoResponseJSONwithEnv;
    static Element getInfoRespElem;

    static {
    }

    public static String streamToString(InputStream stream, Charset cs)
    throws IOException {
        try {
            Reader reader = new BufferedReader(
                    new InputStreamReader(stream, cs));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } finally {
            stream.close();
        }
    }

    public static Unmarshaller getGetInfoResponseUnmarshaller() throws JAXBException {
        if (unmarshaller == null) {
            JAXBContext jaxb = JAXBContext.newInstance(GetInfoResponse.class);
            unmarshaller = jaxb.createUnmarshaller();
        }
        return unmarshaller;
    }
    public static GetInfoResponse getInfoResponsefromXml() throws JAXBException {
        if (getInfoRespJaxb == null) {
            getGetInfoResponseUnmarshaller();
            getInfoRespJaxb = (GetInfoResponse) unmarshaller.unmarshal(
                JaxbToElementTest.class.getResourceAsStream(getInfoResponseXMLfileName));
        }
        return getInfoRespJaxb;
    }

    /**
     * Bored with re-removing license block from test comparison XML - so make test tolerant to it.
     */
    public static String stripXmlCommentsOut(String str) throws IOException {
        int commentIndex = str.indexOf("<!--");
        if (commentIndex == -1) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, commentIndex));
        str = str.substring(commentIndex + 4);
        int endCommentIndex = str.indexOf("-->");
        if (endCommentIndex != -1) {
            sb.append(str.substring(endCommentIndex + 4));
        }
        return stripXmlCommentsOut(sb.toString());
    }

    public static String getTestInfoResponseXml() throws IOException {
        if (getInfoResponseXml == null) {
            try (InputStream is = JaxbToElementTest.class.getResourceAsStream(getInfoResponseXMLfileName)) {
                getInfoResponseXml = streamToString(is, Charsets.UTF_8);
            }
            getInfoResponseXml = stripXmlCommentsOut(getInfoResponseXml);
        }
        return getInfoResponseXml;
    }

    public static String getTestInfoResponseJson() throws IOException {
        if (getInfoResponseJSON == null) {
            try (InputStream is = JaxbToElementTest.class.getResourceAsStream("GetInfoResponse.json")) {
                getInfoResponseJSON = streamToString(is, Charsets.UTF_8).trim();
            }
        }
        return getInfoResponseJSON;
    }

    @BeforeAll
    public static void init() throws Exception {
        getInfoResponsefromXml();
        getTestInfoResponseXml();
        getTestInfoResponseJson();
        StringBuffer sb = new StringBuffer();
        sb.append("{\n\"GetInfoResponse\": ").append(getInfoResponseJSON).append("\n}");
        getInfoResponseJSONwithEnv = sb.toString();
        getInfoRespElem = JaxbUtil.jaxbToElement(getInfoRespJaxb);
    }

  @Test
  void jaxBToElementTest() throws Exception {
    for (int cnt = 1;cnt <= iterationNum;cnt++) {
      Element el = JaxbUtil.jaxbToElement(getInfoRespJaxb);
      String actual = el.prettyPrint();
      String expected = getInfoResponseXml;
      Diff myDiff = DiffBuilder.compare(expected).withTest(actual).build();
      Iterable<Difference> allDifferences = myDiff.getDifferences();
      allDifferences.forEach(
          difference -> ZimbraLog.test.info("Difference:%s [%s]", difference));
      assertFalse(myDiff.hasDifferences());
    }
  }

    private void validateLongString(String message,
                String expected, String actual,
                String expectedFile, String actualFile) {
        if (!actual.equals(expected)) {
            try{
                OutputStreamWriter out = new OutputStreamWriter( new FileOutputStream(actualFile),"UTF-8");
                out.write(actual);
                out.close();
            }catch (Exception e){//Catch exception if any
              ZimbraLog.test.error("validateLongString:Error writing to %s", actualFile, e);
            }
            fail(message + "\nexpected=" + expectedFile + "\nactual=" + actualFile);
        }
    }

  @Test
  void jaxBToJSONElementTest() throws Exception {
    Element el = JaxbUtil.jaxbToElement(getInfoRespJaxb, JSONElement.mFactory);
    // el.toString() and el.prettyPrint() don't provide the
    // name of the element - that only happens when it is a
    // child of other elements (the "soap" envelop)
    String actual = el.prettyPrint();
    assertEquals("GetInfoResponse", el.getName(), "Top level Element name");
    validateLongString("JSON response differs from expected\n", getInfoResponseJSON, actual,
        "GetInfoResponse.json", "/tmp/GetInfoResponse.json");
  }

  @Test
  void elementToJaxbTest() throws Exception {
    Element el = JaxbUtil.jaxbToElement(getInfoRespJaxb);
    org.w3c.dom.Document doc = el.toW3cDom();
    ZimbraLog.test.debug("(XML)elementToJaxbTest toW3cDom() Xml:\n%s", W3cDomUtil.asXML(doc));
    for (int cnt = 1;cnt <= iterationNum;cnt++) {
      GetInfoResponse getInfoResp = JaxbUtil.elementToJaxb(getInfoRespElem);
      assertEquals("user1@tarka.local", getInfoResp.getAccountName(), "Account name");
    }
  }

    private static String searchConvJson =
        "{\n" +
        "    \"SearchConvRequest\": {\n" +
        "      \"includeTagDeleted\": false,\n" +
        "      \"calExpandInstStart\": -1,\n" +
        "      \"calExpandInstEnd\": -1,\n" +
        "      \"query\": \"(((INID:\\\"9bce02cf-9d9a-4d15-b95e-e1495b75770e:2\\\") CONV:\\\"9bce02cf-9d9a-4d15-b95e-e1495b75770e:363\\\" ))\",\n" +
        "      \"types\": \"message\",\n" +
        "      \"sortBy\": \"dateDesc\",\n" +
        "      \"fetch\": \"9bce02cf-9d9a-4d15-b95e-e1495b75770e:446\",\n" +
        "      \"read\": true,\n" +
        "      \"max\": 250000,\n" +
        "      \"html\": true,\n" +
        "      \"neuter\": true,\n" +
        /* "      \"recip\": false,\n" + */
        "%%VALUE%%" +
        "      \"locale\": [{\n" +
        "          \"_content\": \"en_US\"\n" +
        "        }],\n" +
        "      \"prefetch\": true,\n" +
        "      \"resultMode\": \"NORMAL\",\n" +
        "      \"estimateSize\": false,\n" +
        "      \"field\": \"content:\",\n" +
        "      \"limit\": 250,\n" +
        "      \"offset\": 0,\n" +
        "      \"inDumpster\": false,\n" +
        "      \"nest\": false,\n" +
        "      \"cid\": \"9bce02cf-9d9a-4d15-b95e-e1495b75770e:363\",\n" +
        "      \"_jsns\": \"urn:zimbraMail\"\n" +
        "    }\n" +
        "}\n";

    private static String searchConvXml =
        "<SearchConvRequest\n" +
        "        includeTagDeleted=\"false\"\n" +
        "        calExpandInstStart=\"-1\"\n" +
        "        calExpandInstEnd=\"-1\"\n" +
        "        types=\"message\"\n" +
        "        sortBy=\"dateDesc\"\n" +
        "        fetch=\"9bce02cf-9d9a-4d15-b95e-e1495b75770e:446\"\n" +
        "        read=\"true\"\n" +
        "        max=\"250000\"\n" +
        "        html=\"true\"\n" +
        "        neuter=\"true\"\n" +
        "%%VALUE%%" +
        "        prefetch=\"true\"\n" +
        "        resultMode=\"NORMAL\"\n" +
        "        field=\"content:\"\n" +
        "        limit=\"250\"\n" +
        "        offset=\"0\"\n" +
        "        inDumpster=\"false\"\n" +
        "        nest=\"false\"\n" +
        "        cid=\"9bce02cf-9d9a-4d15-b95e-e1495b75770e:363\"\n" +
        "        xmlns=\"urn:zimbraMail\">\n" +
        "    <query>(((INID:\"9bce02cf-9d9a-4d15-b95e-e1495b75770e:2\") CONV:\"9bce02cf-9d9a-4d15-b95e-e1495b75770e:363\" ))</query>\n" +
        "    <locale>en_US</locale>\n" +
        "</SearchConvRequest>\n";

    private static Element getElementForEnvelopedJSON(String json) {
        Element envelope = null;
        try {
            envelope = Element.parseJSON(json);
        } catch (SoapParseException e) {
            fail(String.format("Parse from JSON to Element failed - %s", e.getMessage()));
        }
        assertNotNull(envelope, "Envelope element from parse from JSON");
        Element inner =  envelope.listElements().get(0);
        assertNotNull(inner, "element inside envelope element from parse from JSON");
        return inner;
    }

    private void doJsonSearchConvRecipCheck(String recipValue, WantRecipsSetting expected) throws ServiceException {
        Element elem = getElementForEnvelopedJSON(searchConvJson.replace("%%VALUE%%", recipValue));
        SearchConvRequest req = JaxbUtil.elementToJaxb(elem);
        assertEquals(expected, req.getWantRecipients(), String.format("recips:%s should map to %s", recipValue, expected));
    }

    private void doXmlSearchConvRecipCheck(String recipValue, WantRecipsSetting expected) throws ServiceException {
        Element elem = null;
        String xmlStr = searchConvXml.replace("%%VALUE%%", recipValue);
        elem = Element.parseXML(xmlStr);
        SearchConvRequest req = JaxbUtil.elementToJaxb(elem);
        assertEquals(expected, req.getWantRecipients(), String.format("recips=%s should map to %s", recipValue, expected));
    }

    private String jsonRecipWithValue(String value) {
        return String.format("\"recip\": %s,\n", value);
    }

    private String xmlRecipWithValue(String value) {
        return String.format("recip=\"%s\"\n", value);
    }

  @Test
  void searchConvJsonToJaxbRecipHandling() throws Exception {
    doJsonSearchConvRecipCheck(jsonRecipWithValue("false"), WantRecipsSetting.PUT_SENDERS);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("true"), WantRecipsSetting.PUT_RECIPIENTS);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("0"), WantRecipsSetting.PUT_SENDERS);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("1"), WantRecipsSetting.PUT_RECIPIENTS);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("2"), WantRecipsSetting.PUT_BOTH);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("\"0\""), WantRecipsSetting.PUT_SENDERS);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("\"1\""), WantRecipsSetting.PUT_RECIPIENTS);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("\"2\""), WantRecipsSetting.PUT_BOTH);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("invalid"), WantRecipsSetting.PUT_SENDERS);
    doJsonSearchConvRecipCheck(jsonRecipWithValue("3"), WantRecipsSetting.PUT_SENDERS);
    doJsonSearchConvRecipCheck("", WantRecipsSetting.PUT_SENDERS);
  }

  @Test
  void searchConvXmlToJaxbRecipHandling() throws Exception {
    doXmlSearchConvRecipCheck(xmlRecipWithValue("false"), WantRecipsSetting.PUT_SENDERS);
    doXmlSearchConvRecipCheck(xmlRecipWithValue("true"), WantRecipsSetting.PUT_RECIPIENTS);
    doXmlSearchConvRecipCheck(xmlRecipWithValue("0"), WantRecipsSetting.PUT_SENDERS);
    doXmlSearchConvRecipCheck(xmlRecipWithValue("1"), WantRecipsSetting.PUT_RECIPIENTS);
    doXmlSearchConvRecipCheck(xmlRecipWithValue("2"), WantRecipsSetting.PUT_BOTH);
    doXmlSearchConvRecipCheck(xmlRecipWithValue("invalid"), WantRecipsSetting.PUT_SENDERS);
    doXmlSearchConvRecipCheck(xmlRecipWithValue("3"), WantRecipsSetting.PUT_SENDERS);
    doXmlSearchConvRecipCheck("", WantRecipsSetting.PUT_SENDERS);
  }

    /**
     * Using Dom4j seems problematic - hence why elementToJaxbUsingDom4j is deprecated.  e.g. Seen this from
     * Jenkins, so now disabling the test - the underlying method is deprecated anyway.
     * com.zimbra.common.service.ServiceException: system failure: Unable to unmarshal response for GetInfoResponse
     * <pre>
ExceptionId:main:1337887540849:9bb07215a97a1378
Code:service.FAILURE
    at com.zimbra.common.service.ServiceException.FAILURE(ServiceException.java:258)
    at com.zimbra.soap.JaxbUtil.elementToJaxbUsingDom4j(JaxbUtil.java:1153)
    at com.zimbra.soap.account.JaxbToElementTest.elementToJaxbUsingDom4jTest(JaxbToElementTest.java:244)
Caused by: javax.xml.bind.UnmarshalException: Namespace URIs and local names to the unmarshaller needs to be interned.
    at com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.handleEvent(UnmarshallingContext.java:662)
    at com.sun.xml.bind.v2.runtime.unmarshaller.Loader.reportError(Loader.java:258)
    at com.sun.xml.bind.v2.runtime.unmarshaller.Loader.reportError(Loader.java:253)
    at com.sun.xml.bind.v2.runtime.unmarshaller.Loader.reportUnexpectedChildElement(Loader.java:118)
    at com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$DefaultRootLoader.childElement(UnmarshallingContext.java:1063)
    at com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext._startElement(UnmarshallingContext.java:498)
    at com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.startElement(UnmarshallingContext.java:480)
    at com.sun.xml.bind.v2.runtime.unmarshaller.SAXConnector.startElement(SAXConnector.java:150)
    at org.dom4j.io.SAXWriter.startElement(SAXWriter.java:628)
    at org.dom4j.io.SAXWriter.write(SAXWriter.java:579)
    at org.dom4j.io.SAXWriter.writeContent(SAXWriter.java:475)
    at org.dom4j.io.SAXWriter.write(SAXWriter.java:176)
    at org.dom4j.io.SAXWriter.parse(SAXWriter.java:457)
    at com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl.unmarshal0(UnmarshallerImpl.java:218)
    at com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl.unmarshal(UnmarshallerImpl.java:190)
    at javax.xml.bind.helpers.AbstractUnmarshallerImpl.unmarshal(AbstractUnmarshallerImpl.java:120)
    at javax.xml.bind.helpers.AbstractUnmarshallerImpl.unmarshal(AbstractUnmarshallerImpl.java:103)
    at com.zimbra.soap.JaxbUtil.elementToJaxbUsingDom4j(JaxbUtil.java:1151)
     * </pre>
     */
    @SuppressWarnings("deprecation")
    // Disabled @Test
    public void elementToJaxbUsingDom4jTest() throws Exception {
        for (int cnt = 1; cnt <= iterationNum;cnt++) {
            GetInfoResponse getInfoResp = JaxbUtil.elementToJaxbUsingDom4j(getInfoRespElem);
            assertEquals("user1@tarka.local", getInfoResp.getAccountName(), "Account name");
        }
    }

    /**  Not much point in running a test of a deprecated method - left for future timing comparison purposes */
    @SuppressWarnings("deprecation")
    // Disabled @Test
    public void elementToJaxbUsingByteArrayTest() throws Exception {
        for (int cnt = 1; cnt <= iterationNum;cnt++) {
            GetInfoResponse getInfoResp = JaxbUtil.elementToJaxbUsingByteArray(getInfoRespElem);
            assertEquals("user1@tarka.local", getInfoResp.getAccountName(), "Account name");
        }
    }

  @Test
  void JSONelementToJaxbTest() throws Exception {
    Element env = Element.parseJSON(getInfoResponseJSONwithEnv);
    Element el = env.listElements().get(0);
    org.w3c.dom.Document doc = el.toW3cDom();
    ZimbraLog.test.debug("JSONelementToJaxbTest toW3cDom Xml:\n%s", W3cDomUtil.asXML(doc));
    GetInfoResponse getInfoResp = JaxbUtil.elementToJaxb(el);
    assertEquals("user1@tarka.local", getInfoResp.getAccountName(), "Account name");
  }

    /*
     * This seems to work fine, although similar code in JAXB enabled ExportContacts server-side does not - get:
     *   javax.xml.bind.UnmarshalException: Namespace URIs and local names to the unmarshaller needs to be interned.
     */
    @SuppressWarnings("deprecation")
    // Disabled @Test
    public void JSONelementToJaxbUsingDom4jTest() throws Exception {
        for (int cnt = 1; cnt <= 4;cnt++) {
            Element env = Element.parseJSON(getInfoResponseJSONwithEnv);
            Element el = env.listElements().get(0);
            GetInfoResponse getInfoResp = JaxbUtil.elementToJaxbUsingDom4j(el);
            assertEquals("user1@tarka.local", getInfoResp.getAccountName(), "Account name");
        }
    }

  /**
   * Check that @{link JaxbUtil.elementToJaxb} will accept XML where
   * JAXB expects content type as an attribute but it is specified as
   * an element.
   * @throws Exception
     */
  @Test
  void importContactsWithContentTypeAsElementTest() throws Exception {
    Element icrElem = Element.XMLElement.mFactory.createElement(
        MailConstants.IMPORT_CONTACTS_REQUEST);
    icrElem.addAttribute(MailConstants.A_CSVLOCALE, "fr");
    icrElem.addNonUniqueElement(MailConstants.A_CONTENT_TYPE).setText("csv");
    icrElem.addNonUniqueElement(MailConstants.E_CONTENT).setText("CONTENT");
    ImportContactsRequest icr = JaxbUtil.elementToJaxb(icrElem);
    assertEquals("csv", icr.getContentType(), "ImportContactsRequest content type:");
    assertEquals("fr", icr.getCsvLocale(), "ImportContactsRequest csvlocale:");
    assertEquals("CONTENT", icr.getContent().getValue(), "ImportContactsRequest contents:");
  }

  /**
   * Check that @{link JaxbUtil.elementToJaxb} will accept XML where
   * JAXB expects various attributes that have been specified as elements
   * in a fairly deep structure.  Ensure that @XmlElementRef is handled
   * @throws Exception
     */
  @Test
  void jaxbElementRefsFixupTest() throws Exception {
    Element rootElem = Element.XMLElement.mFactory.createElement(
        AdminConstants.MAIL_QUEUE_ACTION_REQUEST);
    // JAXB Element E_SERVER --> ServerWithQueueAction
    Element svrE = rootElem.addNonUniqueElement(AdminConstants.E_SERVER);
    // JAXB attribute A_NAME
    svrE.addNonUniqueElement(AdminConstants.A_NAME).setText("SERVER-NAME");
    // JAXB Element E_QUEUE --> MailQueueWithAction
    Element qE = svrE.addNonUniqueElement(AdminConstants.E_QUEUE);
    // JAXB attribute A_NAME
    qE.addNonUniqueElement(AdminConstants.A_NAME).setText("queueName");
    // JAXB Element E_ACTION --> MailQueueAction
    Element actE = qE.addNonUniqueElement(AdminConstants.E_ACTION);
    // JAXB attribute A_OP
    actE.addNonUniqueElement(AdminConstants.A_OP).setText("requeue");
    // JAXB attribute A_BY
    actE.addNonUniqueElement(AdminConstants.A_BY).setText("query");
    // MailQueueAction XmlElementRef E_QUERY --> QueueQuery
    // actually, part of XmlMixed, so JAXB class deals in
    // an array of Object
    Element queryE = actE.addNonUniqueElement(AdminConstants.E_QUERY);
    // JAXB attribute A_OFFSET
    queryE.addAttribute(AdminConstants.A_OFFSET, "20");
    // JAXB attribute A_LIMIT
    queryE.addNonUniqueElement(AdminConstants.A_LIMIT).setText("99");
    for (int sfx = 1;sfx <= 3;sfx++) {
      // List<QueueQueryField> fields
      Element fE = queryE.addNonUniqueElement(AdminConstants.E_FIELD);
      fE.addAttribute(AdminConstants.A_NAME, "name" + sfx);
      // List<ValueAttrib> matches
      Element mE = fE.addNonUniqueElement(AdminConstants.E_MATCH);
      // JAXB attribute A_VALUE
      mE.addNonUniqueElement(AdminConstants.A_VALUE).setText("value " + sfx);
      mE = fE.addNonUniqueElement(AdminConstants.E_MATCH);
      // JAXB attribute A_VALUE
      mE.addNonUniqueElement(AdminConstants.A_VALUE).setText("2nd value " + sfx);
    }
    MailQueueActionRequest req = JaxbUtil.elementToJaxb(rootElem);
    ServerWithQueueAction svrWithQ = req.getServer();
    assertEquals("SERVER-NAME", svrWithQ.getName(), "Server name");
    MailQueueWithAction q = svrWithQ.getQueue();
    assertEquals("queueName", q.getName(), "Queue name");
    MailQueueAction a = q.getAction();
    assertEquals(MailQueueAction.QueueActionBy.query, a.getBy(), "Action BY");
    assertEquals(MailQueueAction.QueueAction.requeue, a.getOp(), "Action OP");
    QueueQuery query = a.getQuery();
    assertEquals(20, query.getOffset().intValue(), "Query offset");
    assertEquals(99, query.getLimit().intValue(), "Query limit");
    List<QueueQueryField> qFields = query.getFields();
    assertEquals(3, qFields.size(), "Number of query fields");
    assertEquals("name2",
        qFields.get(1).getName(),
        "Query field 2 name");
    List<ValueAttrib> matches = qFields.get(1).getMatches();
    assertEquals(2, matches.size(), "Number of matches");
    assertEquals("2nd value 2",
        matches.get(1).getValue(),
        "Match 2 value");
  }

  /**
   * Check that @{link JaxbUtil.elementToJaxb} will accept XML where
   * JAXB expects various attributes that have been specified as elements.
   * Ensure that @XmlElements is handled
   * @throws Exception
     */
  @Test
  void jaxbElementsFixupTest() throws Exception {
    Element rootElem = Element.XMLElement.mFactory.createElement(
        MailConstants.GET_CONTACTS_REQUEST);
    // JAXB Attribute A_SYNC
    rootElem.addNonUniqueElement(MailConstants.A_SYNC).addText("true");
    // JAXB Attribute A_FOLDER
    rootElem.addAttribute(MailConstants.A_FOLDER, "folderId");
    // JAXB Attribute A_SORTBY
    rootElem.addNonUniqueElement(MailConstants.A_SORTBY).addText("sortBy");
    // JAXB Elements:
    //    Element E_ATTRIBUTE --> AttributeName
    //    Element E_CONTACT --> Id
    Element attrName1 = rootElem.addNonUniqueElement(MailConstants.E_ATTRIBUTE);
    attrName1.addAttribute(MailConstants.A_ATTRIBUTE_NAME, "aName1");
    Element contact1 = rootElem.addNonUniqueElement(MailConstants.E_CONTACT);
    contact1.addNonUniqueElement(MailConstants.A_ID).addText("ctctId1");
    Element contact2 = rootElem.addNonUniqueElement(MailConstants.E_CONTACT);
    contact2.addAttribute(MailConstants.A_ID, "ctctId2");
    Element attrName2 = rootElem.addNonUniqueElement(MailConstants.E_ATTRIBUTE);
    attrName2.addNonUniqueElement(MailConstants.A_ATTRIBUTE_NAME).addText("aName2");
    Element memAttr1 = rootElem.addNonUniqueElement(MailConstants.E_CONTACT_GROUP_MEMBER_ATTRIBUTE);
    memAttr1.addNonUniqueElement(MailConstants.A_ATTRIBUTE_NAME).addText("grpAttrName1");

    GetContactsRequest req = JaxbUtil.elementToJaxb(rootElem);

    assertEquals(true, req.getSync().booleanValue(), "Sync");
    assertEquals("folderId", req.getFolderId(), "FolderID");
    assertEquals("sortBy", req.getSortBy(), "SortBy");
  }

  /**
   * Check that @{link JaxbUtil.elementToJaxb} will accept XML where
   * JAXB expects various attributes that have been specified as elements.
   * Ensure that attributes in elements of superclasses are handled
   * In this case:
   *                  <a><n>attrName1</n></a>
   * should be recognised as meaning:
   *                  <a n="attrName1"></a>
   * @throws Exception
     */
  @Test
  void jaxbSubclassFixupTest() throws Exception {
    Element rootElem = Element.XMLElement.mFactory.createElement(AdminConstants.CREATE_ACCOUNT_REQUEST);
    // JAXB Attribute E_NAME
    rootElem.addNonUniqueElement(AdminConstants.E_NAME).addText("acctName");
    // JAXB Attribute E_PASSWORD
    rootElem.addNonUniqueElement(AdminConstants.E_PASSWORD).addText("AcctPassword");
    // JAXB Element E_A ---> Attr (actually a List)
    Element a1 = rootElem.addNonUniqueElement(AdminConstants.E_A);
    // JAXB Attribute A_N
    a1.addNonUniqueElement(AdminConstants.A_N).addText("attrName1");
    // value can't be set when we've specified an attribute as an element

    CreateAccountRequest req = JaxbUtil.elementToJaxb(rootElem);
    assertEquals("acctName", req.getName(), "Account name");
    assertEquals("AcctPassword", req.getPassword(), "Account Password");
    List<Attr> attrs = req.getAttrs();
    assertEquals(1, attrs.size(), "Number of attrs");
    assertEquals("attrName1", attrs.get(0).getKey(), "attr 1 name");
    assertEquals("", attrs.get(0).getValue(), "attr 1 value");
  }

  @Test
  void jaxbInfoSuperclassElems() throws Exception {
    JaxbInfo jaxbInfo = JaxbInfo.getFromCache(CreateAccountRequest.class);
    Iterable<String> attrNames = jaxbInfo.getAttributeNames();
    assertEquals(2, Iterables.size(attrNames), "Number of attributes for CreateAccountRequest");
    Iterable<String> elemNames = jaxbInfo.getElementNames();
    assertEquals(1, Iterables.size(elemNames), "Number of elements for CreateAccountRequest");
    assertTrue(-1 != Iterables.indexOf(elemNames, Predicates.equalTo(MailConstants.E_A)), "Has <a>");
    Iterable<JaxbNodeInfo> nodeInfos = jaxbInfo.getJaxbNodeInfos();
    assertEquals(1, Iterables.size(nodeInfos), "Number of nodeInfos for CreateAccountRequest");
    JaxbNodeInfo nodeInfo = Iterables.get(nodeInfos, 0);
    assertEquals(MailConstants.E_A, nodeInfo.getName(), "NodeInfo name ");
    if (!(nodeInfo instanceof JaxbElementInfo)) {
      fail("Expecting JaxbElementInfo but got " + nodeInfo.getClass().getName());
    } else {
      JaxbElementInfo elemInfo = (JaxbElementInfo) nodeInfo;
      assertEquals(Attr.class, elemInfo.getAtomClass(), "Class associated with <a>");
    }
    JaxbNodeInfo node = jaxbInfo.getElemNodeInfo(MailConstants.E_A);
    assertNotNull(node, "has NodeInfo for Element <a>");
    assertTrue(jaxbInfo.hasElement(MailConstants.E_A), "hasElement <a>");
  }

  @Test
  void jaxbInfoWrapperHandling() throws Exception {
    JaxbInfo jaxbInfo = JaxbInfo.getFromCache(WaitSetRequest.class);
    Class<?> klass;
    klass = jaxbInfo.getClassForWrappedElement("notWrapperName", "nonexistent");
    if (klass != null) {
      fail("Class " + klass.getName() + " should be null for non-existent wrapper/wrapped");
    }
    klass = jaxbInfo.getClassForWrappedElement(MailConstants.E_WAITSET_ADD /* add */, "nonexistent");
    if (klass != null) {
      fail("Class " + klass.getName() + " should be null for existing wrapper/non-existent wrapped");
    }
    klass = jaxbInfo.getClassForWrappedElement(MailConstants.E_WAITSET_ADD /* add */, MailConstants.E_A);
    assertNotNull(klass, "Class should NOT be null for existing wrapper/non-existent wrapped");
    assertEquals(WaitSetAddSpec.class, klass, "WaitSetAddSpec class");
  }

  /**
   * Ensure that we still find attributes encoded as elements below a wrapped element in the hierarchy
   * @throws Exception
     */
  @Test
  void jaxbBelowWrapperFixupTest() throws Exception {
    Element rootElem = Element.XMLElement.mFactory.createElement(MailConstants.WAIT_SET_REQUEST);
    // JAXB Attribute - not Element
    rootElem.addNonUniqueElement(MailConstants.A_WAITSET_ID /* waitSet */).addText("myWaitSet");
    // JAXB Attribute - not Element
    rootElem.addNonUniqueElement(MailConstants.A_SEQ /* seq */).addText("lastKnownSeq");
    // JAXB XmlElementWrapper
    Element addElem = rootElem.addNonUniqueElement(MailConstants.E_WAITSET_ADD /* add */);
    Element aElem = addElem.addNonUniqueElement(MailConstants.E_A /* a */);
    // JAXB Attribute - not Element
    aElem.addNonUniqueElement(MailConstants.A_NAME).addText("waitsetName");
    // JAXB Attribute - not Element
    aElem.addNonUniqueElement(MailConstants.A_ID).addText("waitsetId");
    WaitSetRequest req = JaxbUtil.elementToJaxb(rootElem);
    List<WaitSetAddSpec> adds = req.getAddAccounts();
    assertEquals(1, adds.size(), "Waitset add number");
    WaitSetAddSpec wsAdd = adds.get(0);
    assertEquals("waitsetName", wsAdd.getName(), "Waitset name");
    assertEquals("waitsetId", wsAdd.getId(), "Waitset id");
  }

  /**
   * Check that @{link JaxbUtil.elementToJaxb} will accept XML where
   * JAXB expects various attributes that have been specified as elements.
   * Ensure that attributes in wrapped elements are handled
   * @throws Exception
     */
  @Test
  void jaxbWrapperFixupTest() throws Exception {
    Element rootElem = Element.XMLElement.mFactory.createElement(
        AccountConstants.AUTH_REQUEST);
    // JAXB wrapper element name E_PREFS
    Element prefsE = rootElem.addNonUniqueElement(AccountConstants.E_PREFS);
    // JAXB element E_PREF with attribute "name"
    Element prefE = prefsE.addNonUniqueElement(AccountConstants.E_PREF);
    prefE.addNonUniqueElement("name").addText("pref name");

    AuthRequest req = JaxbUtil.elementToJaxb(rootElem);
    List<Pref> prefs = req.getPrefs();
    assertEquals(1, prefs.size(), "Number of prefs");
    assertEquals("pref name", prefs.get(0).getName(), "Pref name");
  }

    /**
     * Explore handling of Jaxb classes which specify an @XmlElement with
     * a super class.  How do subclasses get treated with this?
     * WSDLJaxbTest.ConvActionRequestJaxbSubclassHandlingTest passes,
     * i.e. it successfully unmarshalls to a ConvActionRequest with
     * a FolderActionSelector member.
     * However, even if I use those class files (with package name changed)
     * in place of the committed ones, this test only seems to unmarshall
     * with an ActionSelector member - i.e. the "recursive" and "url"
     * attribute information gets lost.
     */
    // @Test
    public void ConvActionRequestJaxbSubclassHandlingTestDisabled() throws Exception {
        ConvActionSelector actionSelector = ConvActionSelector.createForIdsAndOperation("ids", "op");
        actionSelector.setAcctRelativePath("folder");
        ConvActionRequest car = new ConvActionRequest(actionSelector);
        Element carE = JaxbUtil.jaxbToElement(car);
        String eXml = carE.toString();
        ZimbraLog.test.debug("ConvActionRequestJaxbSubclassHandling: marshalled XML=%s", eXml);
        assertTrue(eXml.contains("acctRelPath=\"folder\""), "Xml should contain acctRelPath attribute");

        carE = Element.XMLElement.mFactory.createElement(MailConstants.CONV_ACTION_REQUEST);
        Element actionE = carE.addNonUniqueElement(MailConstants.E_ACTION);
        actionE.addAttribute(MailConstants.A_OPERATION, "op");
        actionE.addAttribute(MailConstants.A_ID, "ids");
        actionE.addAttribute(MailConstants.A_ACCT_RELATIVE_PATH, "folder");
        ZimbraLog.test.debug("ConvActionRequestJaxbSubclassHandling: half baked XML=%s", carE.toString());
        car = JaxbUtil.elementToJaxb(carE);
        carE = JaxbUtil.jaxbToElement(car);
        eXml = carE.toString();
        ZimbraLog.test.debug("ConvActionRequestJaxbSubclassHandling: round tripped XML=%s", eXml);
        ConvActionSelector as = car.getAction();
        assertEquals("folder", as.getAcctRelativePath(), "acctRelPath attr value");
    }

  /**
   * The Session JAXB object shares the same field for both the value and the id attribute.  Check that nothing is
   * lost in a round trip.
   */
  @Test
  void accountSessionJaxbTest() throws Exception {
    final String myId = "my-id";
    final String myType = "admin";
    Session sess = new Session();
    sess.setId(myId);
    sess.setType(myType);
    Element sessE = JaxbUtil.jaxbToNamedElement(HeaderConstants.E_SESSION, AccountConstants.NAMESPACE_STR, sess,
        Element.XMLElement.mFactory);
    assertNotNull(sessE, "jaxbToNamedElement Session Element");
    assertEquals(myId, sessE.getAttribute(HeaderConstants.A_ID), "from jaxb id attr");
    assertEquals(myType, sessE.getAttribute(HeaderConstants.A_TYPE), "from jaxb type attr");
    assertEquals(myId, sessE.getText(), "from jaxb value");
    sess = JaxbUtil.elementToJaxb(sessE, Session.class);
    assertEquals(myId, sess.getSessionId(), "jaxb from element - value");
    assertEquals(myId, sess.getId(), "jaxb from element - id");
    assertEquals(myType, sess.getType(), "jaxb from element - type");
  }

  @Test
  void standalonElementToJaxbTest() throws Exception {
    InputStream is = getClass().getResourceAsStream("retentionPolicy.xml");
    Element elem = Element.parseXML(is);
    String eXml = elem.toString();
    ZimbraLog.test.debug("retentionPolicy.xml from Element:\n%s", eXml);
    RetentionPolicy rp = JaxbUtil.elementToJaxb(elem, RetentionPolicy.class);
    assertNotNull(rp, "elementToJaxb RetentionPolicy returned object");
    Element elem2 = JaxbUtil.jaxbToElement(rp, XMLElement.mFactory);
    String eXml2 = elem2.toString();
    ZimbraLog.test.debug("Round tripped retentionPolicy.xml from Element:\n%s", eXml2);
    assertFalse(DiffBuilder.compare(eXml).withTest(eXml2).build().hasDifferences());
  }

  @Test
  void IdentityToStringTest() throws Exception {
    com.zimbra.soap.account.type.Identity id =
        new com.zimbra.soap.account.type.Identity("hello", null);
    Map<String, String> attrs = Maps.newHashMap();
    attrs.put("key1", "value1");
    attrs.put("key2", "value2 wonderful");
    id.setAttrs(attrs);
    CreateIdentityRequest request = new CreateIdentityRequest(id);
    String toString = request.toString();
    assertTrue(toString.startsWith("CreateIdentityRequest{identity=Identity{a="), "toString start chars");
    assertTrue(toString.contains("Attr{name=key1, value=value1}"), "toString key1");
    assertTrue(toString.contains("Attr{name=key2, value=value2 wonderful}"), "toString key2");
    assertTrue(toString.contains("name=hello"), "toString name");
    assertTrue(toString.contains("id=null"), "toString id");
  }

  /*
   * Currently, DeleteDataSourceRequest does not have a setter for all datasource children.  Making sure that
   * it works.  Actually believe that JAXB ignores setters for lists and adds by using the getter to get the
   * list and adding to that.
   */
  @Test
  void DeleteDataSourceRequestTest() throws Exception {
    DeleteDataSourceRequest req = new DeleteDataSourceRequest();
    Pop3DataSourceNameOrId pop = new Pop3DataSourceNameOrId();
    pop.setName("pop3name");
    ImapDataSourceNameOrId imap = new ImapDataSourceNameOrId();
    imap.setName("imap4name");
    req.addDataSource(pop);
    req.addDataSource(imap);
    Element elem = JaxbUtil.jaxbToElement(req, XMLElement.mFactory);
    assertNotNull(elem, "DataSourceRequest elem");
    Element imapE = elem.getElement(MailConstants.E_DS_IMAP);
    assertNotNull(imapE, "imap elem");
    Element popE = elem.getElement(MailConstants.E_DS_POP3);
    assertNotNull(popE, "imap elem");
    req = JaxbUtil.elementToJaxb(elem, DeleteDataSourceRequest.class);
    assertNotNull(req, "JAXB DeleteDataSourceRequest");
    assertEquals(2, req.getDataSources().size(), "Number of datasources in JAXB");
  }

  @Test
  void KeyValuePairs() throws Exception {
    try (InputStream is = JaxbToElementTest.class.getResourceAsStream("CreateXMbxSearchRequest.xml")) {
      // String soapXml = streamToString(is, Charsets.UTF_8);
      JAXBContext jaxb = JAXBContext.newInstance(CreateXMbxSearchRequest.class);
      Unmarshaller kvpunmarshaller = jaxb.createUnmarshaller();
      JAXBElement<CreateXMbxSearchRequest> jaxbElem =
          kvpunmarshaller.unmarshal(new StreamSource(is), CreateXMbxSearchRequest.class);
      assertNotNull(jaxbElem, "JAXBElement resulting from unmarshal");
      CreateXMbxSearchRequest soapObj = jaxbElem.getValue();
      assertNotNull(soapObj, "Unmarshal soap object");
      assertEquals(10, soapObj.getKeyValuePairs().size(), "Number of attributes");
    }
  }

  // AutoProvDirectoryEntry contains 2 lists - one via extending AdminKeyValuePairs
  // No order is specified for elements via XmlType propOrder.  Checking how well this works.
  @Test
  void searchAutoProvDirectoryResponse() throws Exception {
    Element resp = Element.XMLElement.mFactory.createElement(
        AdminConstants.SEARCH_AUTO_PROV_DIRECTORY_RESPONSE);
    resp.addAttribute(AdminConstants.A_MORE, false);
    resp.addAttribute(AdminConstants.A_SEARCH_TOTAL, 1);
    Element entryE = resp.addNonUniqueElement(AdminConstants.E_ENTRY);
    entryE.addAttribute(AdminConstants.A_DN, "displayNam");
    entryE.addNonUniqueElement(AdminConstants.E_KEY).setText("keyValue1");
    entryE.addNonUniqueElement(AdminConstants.E_KEY).setText("keyValue2");
    entryE.addNonUniqueElement(AdminConstants.E_A).addAttribute(AdminConstants.A_N, "nVal1").setText("attr1Txt");
    entryE.addNonUniqueElement(AdminConstants.E_A).addAttribute(AdminConstants.A_N, "nVal2").setText("attr2Txt");
    SearchAutoProvDirectoryResponse jaxb = JaxbUtil.elementToJaxb(resp);
    assertNotNull(jaxb, "Unmarshal soap object");
    List<AutoProvDirectoryEntry> entries = jaxb.getEntries();
    assertNotNull(entries, "entries list");
    assertEquals(1, entries.size(), "Number of entries");
    AutoProvDirectoryEntry entry = entries.get(0);
    List<KeyValuePair> kvps = entry.getKeyValuePairs();
    assertNotNull(kvps, "entry - attrs list");
    assertEquals(2, kvps.size(), "entry - Number of attrs");
    List<String> keys = entry.getKeys();
    assertNotNull(keys, "entry - keys list");
    assertEquals(2, keys.size(), "entry - Number of keys");
  }

    // TODO - enable when/if bug fixed.  Note that handler currently uses "Element".  This issue is with
    //        zmsoap producing non-KeyValuePairs compatible JSON.  Although JAXB object originated JSON
    //        currently exhibits similar issues.  Would prefer to fix JAXB to JSON, use the JAXB object in zmsoap
    //        and send the resulting JSON to the server rather than fix the Element code to understand Xml-like
    //        JSON KeyValuePairs - added flexibility means more complexity...
    // @Test
    public void bug62571_zmsoapRequestPrefsSupport() throws Exception {
        HashMap<String, Object> prefs;
        final String zmsoapRequest = "{ \"pref\": [" +
                "{ \"_content\": \"TRUE\", \"name\": \"zimbraPrefSharedAddrBookAutoCompleteEnabled\" }," +
                "{ \"_content\": \"carbon\", \"name\": \"zimbraPrefSkin\" }" +
                "], \"_jsns\": \"urn:zimbraAccount\" }";
        final String idealRequest = "{ \"_attrs\": { " +
                "\"zimbraPrefSharedAddrBookAutoCompleteEnabled\": \"TRUE\"," +
                "\"zimbraPrefSkin\": \"carbon\"" +
                "}, \"_jsns\": \"urn:zimbraAccount\" }";
        // SoapEngine uses this to parse the Whole soapMessage for a request where "in" is a ByteArrayInputStream
        //        document = Element.parseJSON(in);
        // That parseJSON seems to extract the string from the stream and end up calling something similar to this...
        Element zmsoapElem = Element.parseJSON(zmsoapRequest);
        Element idealElem = Element.parseJSON(idealRequest);
        prefs = Maps.newHashMap();
        for (Element.KeyValuePair kvp : idealElem.listKeyValuePairs(AccountConstants.E_PREF, AccountConstants.A_NAME)) {
            String name = kvp.getKey(), value = kvp.getValue();
            StringUtil.addToMultiMap(prefs, name, value);
        }
        assertEquals(2, prefs.size(), "Ideal request - number of prefs found");
        prefs = Maps.newHashMap();
        for (Element.KeyValuePair kvp : zmsoapElem.listKeyValuePairs(AccountConstants.E_PREF, AccountConstants.A_NAME)) {
            String name = kvp.getKey(), value = kvp.getValue();
            StringUtil.addToMultiMap(prefs, name, value);
        }
        assertEquals(2, prefs.size(), "zmsoap request - number of prefs found");
    }

    // Simplified version of what appears in mail.ToXML
    public static void encodeAttr(Element parent, String key, String value, String eltname, String attrname,
            boolean allowed) {

        Element.KeyValuePair kvPair;
        if (allowed) {
            kvPair = parent.addKeyValuePair(key, value, eltname, attrname);
        } else {
            kvPair = parent.addKeyValuePair(key, "", eltname, attrname);
            kvPair.addAttribute(AccountConstants.A_PERM_DENIED, true);
        }
    }

  // GetInfo encoding of identities calls similar code
  @Test
  void encodeAttrsWithDenied() throws Exception {
    Element identExml = Element.XMLElement.mFactory.createElement(AccountConstants.E_IDENTITY);
    encodeAttr(identExml, "keyAllowed", "valueAllowed", AccountConstants.E_A, AccountConstants.A_NAME, true);
    encodeAttr(identExml, "keyDenied", "valueDenied", AccountConstants.E_A, AccountConstants.A_NAME, false);
    Element identEjson = Element.JSONElement.mFactory.createElement(AccountConstants.E_IDENTITY);
    encodeAttr(identEjson, "keyAllowed", "valueAllowed", AccountConstants.E_A, AccountConstants.A_NAME, true);
    encodeAttr(identEjson, "keyDenied", "valueDenied", AccountConstants.E_A, AccountConstants.A_NAME, false);
    // <identity><a name="keyAllowed">valueAllowed</a><a pd="1" name="keyDenied"/></identity>
    ZimbraLog.test.debug("encodeAttrsWithDenied xml\n%s", identExml.toString());
    // {"_attrs":{"keyAllowed":"valueAllowed","keyDenied":{"_content":"","pd":true}}}
    ZimbraLog.test.debug("encodeAttrsWithDenied json\n%s", identEjson.toString());
    com.zimbra.soap.account.type.Attr deniedAttr = com.zimbra.soap.account.type.Attr.forNameWithPermDenied("keyDenied");
    Element elem2 = JaxbUtil.jaxbToNamedElement(AccountConstants.E_A, AccountConstants.NAMESPACE_STR,
        deniedAttr, XMLElement.mFactory);
    String eXml2 = elem2.toString();
    ZimbraLog.test.debug("XML from JAXB denied attr\n%s", eXml2);
    assertEquals(AccountConstants.E_A, elem2.getName(), "XML from JAXB Attr top name");
    assertEquals("1", elem2.getAttribute("pd"), "XML from JAXB Attr pd");
    assertEquals("keyDenied", elem2.getAttribute("name"), "XML from JAXB Attr name");
  }

  // Verify that fromString will throw an exception for a duff value rather than return null
  @Test
  void modGroupMemberOpWithBadValue() {
    try {
      ModifyGroupMemberOperation.fromString("duff");
      fail("ServiceException NOT thrown");
    } catch (ServiceException e) {
    }
    try {
      ModifyGroupMemberOperation.fromString(null);
      fail("ServiceException NOT thrown for null");
    } catch (ServiceException e) {
    }
  }

  @Test
  void jaxbElementNameOrderXmlElementWrapperTest() throws Exception {
    JaxbInfo jaxbInfo = JaxbInfo.getFromCache(GetWhiteBlackListResponse.class);
    List<List<org.dom4j.QName>> expectedOrder = Lists.newArrayList();
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName(AccountConstants.E_WHITE_LIST, AccountConstants.NAMESPACE)));
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName(AccountConstants.E_BLACK_LIST, AccountConstants.NAMESPACE)));

    List<List<org.dom4j.QName>> nameOrder = jaxbInfo.getElementNameOrder();
    assertEquals(expectedOrder.size(), nameOrder.size(), String.format(
        "Number of entries in order expected=%d actual=%d", expectedOrder.size(), nameOrder.size()));
    for (int ndx = 0;ndx < nameOrder.size();ndx++) {
      List<QName> expected = expectedOrder.get(ndx);
      List<QName> actual = nameOrder.get(ndx);
      assertEquals(expected.size(), actual.size(), String.format(
          "Number of entries at pos %d expected=%d actual=%d", ndx, expected.size(), actual.size()));
      for (int cnt = 0;cnt < actual.size();cnt++) {
        QName qExpected = expected.get(cnt);
        QName qActual = actual.get(cnt);
        assertEquals(qExpected.getName(), qActual.getName(), String.format("Element name at pos %d/%d", ndx, cnt));
        assertEquals(qExpected.getNamespaceURI(), qActual.getNamespaceURI(), String.format("Element namespaceURI at pos %d/%d", ndx, cnt));
      }
    }
  }

    private static String modifyPrefsAsJson = "{\"_attrs\":{\"zimbraPrefGroupMailBy\":\"message\","
            + "\"zimbraPrefMailItemsPerPage\":\"200\",\"+zimbraPrefTimeZoneId\":\"Africa/Harare\","
            + "\"zimbraPrefSpellIgnoreWord\":[\"zimbra\",\"jaxb\"]},\"_jsns\":\"urn:zimbraAccount\"}";

  @Test
  void modifyPrefs() throws Exception {
    ModifyPrefsRequest req = new ModifyPrefsRequest();
    req.addPref(new Pref("zimbraPrefGroupMailBy", "message"));
    req.addPref(new Pref("zimbraPrefMailItemsPerPage", "200"));
    req.addPref(new Pref("+zimbraPrefTimeZoneId", "Africa/Harare"));  // method of adding to multivalue
    // method of setting multivalue
    req.addPref(new Pref("zimbraPrefSpellIgnoreWord", "zimbra"));
    req.addPref(new Pref("zimbraPrefSpellIgnoreWord", "jaxb"));
    Element jsonElem = JacksonUtil.jaxbToJSONElement(req);
    assertNotNull(jsonElem, "JSON Element");
    assertEquals(modifyPrefsAsJson, jsonElem.toString(), "JSON");
    req = JaxbUtil.elementToJaxb(jsonElem);
    List<Pref> prefs = req.getPrefs();
    assertEquals(5, prefs.size(), "Number of round tripped prefs");
  }

  @Test
  void jaxbMessageHitInfoElementNameOrder() throws Exception {
    JaxbInfo jaxbInfo = JaxbInfo.getFromCache(MessageHitInfo.class);
    List<List<org.dom4j.QName>> expectedOrder = Lists.newArrayList();
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("meta", MailConstants.NAMESPACE)));
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("fr", MailConstants.NAMESPACE)));
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("e", MailConstants.NAMESPACE)));
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("su", MailConstants.NAMESPACE)));
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("mid", MailConstants.NAMESPACE)));
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("irt", MailConstants.NAMESPACE)));
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("inv", MailConstants.NAMESPACE)));
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("header", MailConstants.NAMESPACE)));
    List<org.dom4j.QName> sameOrder = Lists.newArrayList();
    sameOrder.add(new org.dom4j.QName("mp", MailConstants.NAMESPACE));
    sameOrder.add(new org.dom4j.QName("shr", MailConstants.NAMESPACE));
    sameOrder.add(new org.dom4j.QName("dlSubs", MailConstants.NAMESPACE));
    expectedOrder.add(sameOrder);
    expectedOrder.add(Lists.newArrayList(new org.dom4j.QName("hp", MailConstants.NAMESPACE)));

    List<List<org.dom4j.QName>> nameOrder = jaxbInfo.getElementNameOrder();
    assertEquals(expectedOrder.size(), nameOrder.size(), String.format(
        "Number of entries in order expected=%d actual=%d", expectedOrder.size(), nameOrder.size()));
    for (int ndx = 0;ndx < nameOrder.size();ndx++) {
      List<QName> expected = expectedOrder.get(ndx);
      List<QName> actual = nameOrder.get(ndx);
      assertEquals(expected.size(), actual.size(), String.format(
          "Number of entries at pos %d expected=%d actual=%d", ndx, expected.size(), actual.size()));
      for (int cnt = 0;cnt < actual.size();cnt++) {
        QName qExpected = expected.get(cnt);
        QName qActual = actual.get(cnt);
        assertEquals(qExpected.getName(), qActual.getName(), String.format("Element name at pos %d/%d", ndx, cnt));
        assertEquals(qExpected.getNamespaceURI(), qActual.getNamespaceURI(), String.format("Element namespaceURI at pos %d/%d", ndx, cnt));
      }
    }
  }
}
