// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.QName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.XMbxSearchConstants;
import com.zimbra.soap.account.message.CreateDistributionListResponse;
import com.zimbra.soap.account.message.GetDistributionListMembersResponse;
import com.zimbra.soap.account.message.GetDistributionListResponse;
import com.zimbra.soap.account.type.Attr;
import com.zimbra.soap.account.type.DLInfo;
import com.zimbra.soap.account.type.DistributionListGranteeInfo;
import com.zimbra.soap.account.type.DistributionListInfo;
import com.zimbra.soap.admin.message.AuthResponse;
import com.zimbra.soap.admin.message.CreateXMbxSearchRequest;
import com.zimbra.soap.admin.message.VerifyIndexResponse;
import com.zimbra.soap.jaxb.AnyAttrTester;
import com.zimbra.soap.jaxb.AnyTester;
import com.zimbra.soap.jaxb.ElementRefTester;
import com.zimbra.soap.jaxb.ElementRefsTester;
import com.zimbra.soap.jaxb.EnumAttribEnumElem;
import com.zimbra.soap.jaxb.EnumAttribs;
import com.zimbra.soap.jaxb.EnumElemList;
import com.zimbra.soap.jaxb.KVPairs;
import com.zimbra.soap.jaxb.KeyValuePairsTester;
import com.zimbra.soap.jaxb.MixedAnyTester;
import com.zimbra.soap.jaxb.MixedTester;
import com.zimbra.soap.jaxb.NamespaceDeltaElem;
import com.zimbra.soap.jaxb.OddKeyValuePairsTester;
import com.zimbra.soap.jaxb.StringAttrStringElem;
import com.zimbra.soap.jaxb.StringAttribIntValue;
import com.zimbra.soap.jaxb.TransientTester;
import com.zimbra.soap.jaxb.UniqueTester;
import com.zimbra.soap.jaxb.ViewEnum;
import com.zimbra.soap.jaxb.WrappedEnumElemList;
import com.zimbra.soap.jaxb.WrappedKeyValuePairsTester;
import com.zimbra.soap.jaxb.WrappedRequired;
import com.zimbra.soap.jaxb.WrapperAbsentIfEmpty;
import com.zimbra.soap.jaxb.XmlElemJsonAttr;
import com.zimbra.soap.json.JacksonUtil;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;
import com.zimbra.soap.mail.message.GetFilterRulesResponse;
import com.zimbra.soap.mail.message.GetSystemRetentionPolicyResponse;
import com.zimbra.soap.mail.message.NoOpResponse;
import com.zimbra.soap.mail.type.AppointmentData;
import com.zimbra.soap.mail.type.CalendaringDataInterface;
import com.zimbra.soap.mail.type.FilterAction;
import com.zimbra.soap.mail.type.FilterRule;
import com.zimbra.soap.mail.type.FilterTest;
import com.zimbra.soap.mail.type.FilterTests;
import com.zimbra.soap.mail.type.InstanceDataInfo;
import com.zimbra.soap.mail.type.Policy;
import com.zimbra.soap.mail.type.RetentionPolicy;
import com.zimbra.soap.type.GranteeType;
import com.zimbra.soap.type.KeyValuePair;

public class JaxbToJsonTest {
     public String testName;

    private static final Logger LOG = Logger.getLogger(JaxbToJsonTest.class);

    private static String jsonEmptyGetSystemRetentionPolicyResponse =
            "{\n" +
            "  \"retentionPolicy\": [{\n" +
            "      \"keep\": [{}],\n" +
            "      \"purge\": [{}]\n" +
            "    }],\n" +
            "  \"_jsns\": \"urn:zimbraMail\"\n" +
            "}";

    private static String jsonGetSystemRetentionPolicyResponse =
            "{\n" +
            "  \"retentionPolicy\": [{\n" +
            "      \"keep\": [{\n" +
            "          \"policy\": [{\n" +
            "              \"type\": \"user\",\n" +
            "              \"lifetime\": \"200\"\n" +
            "            }]\n" +
            "        }],\n" +
            "      \"purge\": [{\n" +
            "          \"policy\": [{\n" +
            "              \"type\": \"user\",\n" +
            "              \"lifetime\": \"400\"\n" +
            "            }]\n" +
            "        }]\n" +
            "    }],\n" +
            "  \"_jsns\": \"urn:zimbraMail\"\n" +
            "}";

    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        LOG.setLevel(Level.INFO);
    }

    private void logDebug(String format, Object ... objects) {
        if (LOG.isDebugEnabled()) {
            LOG.debug( testName + ":" + String.format(format, objects));
        }
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

  /**
   * the element referenced MailConstants.E_FRAG is treated in XML as an element with content but no attributes.
   * However in JSON, it is just treated like an ordinary attribute.
   * So, in JSON we DO want:
   *    "fr": "Here is some wonderful text and some more",
   * We do NOT want it to be treated as if it was an element which would look like:
   *    "fr": [{
   *           "_content": "Here is some wonderful text and some more"
   *         }],
   */
  @Test
  void bug61264AttributeDispositionCONTENThandling() throws Exception {
    StringBuilder sb;
    final String uid = "uidString";
    final String frag = "Fragment text";
    final String name = "name Attribute";

    // From stacktrace of where the SOAP response gets assembled:
    //     at com.zimbra.common.soap.Element.output(Element.java:432)
    //     at com.zimbra.soap.SoapServlet.sendResponse(SoapServlet.java:349)
    //     at com.zimbra.soap.SoapServlet.doWork(SoapServlet.java:307)
    //     at com.zimbra.soap.SoapServlet.doPost(SoapServlet.java:206)

    // Bug 61264 is about issues with code that used JAXB which replaced code in GetCalendarItemSummaries
    // which started with an element created using:
    //     calItemElem = lc.createElement(isAppointment ? MailConstants.E_APPOINTMENT : MailConstants.E_TASK);
    // At present, that code has been reverted to be element based.

    // For comparison purposes, create an JSONElement tree and a XMLElement tree
    Element jsoncalItemElem = JSONElement.mFactory.createElement(MailConstants.E_APPOINTMENT);
    jsoncalItemElem.addAttribute(MailConstants.A_UID, uid);
    jsoncalItemElem.addAttribute("x_uid", uid);
    Element instElt = jsoncalItemElem.addNonUniqueElement(MailConstants.E_INSTANCE);
    instElt.addAttribute(MailConstants.E_FRAG, frag, Element.Disposition.CONTENT);
    instElt.addAttribute(MailConstants.A_CAL_IS_EXCEPTION, true);
    instElt.addAttribute(MailConstants.A_NAME, name);

    Element xmlcalItemElem = XMLElement.mFactory.createElement(MailConstants.E_APPOINTMENT);
    xmlcalItemElem.addAttribute(MailConstants.A_UID, uid);
    xmlcalItemElem.addAttribute("x_uid", uid);
    Element xmlinstElt = xmlcalItemElem.addNonUniqueElement(MailConstants.E_INSTANCE);
    xmlinstElt.addAttribute(MailConstants.E_FRAG, frag, Element.Disposition.CONTENT);
    xmlinstElt.addAttribute(MailConstants.A_CAL_IS_EXCEPTION, true);
    xmlinstElt.addAttribute(MailConstants.A_NAME, name);

    CalendaringDataInterface calData = null;
    calData = new AppointmentData(uid, uid);
    InstanceDataInfo instance = new InstanceDataInfo();
    calData.addCalendaringInstance(instance);
    instance.setIsException(true);
    instance.setName(name);
    instance.setFragment(frag);

    Element jsonJaxbElem = JaxbUtil.jaxbToNamedElement(MailConstants.E_APPOINTMENT,
        MailConstants.NAMESPACE_STR,
        calData, JSONElement.mFactory);

    Element xmlJaxbElem = JaxbUtil.jaxbToNamedElement(MailConstants.E_APPOINTMENT,
        MailConstants.NAMESPACE_STR,
        calData, XMLElement.mFactory);

    // As AppointmentData doesn't have an XmlRootElement, this gives a poor choice for the root name.
    //     Element jacksonJaxbElem = JacksonUtil.jaxbToJSONElement(calData);
    // This is probably a closer analog to JSONElement.mFactory.createElement(MailConstants.E_APPOINTMENT);
    //     Element jacksonJaxbElem = JacksonUtil.jaxbToJSONElement(calData, new QName("appt", null));
    Element jacksonJaxbElem = JacksonUtil.jaxbToJSONElement(calData,
        new QName(MailConstants.E_APPOINTMENT, MailConstants.NAMESPACE));

    Element parent4legacyJson = JSONElement.mFactory.createElement(new QName("legacy-json", MailConstants.NAMESPACE));
    Element parent4jackson = JSONElement.mFactory.createElement(new QName("jacksonjson", MailConstants.NAMESPACE));
    parent4legacyJson.addNonUniqueElement(jsoncalItemElem);
    parent4jackson.addNonUniqueElement(jacksonJaxbElem);

    sb = new StringBuilder();
    xmlcalItemElem.output(sb);
    logDebug("bug61264 - XML from XMLElement\n%1$s", sb.toString());

    sb = new StringBuilder();
    xmlJaxbElem.output(sb);
    logDebug("bug61264 - XML from JAXB\n%1$s", sb.toString());

    sb = new StringBuilder();  // something that is appendable for Element.out(Appendable) to play with
    jsoncalItemElem.output(sb);
    String jsonFromElement = sb.toString();
    logDebug("bug61264 - JSON from JSONElement\n%1$s", jsonFromElement);

    sb = new StringBuilder();
    jsonJaxbElem.output(sb);
    logDebug("bug61264 - JSON from JAXB\n%1$s", sb.toString());

    sb = new StringBuilder();
    jacksonJaxbElem.output(sb);
    logDebug("bug61264 - JSON from JAXB using Jackson\n%1$s", sb.toString());
    sb = new StringBuilder();
    parent4legacyJson.output(sb);
    logDebug("bug61264 - JSON from JAXB child using Jackson\n%1$s", sb.toString());
    sb = new StringBuilder();
    parent4jackson.output(sb);
    logDebug("bug61264 - JSON from JSONElement child\n%1$s", sb.toString());
    assertEquals(uid, jacksonJaxbElem.getAttribute(MailConstants.A_UID), "UID");
    assertEquals(uid, jacksonJaxbElem.getAttribute("x_uid"), "x_uid");
    Element instE = jacksonJaxbElem.getElement(MailConstants.E_INSTANCE);
    assertNotNull(instE, "instance elem");
    assertEquals(frag, instE.getAttribute(MailConstants.E_FRAG), "fragment");
    assertTrue(instE.getAttributeBool(MailConstants.A_CAL_IS_EXCEPTION), "is exception");
    assertEquals(name, instE.getAttribute(MailConstants.A_NAME), "name");
  }

  /**
   *  {
   *    "status": [{
   *        "_content": "true"   # Actually get true not "true" but should be ok
   *      }],
   *    "message": [{
   *        "_content": "ver ndx message"
   *      }],
   *    "_jsns": "urn:zimbraAdmin"
   *  }
   */
  @Test
  void zmBooleanAntStringXmlElements() throws Exception {
    final String msg = "ver ndx message";
    // ---------------------------------  For Comparison - Element handling
    Element legacyElem = JSONElement.mFactory.createElement(AdminConstants.VERIFY_INDEX_RESPONSE);
    legacyElem.addNonUniqueElement(AdminConstants.E_STATUS).addText(String.valueOf(true));
    legacyElem.addNonUniqueElement(AdminConstants.E_MESSAGE).addText(msg);
    logDebug("VerifyIndexResponse JSONElement ---> prettyPrint\n%1$s", legacyElem.prettyPrint());

    VerifyIndexResponse viResp = new VerifyIndexResponse(true, msg);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(viResp);
    logDebug("VerifyIndexResponse JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(true, jsonJaxbElem.getAttributeBool(AdminConstants.E_STATUS), "status");
    assertEquals(msg, jsonJaxbElem.getAttribute(AdminConstants.E_MESSAGE), "message");
    VerifyIndexResponse roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, VerifyIndexResponse.class);
    assertEquals(true, roundtripped.isStatus(), "roundtripped status");
    assertEquals(msg, roundtripped.getMessage(), "roundtripped message");
  }

  /**
   * Desired JSON :
   * {
   *   "_attrs": {
   *     "key1": "value1",
   *     "key2": [
   *       "value2-a",
   *       "value2-b"]
   *   },
   *   "_jsns": "urn:zimbraTest"
   * }
   */
  @Test
  void keyValuePairs() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("key-value-pairs", "urn:zimbraTest"));
    jsonElem.addKeyValuePair("key1", "value1");
    jsonElem.addKeyValuePair("key2", "value2-a");
    jsonElem.addKeyValuePair("key2", "value2-b");
    KVPairs kvPairs = new KVPairs();
    kvPairs.addKeyValuePair(new KeyValuePair("key1", "value1"));
    kvPairs.addKeyValuePair(new KeyValuePair("key2", "value2-a"));
    kvPairs.addKeyValuePair(new KeyValuePair("key2", "value2-b"));
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(kvPairs);
    KVPairs roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, KVPairs.class);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    List<Element.KeyValuePair> elemKVPs = jsonJaxbElem.listKeyValuePairs();
    assertEquals(3, elemKVPs.size(), "elemKVP num");
    List<KeyValuePair> kvps = roundtripped.getKeyValuePairs();
    assertEquals(3, kvps.size(), "roundtripped kvps num");
    assertEquals(jsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "prettyPrint");
  }

  /**
   * Check that JSON can be deserialised into a JAXB object when some of the JSON represents Zimbra KeyValuePairs
   * (i.e. An "_attrs" array).
   * In this case, the target objects for each keyvalue pair use the defaults of "a" for the element name and
   * "n" for the attribute name in the XML form.
   * Desired XML :<br />
   * &lt;key-value-pairs-tester xmlns="urn:zimbraTest">
   *   &lt;a n="key1">value1&lt;/a>
   *   &lt;a n="key2">value2-a&lt;/a>
   *   &lt;a n="key2">value2-b&lt;/a>
   * &lt;/key-value-pairs-tester>
   *<br />
   * Desired JSON :<br />
   * {
   *   "_attrs": {
   *     "key1": "value1",
   *     "key2": [
   *       "value2-a",
   *       "value2-b"]
   *   },
   *   "_jsns": "urn:zimbraTest"
   * }
   */
  @Test
  void zimbraKeyValuePairsAnnotation() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("key-value-pairs", "urn:zimbraTest"));
    jsonElem.addKeyValuePair("key1", "value1");
    jsonElem.addKeyValuePair("key2", "value2-a");
    jsonElem.addKeyValuePair("key2", "value2-b");
    List<KeyValuePair> attrs = Lists.newArrayList();
    attrs.add(new KeyValuePair("key1", "value1"));
    attrs.add(new KeyValuePair("key2", "value2-a"));
    attrs.add(new KeyValuePair("key2", "value2-b"));
    KeyValuePairsTester jaxb = new KeyValuePairsTester(attrs);
    logDebug("XMLElement (from JAXB) ---> prettyPrint\n%1$s",
        JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false).prettyPrint());
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    KeyValuePairsTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, KeyValuePairsTester.class);
    List<Element.KeyValuePair> elemKVPs = jsonJaxbElem.listKeyValuePairs();
    assertEquals(3, elemKVPs.size(), "elemKVP num");
    assertEquals(jsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "prettyPrint");
    List<KeyValuePair> kvps = roundtripped.getAttrList();
    assertEquals(3, kvps.size(), "roundtripped kvps num");
  }

  /**
   * Check that JSON can be deserialised into a JAXB object when some of the JSON represents Zimbra KeyValuePairs
   * (i.e. An "_attrs" array).
   * In this case, the target objects for each keyvalue pair do NOT use the defaults of "a" for the element name and
   * "n" for the attribute name in the XML form.
   * Desired XML :<br />
   * &lt;key-value-pairs-tester xmlns="urn:zimbraTest">
   *   &lt;oddElemName name="key1">value1&lt;/oddElemName>
   *   &lt;oddElemName name="key2">value2-a&lt;/oddElemName>
   *   &lt;oddElemName name="key2">value2-b&lt;/oddElemName>
   * &lt;/key-value-pairs-tester>
   *<br />
   * Desired JSON :<br />
   * {
   *   "_attrs": {
   *     "key1": "value1",
   *     "key2": [
   *       "value2-a",
   *       "value2-b"]
   *   },
   *   "_jsns": "urn:zimbraTest"
   * }
   */
  @Test
  void zimbraOddKeyValuePairsAnnotation() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("key-value-pairs", "urn:zimbraTest"));
    jsonElem.addKeyValuePair("key1", "value1", "oddElemName", "name");
    jsonElem.addKeyValuePair("key2", "value2-a", "oddElemName", "name");
    jsonElem.addKeyValuePair("key2", "value2-b", "oddElemName", "name");
    List<Attr> attrs = Lists.newArrayList();
    attrs.add(new Attr("key1", "value1"));
    attrs.add(new Attr("key2", "value2-a"));
    attrs.add(new Attr("key2", "value2-b"));
    OddKeyValuePairsTester jaxb = new OddKeyValuePairsTester(attrs);
    logDebug("XMLElement (from JAXB) ---> prettyPrint\n%1$s",
        JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false).prettyPrint());
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    String origJson = jsonJaxbElem.prettyPrint();
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", origJson);
    OddKeyValuePairsTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, OddKeyValuePairsTester.class);
    List<Element.KeyValuePair> elemKVPs = jsonJaxbElem.listKeyValuePairs();
    assertEquals(3, elemKVPs.size(), "elemKVP num");
    assertEquals(jsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "prettyPrint");
    List<Attr> kvps = roundtripped.getAttrList();
    assertEquals(3, kvps.size(), "roundtripped kvps num");
    jsonJaxbElem = JacksonUtil.jaxbToJSONElement(roundtripped);
    String finalJson = jsonJaxbElem.prettyPrint();
    if (!origJson.equals(finalJson)) {
      logDebug("JSONElement from roundtripped JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
      assertEquals(origJson, finalJson, "roundtripped JSON pretty print");
    }
  }

  /**
   * Desired JSON :
   * {
   *   "wrapper": {
   *     "_attrs": {
   *       "key1": "value1",
   *       "key2": "value2"
   *     }
   *   },
   *   "_jsns": "urn:zimbraTest"
   * }
   */
  @Test
  void wrappedZimbraKeyValuePairsAnnotation() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("key-value-pairs", "urn:zimbraTest"));
    Element wrapperElem = jsonElem.addUniqueElement("wrapper");
    wrapperElem.addKeyValuePair("key1", "value1");
    wrapperElem.addKeyValuePair("key2", "value2");
    List<KeyValuePair> attrs = Lists.newArrayList();
    attrs.add(new KeyValuePair("key1", "value1"));
    attrs.add(new KeyValuePair("key2", "value2"));
    WrappedKeyValuePairsTester jaxb = new WrappedKeyValuePairsTester(attrs);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(jsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "prettyPrint");
    WrappedKeyValuePairsTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, WrappedKeyValuePairsTester.class);
    List<Element.KeyValuePair> elemKVPs = jsonJaxbElem.getElement("wrapper").listKeyValuePairs();
    assertEquals(2, elemKVPs.size(), "elemKVP num");
    List<KeyValuePair> kvps = roundtripped.getAttrList();
    assertEquals(2, kvps.size(), "roundtripped kvps num");
  }

    /*
# zmsoap -z -t account -m user1 GetDistributionListRequest/dl=grendl@coco.local @by=name
<GetDistributionListResponse xmlns="urn:zimbraAccount">
  <dl id="7a3e8ec5-4892-4b17-9225-cf17e8b3acc9" dynamic="1" name="grendl@coco.local" isOwner="1" isMember="1">
    <a n="mail">grendl@coco.local</a>
    <a n="zimbraMailStatus">enabled</a>
    <a n="zimbraMailAlias">grendl@coco.local</a>
    <a n="description">Wonder at that</a>
    <a n="displayName">Gren DLfun</a>
  </dl>
</GetDistributionListResponse>
# zmsoap --json -z -t account -m user1 GetDistributionListRequest/dl=grendl@coco.local @by=name
{
  "dl": [{
      "name": "grendl@coco.local",
      "id": "7a3e8ec5-4892-4b17-9225-cf17e8b3acc9",
      "dynamic": true,
      "isMember": true,
      "isOwner": true,
      "_attrs": {
        "mail": "grendl@coco.local",
        "zimbraMailStatus": "enabled",
        "zimbraMailAlias": "grendl@coco.local",
        "description": "Wonder at that",
        "displayName": "Gren DLfun"
      }
    }],
  "_jsns": "urn:zimbraAccount"
}

Extract from mailbox.log for creation of this DL by ZWC - demonstrating the different handling of attrs - See Bug 74371

      "CreateDistributionListResponse": [{
          "dl": [{
              "name": "grendl@coco.local",
              "id": "7a3e8ec5-4892-4b17-9225-cf17e8b3acc9",
              "dynamic": true,
              "a": [
                {
                  "n": "memberURL",
                  "_content": "ldap:///??sub?(|(zimbraMemberOf=7a3e8ec5-4892-4b17-9225-cf17e8b3acc9)(zimbraId=de47828e-94dd-45c3-9770-4dbd255564ca))"
                },
                {
                  "n": "mail",
                  "_content": "grendl@coco.local"
                },
                ...
     */
    /**
     * Desired JSON
     */
    // Re-enable when Bug 74371 is fixed? @Test
    public void kvpForCreateDLRespBug74371() throws Exception {
        Element jsonElem = JSONElement.mFactory.createElement(
                QName.get(AccountConstants.E_CREATE_DISTRIBUTION_LIST_RESPONSE, AccountConstants.NAMESPACE_STR));
        populateCreateDlResp(jsonElem);
        Element xmlElem = XMLElement.mFactory.createElement(
                QName.get(AccountConstants.E_CREATE_DISTRIBUTION_LIST_RESPONSE, AccountConstants.NAMESPACE_STR));
        populateCreateDlResp(xmlElem);
        logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
        logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
        List<KeyValuePair> attrs = Lists.newArrayList();
        attrs.add(new KeyValuePair("key1", "value1"));
        attrs.add(new KeyValuePair("key2", "value2"));
        DLInfo dl = new DLInfo("myId", "myRef", "my name",  null, null, null, null, null);
        CreateDistributionListResponse jaxb = new CreateDistributionListResponse(dl);
        Element xmlJaxbElem = JaxbUtil.jaxbToElement(jaxb, XMLElement.mFactory);
        Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
        DLInfo roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, DLInfo.class);
        logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
        logDebug("XMLElement from JAXB ---> prettyPrint\n%1$s", xmlJaxbElem.prettyPrint());
        Element eDL = jsonJaxbElem.getElement(AdminConstants.E_DL);
        List<? extends KeyValuePair> kvps = roundtripped.getAttrList();
        assertEquals(2, kvps.size(), "roundtripped kvps num");
        List<Element.KeyValuePair> elemKVPs = eDL.getElement("a").listKeyValuePairs();
        assertEquals(2, elemKVPs.size(), "elemKVP num");
        assertEquals(jsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "prettyPrint");
    }

  /**
   * Desired JSON
   * {
   *   "dl": [{
   *       "name": "my name",
   *       "id": "myId",
   *       "dynamic": true,
   *       "_attrs": {
   *         "mail": "fun@example.test",
   *         "zimbraMailStatus": "enabled"
   *       }
   *     }],
   *   "_jsns": "urn:zimbraAccount"
   * }
   */
  @Test
  void kvpForGetDLResp() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(
        QName.get(AccountConstants.E_GET_DISTRIBUTION_LIST_RESPONSE, AccountConstants.NAMESPACE_STR));
    populateGetDlResp(jsonElem);
    Element xmlElem = XMLElement.mFactory.createElement(
        QName.get(AccountConstants.E_GET_DISTRIBUTION_LIST_RESPONSE, AccountConstants.NAMESPACE_STR));
    populateGetDlResp(xmlElem);
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    // ObjectInfo declares this field:
    //     @ZimbraKeyValuePairs
    //     @XmlElement(name=AccountConstants.E_A /* a */, required=false)
    //     private final List<KeyValuePair> attrList;
    List<KeyValuePair> attrs = Lists.newArrayList();
    attrs.add(new KeyValuePair("mail", "fun@example.test"));
    attrs.add(new KeyValuePair("zimbraMailStatus", "enabled"));
    DistributionListInfo dl = new DistributionListInfo("myId", "my name",  null, attrs);
    dl.setDynamic(true);
    GetDistributionListResponse jaxb = new GetDistributionListResponse(dl);
    Element xmlJaxbElem = JaxbUtil.jaxbToElement(jaxb, XMLElement.mFactory);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    GetDistributionListResponse roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, GetDistributionListResponse.class);
    GetDistributionListResponse roundtrippedX = JaxbUtil.elementToJaxb(xmlJaxbElem, GetDistributionListResponse.class);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    logDebug("XMLElement from JAXB ---> prettyPrint\n%1$s", xmlJaxbElem.prettyPrint());
    List<? extends KeyValuePair> kvps = roundtripped.getDl().getAttrList();
    assertEquals(2, kvps.size(), "roundtripped kvps num");
    assertEquals(jsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "prettyPrint");

    // ensure that the JAXB handles empty owners OK (not using empty list in JAXB field initializer)
    assertNull(roundtripped.getDl().getOwners(), "roundtripped owner");
    assertNull(roundtrippedX.getDl().getOwners(), "roundtrippedX owner");
  }

  /**
   * Ensuring that JAXB can handle having an owner in a list that is not an empty array when there are no owners
   */
  @Test
  void kvpForGetDLRespWithOwner() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(
        QName.get(AccountConstants.E_GET_DISTRIBUTION_LIST_RESPONSE, AccountConstants.NAMESPACE_STR));
    populateGetDlResp(jsonElem);
    Element xmlElem = XMLElement.mFactory.createElement(
        QName.get(AccountConstants.E_GET_DISTRIBUTION_LIST_RESPONSE, AccountConstants.NAMESPACE_STR));
    populateGetDlResp(xmlElem);
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    List<KeyValuePair> attrs = Lists.newArrayList();
    attrs.add(new KeyValuePair("mail", "fun@example.test"));
    attrs.add(new KeyValuePair("zimbraMailStatus", "enabled"));
    DistributionListInfo dl = new DistributionListInfo("myId", "my name",  null, attrs);
    dl.setDynamic(true);
    DistributionListGranteeInfo grantee = new DistributionListGranteeInfo(GranteeType.usr, "ownerId", "ownerName");
    dl.addOwner(grantee);
    GetDistributionListResponse jaxb = new GetDistributionListResponse(dl);
    Element xmlJaxbElem = JaxbUtil.jaxbToElement(jaxb, XMLElement.mFactory);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    GetDistributionListResponse roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, GetDistributionListResponse.class);
    GetDistributionListResponse roundtrippedX = JaxbUtil.elementToJaxb(xmlJaxbElem, GetDistributionListResponse.class);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    logDebug("XMLElement from JAXB ---> prettyPrint\n%1$s", xmlJaxbElem.prettyPrint());
    List<? extends KeyValuePair> kvps = roundtripped.getDl().getAttrList();
    assertEquals(2, kvps.size(), "roundtripped kvps num");
    assertEquals(1, roundtripped.getDl().getOwners().size(), "roundtripped owner num");
    assertEquals(1, roundtrippedX.getDl().getOwners().size(), "roundtrippedX owner num");
  }

  /**
   * CreateXMbxSearchRequest currently extends AdminKeyValuePairs which uses the {@link ZimbraKeyValuePairs}
   * annotation.
   *
   * Want JSON for KeyValuePairs to look something like :
   *   "_attrs": {
   *     "query": "Kitchen",
   *     "accounts": "*"
   *   },
   */
  @Test
  void keyValuePairMbxSearch() throws Exception {
    final String notifMsg = "Search task %taskId% completed with status %status%. \nImported: %numMsgs% messages. \nSearch query used: %query%.";

    Element legacyElem = JSONElement.mFactory.createElement(XMbxSearchConstants.CREATE_XMBX_SEARCH_REQUEST);
    legacyElem.addKeyValuePair("query", "Kitchen");
    legacyElem.addKeyValuePair("accounts", "*");
    legacyElem.addKeyValuePair("limit", "0");
    legacyElem.addKeyValuePair("notificationMessage", notifMsg);
    logDebug("CreateXmbxSearchRequest JSONElement ---> prettyPrint\n%1$s", legacyElem.prettyPrint());
    // CreateXMbxSearchRequest extends AdminKeyValuePairs which uses ZimbraKeyValuePairs annotation to flag need
    // for special handling required  when serializing to JSON:
    //     @ZimbraKeyValuePairs
    //     @XmlElement(name=AdminConstants.E_A /* a */, required=false)
    //     private List<KeyValuePair> keyValuePairs;
    CreateXMbxSearchRequest jaxb = new CreateXMbxSearchRequest();
    jaxb.addKeyValuePair(new KeyValuePair("query", "Kitchen"));
    jaxb.addKeyValuePair(new KeyValuePair("accounts", "*"));
    jaxb.addKeyValuePair(new KeyValuePair("limit", "0"));
    jaxb.addKeyValuePair(new KeyValuePair("notificationMessage", notifMsg));
    Element elem = JacksonUtil.jaxbToJSONElement(jaxb, XMbxSearchConstants.CREATE_XMBX_SEARCH_REQUEST);
    logDebug("CreateXmbxSearchRequest JSONElement from JAXB ---> prettyPrint\n%1$s", elem.prettyPrint());
    List<Element.KeyValuePair> kvps = elem.listKeyValuePairs();
    assertEquals(4, kvps.size(), "Number of keyValuePairs ");
    Element.KeyValuePair kvp4 = kvps.get(3);
    assertEquals("notificationMessage", kvp4.getKey(), "KeyValuePair notificationMessage key");
    assertEquals(notifMsg, kvp4.getValue(), "KeyValuePair notificationMessage value");
  }

    // Cut down version of com.zimbra.cs.service.admin.ToXML.encodeAttr
    private void encodeAttr(Element parent, String key, String value, String eltname, String attrname) {
        Element e = parent.addNonUniqueElement(eltname);
        e.addAttribute(attrname, key);
        e.setText(value);
    }

    // Cut down version of com.zimbra.cs.service.admin.ToXML.encodeAttrs
    // TODO: test with String[] values
    private void encodeAttrs(Element e, Map<String,Object> attrs, String key) {
        for (Iterator<Entry<String, Object>> iter = attrs.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String,?> entry = iter.next();
            String name = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String[]) {
                String sv[] = (String[]) value;
                for (int i = 0; i < sv.length; i++) {
                    encodeAttr(e, name, sv[i], AdminConstants.E_A, key);
                }
            } else if (value instanceof String) {
                encodeAttr(e, name, (String)value, AdminConstants.E_A, key);
            }
        }
    }

    private void populateCreateDlResp(Element elem) {
        Element eDL = elem.addNonUniqueElement(AdminConstants.E_DL);
        eDL.addAttribute(AdminConstants.A_NAME, "my name");
        eDL.addAttribute(AdminConstants.A_ID, "myId");
        eDL.addAttribute(AdminConstants.A_DYNAMIC, true);
        Map<String,Object> unicodeAttrs = Maps.newHashMap();
        unicodeAttrs.put("key1", "value1");
        String[] strs = {"Hello", "There"};
        unicodeAttrs.put("key2", strs);
        encodeAttrs(eDL, unicodeAttrs, AdminConstants.A_N);
    }

    private void populateGetDlResp(Element elem) {
        Element eDL = elem.addNonUniqueElement(AdminConstants.E_DL);
        eDL.addAttribute(AdminConstants.A_NAME, "my name");
        eDL.addAttribute(AdminConstants.A_ID, "myId");
        eDL.addKeyValuePair("mail", "fun@example.test", AccountConstants.E_A, AccountConstants.A_N);
        eDL.addKeyValuePair("zimbraMailStatus", "enabled", AccountConstants.E_A, AccountConstants.A_N);
        eDL.addAttribute(AdminConstants.A_DYNAMIC, true);
    }

  /**
   * Test for "List of strings" field annotated with {@link XmlElement}.
   * Desired JSON:
   * {
   *   "more": false,
   *   "total": 23,
   *   "dlm": [
   *     {
   *       "_content": "dlmember1@no.where"
   *     },
   *     {
   *       "_content": "dlmember2@no.where"
   *     },
   *     {
   *       "_content": "dlmember3@no.where"
   *     }],
   *   "_jsns": "urn:zimbraAccount"
   * }
   */
  @Test
  void contentList() throws Exception {
    Element legacyElem = JSONElement.mFactory.createElement(AccountConstants.GET_DISTRIBUTION_LIST_MEMBERS_RESPONSE);
    legacyElem.addAttribute(AccountConstants.A_MORE, false);
    legacyElem.addAttribute(AccountConstants.A_TOTAL, 23);
    legacyElem.addNonUniqueElement(AccountConstants.E_DLM).setText("dlmember1@no.where");
    legacyElem.addNonUniqueElement(AccountConstants.E_DLM).setText("dlmember2@no.where");
    legacyElem.addNonUniqueElement(AccountConstants.E_DLM).setText("dlmember3@no.where");
    logDebug("GetDistributionListMembersResponse JSONElement ---> prettyPrint\n%1$s", legacyElem.prettyPrint());
    // GetDistributionListMembersResponse has:
    //      @XmlElement(name=AccountConstants.E_DLM, required=false)
    //      private List<String> dlMembers = Lists.newArrayList();
    GetDistributionListMembersResponse jaxb = new GetDistributionListMembersResponse();
    jaxb.setMore(false);
    jaxb.setTotal(23);
    jaxb.addDlMember("dlmember1@no.where");
    jaxb.addDlMember("dlmember2@no.where");
    jaxb.addDlMember("dlmember3@no.where");
    Element elem = JacksonUtil.jaxbToJSONElement(jaxb, AccountConstants.GET_DISTRIBUTION_LIST_MEMBERS_RESPONSE);
    logDebug("GetDistributionListMembersResponse JSONElement from JAXB ---> prettyPrint\n%1$s", elem.prettyPrint());
    List<Element> dlMembers = elem.listElements(AccountConstants.E_DLM);
    assertEquals(3, dlMembers.size(), "Number of dlMembers");
    Element dlMem3 = dlMembers.get(2);
    assertEquals("dlmember3@no.where", dlMem3.getText(), "dlMember 3");
    assertEquals(23, elem.getAttributeInt(AccountConstants.A_TOTAL), "total");
    assertEquals(false, elem.getAttributeBool(AccountConstants.A_MORE), "more");
    assertEquals(legacyElem.prettyPrint(), elem.prettyPrint(), "prettyPrint");
  }

  /**
   * By default JAXB renders true and false in XML as "true" and "false"
   * For Zimbra SOAP the XML flavor uses "1" and "0"
   * The JSON flavor uses "true" and "false"
   * @throws Exception
     */
  @Test
  void booleanMarshal() throws Exception {
    Element legacy0Elem = JSONElement.mFactory.createElement(MailConstants.NO_OP_RESPONSE);
    legacy0Elem.addAttribute(MailConstants.A_WAIT_DISALLOWED, false);
    logDebug("NoOpResponse JSONElement ---> prettyPrint\n%1$s", legacy0Elem.prettyPrint());
    Element legacy1Elem = JSONElement.mFactory.createElement(MailConstants.NO_OP_RESPONSE);
    legacy1Elem.addAttribute(MailConstants.A_WAIT_DISALLOWED, true);
    Element legacyFalseElem = JSONElement.mFactory.createElement(MailConstants.NO_OP_RESPONSE);
    legacyFalseElem.addAttribute(MailConstants.A_WAIT_DISALLOWED, "false");
    Element legacyTrueElem = JSONElement.mFactory.createElement(MailConstants.NO_OP_RESPONSE);
    legacyTrueElem.addAttribute(MailConstants.A_WAIT_DISALLOWED, "true");
    // NoOpResponse has:
    //     @XmlAttribute(name=MailConstants.A_WAIT_DISALLOWED, required=false)
    //     private ZmBoolean waitDisallowed;
    NoOpResponse jaxb = NoOpResponse.create(false);
    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory);
    logDebug("XMLElement from JAXB (false)---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    assertEquals("0", xmlElem.getAttribute(MailConstants.A_WAIT_DISALLOWED), "false Value of 'waitDisallowed'");
    Element jsonElem = JacksonUtil.jaxbToJSONElement(jaxb, MailConstants.NO_OP_RESPONSE);
    logDebug("NoOpResponse JSONElement from JAXB (false)---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    assertEquals("false", jsonElem.getAttribute(MailConstants.A_WAIT_DISALLOWED), "false Value of 'waitDisallowed'");

    // ensure that marshaling where Boolean has value true works
    jaxb.setWaitDisallowed(true);
    xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory);
    logDebug("XMLElement from JAXB (true) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    assertEquals("1", xmlElem.getAttribute(MailConstants.A_WAIT_DISALLOWED), "true Value of 'waitDisallowed'");
    jsonElem = JacksonUtil.jaxbToJSONElement(jaxb, MailConstants.NO_OP_RESPONSE);
    logDebug("NoOpResponse JSONElement from JAXB (true) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    assertEquals("true", jsonElem.getAttribute(MailConstants.A_WAIT_DISALLOWED), "true Value of 'waitDisallowed'");
    // ensure that unmarshaling where XML Boolean representation is "1" for true works
    jaxb = JaxbUtil.elementToJaxb(legacy1Elem);
    assertEquals(Boolean.TRUE, jaxb.getWaitDisallowed(), "legacy1Elem waitDisallowed value");
    // ensure that unmarshaling where XML Boolean representation is "0" for false works
    jaxb = JaxbUtil.elementToJaxb(legacy0Elem);
    assertEquals(Boolean.FALSE, jaxb.getWaitDisallowed(), "legacy0Elem waitDisallowed value");
    // ensure that unmarshaling where XML Boolean representation is "false" works
    jaxb = JaxbUtil.elementToJaxb(legacyFalseElem);
    assertEquals(Boolean.FALSE, jaxb.getWaitDisallowed(), "legacyFalseElem waitDisallowed value");
    // ensure that unmarshaling where XML Boolean representation is "true" works
    jaxb = JaxbUtil.elementToJaxb(legacyTrueElem);
    assertEquals(Boolean.TRUE, jaxb.getWaitDisallowed(), "legacyTrueElem waitDisallowed value");
  }

    /*
     *
<GetFilterRulesResponse xmlns="urn:zimbraMail">
  <filterRules>
    <filterRule name="filter1.1318830338466.3" active="0">
      <filterTests condition="anyof">
        <headerTest index="0" value="0" stringComparison="contains"
header="X-Spam-Score"/>
      </filterTests>
      <filterActions>
        <actionFlag index="0" flagName="flagged"/>
        <actionStop index="1"/>
      </filterActions>
    </filterRule>
  </filterRules>
</GetFilterRulesResponse>
     */
    private Element mkFilterRulesResponse(Element.ElementFactory factory) {
        Element legacyElem = factory.createElement(MailConstants.GET_FILTER_RULES_RESPONSE);
        Element filterRulesE = legacyElem.addNonUniqueElement(MailConstants.E_FILTER_RULES);
        Element filterRuleE = filterRulesE.addNonUniqueElement(MailConstants.E_FILTER_RULE);
        filterRuleE.addAttribute(MailConstants.A_NAME, "filter.bug65572");
        filterRuleE.addAttribute(MailConstants.A_ACTIVE, false);
        Element filterTestsE = filterRuleE.addNonUniqueElement(MailConstants.E_FILTER_TESTS);
        filterTestsE.addAttribute(MailConstants.A_CONDITION, "anyof");
        Element hdrTestE = filterTestsE.addNonUniqueElement(MailConstants.E_HEADER_TEST);
        hdrTestE.addAttribute(MailConstants.A_INDEX, 0);
        hdrTestE.addAttribute(MailConstants.A_HEADER, "X-Spam-Score");
        hdrTestE.addAttribute(MailConstants.A_CASE_SENSITIVE, false);  /* not actually in above test */
        hdrTestE.addAttribute(MailConstants.A_STRING_COMPARISON, "contains");
        hdrTestE.addAttribute(MailConstants.A_VALUE, "0");
        Element filterActionsE = filterRuleE.addNonUniqueElement(MailConstants.E_FILTER_ACTIONS);
        Element actionFlagE = filterActionsE.addNonUniqueElement(MailConstants.E_ACTION_FLAG);
        actionFlagE.addAttribute(MailConstants.A_FLAG_NAME, "flagged");
        actionFlagE.addAttribute(MailConstants.A_INDEX, 0);
        Element actionStopE = filterActionsE.addNonUniqueElement(MailConstants.E_ACTION_STOP);
        actionStopE.addAttribute(MailConstants.A_INDEX, 1);
        return legacyElem;
    }

  /**
   *
  {
      "filterRules": [{
          "filterRule": [{
              "name": "filter.bug65572",
              "active": false,
              "filterTests": [{
                  "condition": "anyof",
                  "headerTest": [{
                      "index": 0,
                      "header": "X-Spam-Score",
                      "caseSensitive": false,
                      "stringComparison": "contains",
                      "value": "0"
                    }]
                }],
              "filterActions": [{
                  "actionFlag": [{
                      "flagName": "flagged",
                      "index": 0
                    }],
                  "actionStop": [{
                      "index": 1
                    }]
                }]
            }]
        }],
      "_jsns": "urn:zimbraMail"
    }
   */

  /**
   * This also tests {@link XmlElements} - It is used in {@link FilterTests}
   * @throws Exception
     */
  @Test
  void bug65572BooleanAndXmlElements() throws Exception {
    Element legacyXmlElem = mkFilterRulesResponse(XMLElement.mFactory);
    Element legacyJsonElem = mkFilterRulesResponse(JSONElement.mFactory);

    GetFilterRulesResponse jaxb = new GetFilterRulesResponse();
    FilterTests tests = FilterTests.createForCondition("anyof");
    FilterTest.HeaderTest hdrTest = FilterTest.HeaderTest.createForIndexNegative(0, null);
    hdrTest.setHeaders("X-Spam-Score");
    hdrTest.setCaseSensitive(false);
    hdrTest.setStringComparison("contains");
    hdrTest.setValue("0");
    tests.addTest(hdrTest);
    FilterAction.FlagAction flagAction = new FilterAction.FlagAction("flagged");
    flagAction.setIndex(0);
    FilterAction.StopAction stopAction = new FilterAction.StopAction();
    stopAction.setIndex(1);
    FilterRule rule1 = FilterRule.createForNameFilterTestsAndActiveSetting("filter.bug65572", tests, false);
    rule1.addFilterAction(flagAction);
    rule1.addFilterAction(stopAction);
    jaxb.addFilterRule(rule1);
    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory);
    logDebug("legacyXMLElement ---> prettyPrint\n%1$s", legacyXmlElem.prettyPrint());
    logDebug("XMLElement from JAXB ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    // Attribute Ordering not reliable: Assert.assertEquals("XML", legacyXmlElem.prettyPrint(), xmlElem.prettyPrint());
    Element xmlFr = xmlElem.getElement(MailConstants.E_FILTER_RULES).getElement(MailConstants.E_FILTER_RULE);
    assertEquals("filter.bug65572", xmlFr.getAttribute(MailConstants.A_NAME), "XMLElement from JAXB filter rule name");
    assertEquals(false, xmlFr.getAttributeBool(MailConstants.A_ACTIVE), "XMLElement from JAXB filter rule active");
    Element xmlFT = xmlFr.getElement(MailConstants.E_FILTER_TESTS);
    assertEquals("anyof", xmlFT.getAttribute(MailConstants.A_CONDITION), "XMLElement from JAXB filter tests condition");
    Element xmlHdrT = xmlFT.getElement(MailConstants.E_HEADER_TEST);
    assertEquals(0, xmlHdrT.getAttributeInt(MailConstants.A_INDEX), "XMLElement from JAXB filter hdr test index");
    assertEquals("X-Spam-Score", xmlHdrT.getAttribute(MailConstants.A_HEADER), "XMLElement from JAXB filter hdr test hdr");
    assertEquals(false, xmlHdrT.getAttributeBool(MailConstants.A_CASE_SENSITIVE), "XMLElement from JAXB filter hdr test caseSense");
    assertEquals("contains", xmlHdrT.getAttribute(MailConstants.A_STRING_COMPARISON), "XMLElement from JAXB filter hdr test comparison");
    assertEquals(0, xmlHdrT.getAttributeInt(MailConstants.A_VALUE), "XMLElement from JAXB filter hdr test value");
    Element xmlFA = xmlFr.getElement(MailConstants.E_FILTER_ACTIONS);
    Element xmlFlag = xmlFA.getElement(MailConstants.E_ACTION_FLAG);
    assertEquals("flagged", xmlFlag.getAttribute(MailConstants.A_FLAG_NAME), "XMLElement from JAXB action flag name");
    assertEquals(0, xmlFlag.getAttributeInt(MailConstants.A_INDEX), "XMLElement from JAXB action flag index");
    Element xmlStop = xmlFA.getElement(MailConstants.E_ACTION_STOP);
    assertEquals(1, xmlStop.getAttributeInt(MailConstants.A_INDEX), "XMLElement from JAXB action stop index");

    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb, MailConstants.GET_FILTER_RULES_RESPONSE);
    logDebug("GetFilterRulesResponse legacyJSONElement ---> prettyPrint\n%1$s", legacyJsonElem.prettyPrint());
    logDebug("GetFilterRulesResponse JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(legacyJsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "JSON");
    GetFilterRulesResponse roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, GetFilterRulesResponse.class);
    List<FilterRule> rules = roundtripped.getFilterRules();
    assertEquals(1, rules.size(), "num roundtripped rules");
    FilterRule rtRule = rules.get(0);
    assertEquals("filter.bug65572", rtRule.getName(), "roundtripped rule name");
    assertEquals(false, rtRule.isActive(), "roundtripped rule active setting");
    assertEquals(2, rtRule.getActionCount(), "roundtripped rule action count");
    FilterTests rtTests = rtRule.getFilterTests();
    assertEquals("anyof",  rtTests.getCondition(),  "roundtripped filterTests condition");
    List<FilterTest> rtFilterTests = rtTests.getTests();
    assertEquals(1, rtFilterTests.size(), "num roundtripped filter tests");
    FilterTest.HeaderTest rtHdrTest = (FilterTest.HeaderTest) rtFilterTests.get(0);
    assertEquals(0, rtHdrTest.getIndex(), "roundtripped header test index");
    assertEquals("X-Spam-Score", rtHdrTest.getHeaders(), "roundtripped header test header");
    assertEquals(false, rtHdrTest.isCaseSensitive(), "roundtripped header test caseSens");
    assertEquals("contains", rtHdrTest.getStringComparison(), "roundtripped header test stringComparison");
    assertEquals("0", rtHdrTest.getValue(), "roundtripped header test value");
    List<FilterAction> rtActions = rtRule.getFilterActions();
    assertEquals(2, rtActions.size(), "num roundtripped actions");
    FilterAction.FlagAction rtFlagAction = (FilterAction.FlagAction) rtActions.get(0);
    assertEquals("flagged", rtFlagAction.getFlag(), "roundtripped FlagAction name");
    assertEquals(0, rtFlagAction.getIndex(), "roundtripped FlagAction index");
    FilterAction.StopAction rtStopAction = (FilterAction.StopAction) rtActions.get(1);
    assertEquals(1, rtStopAction.getIndex(), "roundtripped StopAction index");
  }

  /**
   * Tests a list of strings.  Was using a JSON Serializer called ContentListSerializer but the ObjectMapper we
   * use for Zimbra JSON now handles lists of strings this way by default.  GetFilterRules uses JAXB rather than
   * Element already.
   * Similar situation using Element based code for GetDistributionListMembers response used multiple calls to:
   *     parent.addNonUniqueElement(AccountConstants.E_DLM).setText(member);
   * Desired JSON :
         {
           "condition": "anyof",
           "inviteTest": [{
               "index": 0,
               "method": [
                 {
                   "_content": "REQUEST"
                 },
                 {
                   "_content": "REPLY"
                 },
                 {
                   "_content": "CANCEL"
                 }]
             }],
           "_jsns": "urn:zimbraMail"
         }
   */
  @Test
  void inviteTestMethods() throws Exception {
    FilterTests tests = FilterTests.createForCondition("anyof");
    FilterTest.InviteTest inviteTest = new FilterTest.InviteTest();
    inviteTest.addMethod("REQUEST");
    inviteTest.addMethod("REPLY");
    inviteTest.addMethod("CANCEL");
    tests.addTest(inviteTest);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tests,
        QName.get(MailConstants.E_FILTER_TESTS, MailConstants.NAMESPACE));
    logDebug("filterTests JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    FilterTests roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, FilterTests.class);
    FilterTest.InviteTest rtInviteTest = (FilterTest.InviteTest) roundtripped.getTests().get(0);
    assertEquals(3, rtInviteTest.getMethods().size(), "roundtripped num methods");
  }

  /**
   * If you just use a pair of annotation introspectors (JacksonAnnotationIntrospector/JaxbAnnotationIntrospector)
   * then "XmlEnumValue"'s are ignored - AnnotationIntrospector.Pair's findEnumValue(Enum<?> e) method won't
   * call the secondary's findEnumValue unless primary's findEnumValue returns null.
   * (if we made JaxbAnnotationIntrospector the primary, this would work but other things wouldn't)
   * To fix this, the current code makes use of ZmPairAnnotationIntrospector which overrides findEnumValue
   * Desired JSON :
   *     {
   *       "fold1": "virtual conversation",
   *       "fold2": [{
   *           "_content": ""
   *         }],
   *       "_jsns": "urn:zimbraTest"
   *     }
   */
  @Test
  void xmlEnumValuesInAttrAndElem() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("enum-tester", "urn:zimbraTest"));
    jsonElem.addAttribute("fold1", ViewEnum.VIRTUAL_CONVERSATION.toString());
    jsonElem.addNonUniqueElement("fold2").addText(ViewEnum.UNKNOWN.toString());
    EnumAttribEnumElem tstr = new EnumAttribEnumElem();
    tstr.setFold1(ViewEnum.VIRTUAL_CONVERSATION);
    tstr.setFold2(ViewEnum.UNKNOWN);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr, QName.get("enum-tester", "urn:zimbraTest"));
    EnumAttribEnumElem roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, EnumAttribEnumElem.class);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    // Want 'virtual conversation' not 'VIRTUAL_CONVERSATION'
    assertEquals(ViewEnum.VIRTUAL_CONVERSATION.toString(), jsonJaxbElem.getAttribute("fold1"), "fold1 value");
    // Want '' not 'UNKNOWN'
    assertEquals(ViewEnum.UNKNOWN.toString(), jsonJaxbElem.getElement("fold2").getText(), "fold2 value");
    assertEquals(ViewEnum.VIRTUAL_CONVERSATION, roundtripped.getFold1(), "roundtripped fold1");
    assertEquals(ViewEnum.UNKNOWN, roundtripped.getFold2(), "roundtripped fold2");
  }


  /**
   * If you just use a pair of annotation introspectors (JacksonAnnotationIntrospector/JaxbAnnotationIntrospector)
   * then "XmlEnumValue"'s are ignored - AnnotationIntrospector.Pair's findEnumValue(Enum<?> e) method won't
   * call the secondary's findEnumValue unless primary's findEnumValue returns null.
   * (if we made JaxbAnnotationIntrospector the primary, this would work but other things wouldn't)
   * To fix this, the current code makes use of ZmPairAnnotationIntrospector which overrides findEnumValue
   * Desired JSON :
   *     {
   *       "fold1": "virtual conversation",
   *       "fold2": "",
   *       "_jsns": "urn:zimbraTest"
   *     }
   */
  @Test
  void xmlEnumValuesInAttributes() throws Exception {
    EnumAttribs tstr = new EnumAttribs();
    tstr.setFold1(ViewEnum.VIRTUAL_CONVERSATION);
    tstr.setFold2(ViewEnum.UNKNOWN);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr, QName.get("enum-tester", "urn:zimbraTest"));
    EnumAttribs roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, EnumAttribs.class);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    // Want 'virtual conversation' not 'VIRTUAL_CONVERSATION'
    assertEquals(ViewEnum.VIRTUAL_CONVERSATION.toString(), jsonJaxbElem.getAttribute("fold1"), "fold1 value");
    // Want '' not 'UNKNOWN'
    assertEquals(ViewEnum.UNKNOWN.toString(), jsonJaxbElem.getAttribute("fold2"), "fold2 value");
    assertEquals(ViewEnum.VIRTUAL_CONVERSATION, roundtripped.getFold1(), "roundtripped fold1");
    assertEquals(ViewEnum.UNKNOWN, roundtripped.getFold2(), "roundtripped fold2");
  }

  /**
   * Testing String attribute and String element.
   * Also testing differing namespaces on package, root element and field element
   * Desired JSON :
   *      {
   *        "attribute-1": "My attribute ONE",
   *        "element1": [{
   *            "_content": "My element ONE",
   *            "_jsns": "urn:ZimbraTest3"
   *          }],
   *        "_jsns": "urn:ZimbraTest2"
   *      }
   */
  @Test
  void stringAttrAndElem() throws Exception {
    final String attr1Val = "My attribute ONE";
    final String elem1Val = "My element ONE";
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("string-tester", "urn:zimbraTest2"));
    jsonElem.addAttribute("attribute-1", attr1Val);
    jsonElem.addNonUniqueElement(QName.get("element1", "urn:zimbraTest3")).addText(elem1Val);
    StringAttrStringElem tstr = new StringAttrStringElem();
    tstr.setAttr1(attr1Val);
    tstr.setElem1(elem1Val);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    Element xmlElem = JaxbUtil.jaxbToElement(tstr, Element.XMLElement.mFactory, true, false);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    StringAttrStringElem roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, StringAttrStringElem.class);
    StringAttrStringElem roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, StringAttrStringElem.class);
    assertEquals(attr1Val, jsonJaxbElem.getAttribute("attribute-1"), "JSONElement attr1");
    assertEquals(elem1Val, jsonJaxbElem.getElement("element1").getText(), "JSONElement elem1");
    assertEquals(attr1Val, roundtrippedX.getAttr1(), "roundtrippedX attr1");
    assertEquals(elem1Val, roundtrippedX.getElem1(), "roundtrippedX elem1");
    assertEquals(attr1Val, roundtripped.getAttr1(), "roundtripped attr1");
    assertEquals(elem1Val, roundtripped.getElem1(), "roundtripped elem1");
  }

  /**
   * Desired JSON :
   * {
   *   "strAttrStrElem": [{
   *       "attribute-1": "My attribute ONE",
   *       "element1": [{
   *           "_content": "My element ONE",
   *           "_jsns": "urn:ZimbraTest3"
   *         }],
   *       "_jsns": "urn:ZimbraTest5"
   *     }],
   *   "_jsns": "urn:ZimbraTest4"
   * }
   */
  @Test
  void elemsInDiffNamespace() throws Exception {
    final String attr1Val = "My attribute ONE";
    final String elem1Val = "My element ONE";
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("ns-delta", "urn:ZimbraTest4"));
    Element saseElem = jsonElem.addNonUniqueElement(QName.get("strAttrStrElem", "urn:ZimbraTest5"));
    saseElem.addAttribute("attribute-1", attr1Val);
    saseElem.addNonUniqueElement(QName.get("element1", "urn:ZimbraTest3")).addText(elem1Val);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    NamespaceDeltaElem tstr = new NamespaceDeltaElem();
    StringAttrStringElem tstrSase = new StringAttrStringElem();
    tstrSase.setAttr1(attr1Val);
    tstrSase.setElem1(elem1Val);
    tstr.setSase(tstrSase);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    NamespaceDeltaElem roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, NamespaceDeltaElem.class);
    Element saseJsonJaxbElem = jsonJaxbElem.getElement(QName.get("strAttrStrElem", "urn:ZimbraTest5"));
    assertEquals(attr1Val, saseJsonJaxbElem.getAttribute("attribute-1"), "JSONElement attr1");
    assertEquals(elem1Val, saseJsonJaxbElem.getElement("element1").getText(), "JSONElement elem1");
    logDebug("roundtripped attr1=%1$s", roundtripped.getSase().getAttr1());
    logDebug("roundtripped elem1=%1$s", roundtripped.getSase().getElem1());
    assertEquals(attr1Val, roundtripped.getSase().getAttr1(), "roundtripped attr1");
    assertEquals(elem1Val, roundtripped.getSase().getElem1(), "roundtripped elem1");
    assertEquals(jsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "prettyPrint");
  }

  /**
   * Desired JSON :
   *      {
   *        "authToken": [{
   *            "_content": "authenticationtoken"
   *          }],
   *        "lifetime": 3000,
   *        "_jsns": "urn:zimbraAdmin"
   *      }
   */
  @Test
  void stringElemAndZimbraJsonlongAttr() throws Exception {
    final long lifetime = 3000;
    final String lifetimeStr = new Long(lifetime).toString();
    final String authToken = "authenticationtoken";
    AuthResponse tstr = new AuthResponse();
    tstr.setAuthToken(authToken);
    tstr.setLifetime(lifetime);
    Element xmlElem = JaxbUtil.jaxbToElement(tstr, Element.XMLElement.mFactory, true, false);
    AuthResponse roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, AuthResponse.class);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    AuthResponse roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, AuthResponse.class);
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(lifetimeStr, jsonJaxbElem.getAttribute("lifetime"), "JSONElement lifetime");
    assertEquals(authToken, jsonJaxbElem.getElement("authToken").getText(), "JSONElement authToken");
    assertEquals(lifetime, roundtripped.getLifetime(), "roundtripped lifetime");
    assertEquals(authToken, roundtripped.getAuthToken(), "roundtripped authToken");
    assertEquals(lifetime, roundtrippedX.getLifetime(), "roundtrippedX lifetime");
    assertEquals(authToken, roundtrippedX.getAuthToken(), "roundtrippedX authToken");
  }

  /**
   * Desired JSON :
   *      {
   *        "attr1": "Attribute ONE",
   *        "_content": "3",
   *        "_jsns": "urn:zimbraTest"
   *      }
   */
  @Test
  void stringAttrintValue() throws Exception {
    final String attrib1 = "Attribute ONE";
    final int value = 3;
    final String valueStr = new Integer(value).toString();
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("string-attr-int-value", "urn:zimbraTest"));
    jsonElem.addAttribute("attr1", attrib1);
    jsonElem.addText(valueStr);
    StringAttribIntValue tstr = new StringAttribIntValue(attrib1, value);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    StringAttribIntValue roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, StringAttribIntValue.class);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(attrib1, jsonJaxbElem.getAttribute("attr1"), "attr1");
    assertEquals(valueStr, jsonJaxbElem.getText(), "XmlValue");
    assertEquals(attrib1, roundtripped.getAttrib1(), "roundtripped attrib1");
    assertEquals(value, roundtripped.getMyValue(), "roundtripped value");
  }

  /**
   * A JSON element added via {@code addNonUniqueElement} is always serialized as an array, because there could be
   * further {@code addNonUniqueElement} calls with the same element name.  On the other hand, a JSON element added via
   * {@code addUniqueElement} won't be serialized as an array as their is an implicit assumption that there will
   * be only one element with that name.
   * Desired JSON :
   * {
   *   "unique-str-elem": {
   *      "_content": "Unique element ONE",
   *      "_jsns": "urn:zimbraTest1"
   *    },
   *    "non-unique-elem": [{
   *        "_content": "Unique element ONE",
   *        "_jsns": "urn:zimbraTest1"
   *      }],
   *    "unique-complex-elem": {
   *      "attr1": "Attribute ONE",
   *      "_content": "3"
   *    },
   *    "_jsns": "urn:zimbraTest"
   * }
   */
  @Test
  void uniqeElementHandling() throws Exception {
    final String elem1 = "Unique element ONE";
    final String attrib1 = "Attribute ONE";
    final int value = 3;
    final String valueStr = new Integer(value).toString();
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("unique-tester", "urn:zimbraTest"));
    jsonElem.addUniqueElement(QName.get("unique-str-elem", "urn:zimbraTest1")).setText(elem1);
    jsonElem.addNonUniqueElement(QName.get("non-unique-elem", "urn:zimbraTest1")).setText(elem1);
    jsonElem.addUniqueElement("unique-complex-elem").addAttribute("attr1", attrib1).setText(valueStr);
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    UniqueTester tstr = new UniqueTester();
    tstr.setUniqueStrElem(elem1);
    tstr.setNonUniqueStrElem(elem1);
    StringAttribIntValue complex = new StringAttribIntValue(attrib1, value);
    tstr.setUniqueComplexElem(complex);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    UniqueTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, UniqueTester.class);
    assertEquals(elem1, roundtripped.getNonUniqueStrElem(), "roundtripped non-unique elem");
    assertEquals(elem1, roundtripped.getUniqueStrElem(), "roundtripped unique elem");
    StringAttribIntValue roundtrippedComplex = tstr.getUniqueComplexElem();
    assertEquals(attrib1, roundtrippedComplex.getAttrib1(), "roundtripped complex attrib1");
    assertEquals(value, roundtrippedComplex.getMyValue(), "roundtripped complex value");
  }

  /**
   *  Testing form:
   *      {@code @XmlElement(name="enum-entry", required=false)
     *      private List<ViewEnum> entries = Lists.newArrayList();}
   * Desired JSON :
   *      {
   *        "enum-entry": [
   *          {
   *            "_content": "appointment"
   *          },
   *          {
   *            "_content": ""
   *          },
   *          {
   *            "_content": "document"
   *          }],
   *        "_jsns": "urn:zimbraTest"
   */
  @Test
  void enumElemList() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("enum-elem-list", "urn:zimbraTest"));
    jsonElem.addNonUniqueElement("enum-entry").addText(ViewEnum.APPOINTMENT.toString());
    jsonElem.addNonUniqueElement("enum-entry").addText(ViewEnum.UNKNOWN.toString());
    jsonElem.addNonUniqueElement("enum-entry").addText(ViewEnum.DOCUMENT.toString());
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    EnumElemList tstr = new EnumElemList();
    tstr.addEntry(ViewEnum.APPOINTMENT);
    tstr.addEntry(ViewEnum.UNKNOWN);
    tstr.addEntry(ViewEnum.DOCUMENT);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    EnumElemList roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, EnumElemList.class);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    List<Element> jsonElems = jsonJaxbElem.listElements();
    List<ViewEnum> entries = roundtripped.getEntries();
    assertEquals(3, jsonElems.size(), "jsonElems num");
    assertEquals(3, entries.size(), "entries num");
    assertTrue(entries.contains(ViewEnum.APPOINTMENT), "has APPOINTMENT");
    assertTrue(entries.contains(ViewEnum.UNKNOWN), "has UNKNOWN");
    assertTrue(entries.contains(ViewEnum.DOCUMENT), "has DOCUMENT");
  }

  /**
   * Desired JSON :
   * {
   *   "_jsns": "urn:zimbraTest"
   * }
   */
  @Test
  void emptyEnumElemList() throws Exception {
    EnumElemList tstr = new EnumElemList();
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    logDebug("JSONElement from JAXB FOR EMPTY LIST ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    EnumElemList roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, EnumElemList.class);
    List<Element> jsonElems = jsonJaxbElem.listElements();
    List<ViewEnum> entries = roundtripped.getEntries();
    assertEquals(0, jsonElems.size(), "jsonElems num");
    assertEquals(0, entries.size(), "entries num");
  }

  /**
   *  Testing form:
   *      {@code @XmlElementWrapper(name="wrapper", required=false)
   *      @XmlElement(name="enum-entry", required=false)
     *      private List<ViewEnum> entries = Lists.newArrayList();}
     * Desired JSON :
     *      {
     *        "wrapper": [{
     *            "enum-entry": [
     *              {
     *                "_content": "appointment"
     *              },
     *              {
     *                "_content": "document"
     *              }]
     *          }],
     *        "_jsns": "urn:zimbraTest"
     *      }
   */
  @Test
  void wrappedEnumElemList() throws Exception {
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("wrapped-enum-elem-list", "urn:zimbraTest"));
    Element wrapElem = jsonElem.addNonUniqueElement("wrapper");
    wrapElem.addNonUniqueElement("enum-entry").addText(ViewEnum.APPOINTMENT.toString());
    wrapElem.addNonUniqueElement("enum-entry").addText(ViewEnum.DOCUMENT.toString());
    logDebug("JSONElement (for comparison) ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    WrappedEnumElemList tstr = new WrappedEnumElemList();
    tstr.addEntry(ViewEnum.APPOINTMENT);
    tstr.addEntry(ViewEnum.DOCUMENT);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    WrappedEnumElemList roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, WrappedEnumElemList.class);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    Element wElem = jsonJaxbElem.getElement("wrapper");
    List<Element> jsonElems = wElem.listElements();
    List<ViewEnum> entries = roundtripped.getEntries();
    assertEquals(2, jsonElems.size(), "jsonElems num");
    assertEquals(2, entries.size(), "entries num");
    assertTrue(entries.contains(ViewEnum.APPOINTMENT), "has APPOINTMENT");
    assertTrue(entries.contains(ViewEnum.DOCUMENT), "has DOCUMENT");
  }

  /** Permissive handling - if required things are not present, users need to handle this */
  @Test
  void missingRequiredStringElem() throws Exception {
    final String attr1Val = "My attribute ONE";
    StringAttrStringElem tstr = new StringAttrStringElem();
    tstr.setAttr1(attr1Val);
    // tstr.setElem1(elem1Val);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    StringAttrStringElem roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, StringAttrStringElem.class);
    assertEquals(attr1Val, jsonJaxbElem.getAttribute("attribute-1"), "JSONElement attr1");
    try {
      jsonJaxbElem.getElement("element1");
    } catch (ServiceException svcE) {
      assertEquals(ServiceException.INVALID_REQUEST, svcE.getCode(), "JSONElement exception when getting missing item:");
    }
    assertEquals(attr1Val, roundtripped.getAttr1(), "roundtripped attr1");
    assertNull(roundtripped.getElem1(), "roundtripped elem1");
  }

  /** Permissive handling - if required things are not present, users need to handle this */
  @Test
  void missingRequiredEnumAttrib() throws Exception {
    EnumAttribEnumElem tstr = new EnumAttribEnumElem();
    // tstr.setFold1(ViewEnum.VIRTUAL_CONVERSATION);
    tstr.setFold2(ViewEnum.UNKNOWN);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    EnumAttribEnumElem roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, EnumAttribEnumElem.class);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(ViewEnum.UNKNOWN.toString(), jsonJaxbElem.getElement("fold2").getText(), "fold2 value");
    assertNull(roundtripped.getFold1(), "roundtripped fold1");
    assertEquals(ViewEnum.UNKNOWN, roundtripped.getFold2(), "roundtripped fold2");
  }

  /** Permissive handling - if required things are not present, users need to handle this */
  @Test
  void missingRequiredWrappedAndInt() throws Exception {
    WrappedRequired tstr = new WrappedRequired();
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(tstr);
    WrappedRequired roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, WrappedRequired.class);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    try {
      jsonJaxbElem.getElement("wrapper");
    } catch (ServiceException svcE) {
      assertEquals(ServiceException.INVALID_REQUEST, svcE.getCode(), "JSONElement exception when getting missing wrapper:");
    }
    try {
      jsonJaxbElem.getAttributeInt("required-int");
    } catch (ServiceException svcE) {
      assertEquals(ServiceException.INVALID_REQUEST, svcE.getCode(), "JSONElement exception when getting missing int:");
    }
    try {
      jsonJaxbElem.getAttributeInt("required-bool");
    } catch (ServiceException svcE) {
      assertEquals(ServiceException.INVALID_REQUEST, svcE.getCode(), "JSONElement exception when getting missing bool:");
    }
    try {
      jsonJaxbElem.getAttributeInt("required-complex");
    } catch (ServiceException svcE) {
      assertEquals(ServiceException.INVALID_REQUEST, svcE.getCode(), "JSONElement exception when getting missing complex element:");
    }
    assertEquals(0, roundtripped.getEntries().size(), "roundtripped entries");
    assertEquals(0, roundtripped.getRequiredInt(), "roundtripped required int");
    assertNull(roundtripped.getRequiredBool(), "roundtripped required ZmBoolean");
    assertNull(roundtripped.getRequiredComplex(), "roundtripped required complex");
  }

  /**
   * How to achieve a wrapped list where no element for the wrapper will appear in the XML if the list is empty.
   * This no longer works!  Fortunately, looks like we don't use any classes similar to WrapperAbsentIfEmpty
   * at the moment.
   * For JUDASPRIEST/Java 7 setNumbers is called with 2 integers.  For main/Java 8
   * it is called with an empty list.
   *     WrapperAbsentIfEmpty.setNumbers(List<Integer>) line: 51
   *     WrapperAbsentIfEmpty$JaxbAccessorM_getNumbers_setNumbers_java_util_List.set(Object, Object) line: 60
   *     Lister$CollectionLister<BeanT,T>.startPacking(BeanT, Accessor<BeanT,T>) line: 298
   *     Lister$CollectionLister<BeanT,T>.startPacking(Object, Accessor) line: 269
   *     Scope<BeanT,PropT,ItemT,PackT>.start(Accessor<BeanT,PropT>, Lister<BeanT,PropT,ItemT,PackT>) line: 142
   *     ArrayERProperty$ItemsLoader.startElement(UnmarshallingContext$State, TagName) line: 119
   *     UnmarshallingContext._startElement(TagName) line: 501
   *     UnmarshallingContext.startElement(TagName) line: 480
   *     InterningXmlVisitor.startElement(TagName) line: 75
   *     SAXConnector.startElement(String, String, String, Attributes) line: 150
   *     DOMScanner.visit(Element) line: 244
   *     DOMScanner.visit(Node) line: 281
   *     DOMScanner.visit(Element) line: 250
   *     DOMScanner.scan(Element) line: 127
   *     UnmarshallerImpl.unmarshal0(Node, JaxBeanInfo) line: 369
   *     UnmarshallerImpl.unmarshal(Node, Class<T>) line: 348
   *     JaxbUtil.rawW3cDomDocToJaxb(Document, Class<?>, boolean) line: 1420
   *     JaxbUtil.w3cDomDocToJaxb(Document, Class<?>, boolean) line: 1392
   *     JaxbUtil.elementToJaxb(Element, Class<?>) line: 1438
   *     JaxbToJsonTest.wrapperAbsentIfEmpty() line: 1516
   */
  @Disabled("Bug:95509 - this is hypothetical functionality that isn't currently used, so low priority")
  @Test
  void wrapperAbsentIfEmpty() throws Exception {
    WrapperAbsentIfEmpty jaxb = new  WrapperAbsentIfEmpty();
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    WrapperAbsentIfEmpty roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, WrapperAbsentIfEmpty.class);
    logDebug("JSONElement from JAXB (empty numbers)--> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertNull(jaxb.getNumbers(), "original getNumbers (empty list)");
    assertNull(roundtripped.getNumbers(), "JSON roundtripped getNumbers (empty list)");
    assertEquals("{\n  \"_jsns\": \"urn:zimbraTest\"\n}", jsonJaxbElem.prettyPrint(), "JSON for empty Numbers");
    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("XmlElement from JAXB (empty numbers) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    WrapperAbsentIfEmpty roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, WrapperAbsentIfEmpty.class);
    assertNull(roundtrippedX.getNumbers(), "XML roundtripped getNumbers (empty list)");
    assertEquals("<wrapper-absent-if-empty xmlns=\"urn:zimbraTest\"/>", xmlElem.prettyPrint(), "XML for empty Numbers");
    jaxb.addNumber(314159);
    jaxb.addNumber(42);
    jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, WrapperAbsentIfEmpty.class);
    logDebug("JSONElement from JAXB (empty numbers)--> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(2, jaxb.getNumbers().size(), "original size of Numbers (2 numbers)");
    assertNotNull(roundtrippedX.getNumbers(), "XML roundtripped getNumbers (when 2 numbers in list)");
    assertEquals(2, roundtripped.getNumbers().size(), "JSON roundtripped size of Numbers (2 numbers)");
    xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("XmlElement from JAXB (empty numbers) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, WrapperAbsentIfEmpty.class);
    assertEquals(2, roundtrippedX.getNumbers().size(), "XML roundtripped size of Numbers (2 numbers)");
    assertEquals(String.format("%s\n  %s\n    %s\n    %s\n  %s\n%s",
        "<wrapper-absent-if-empty xmlns=\"urn:zimbraTest\">",
        "<numbers>",
        "<number>314159</number>",
        "<number>42</number>",
        "</numbers>",
        "</wrapper-absent-if-empty>"), xmlElem.prettyPrint(), "XML when have 2 numbers");
  }

  /**
   * XmlElementRef handling.  Note that slightly counter-intuitively, any name specified is ignored (unless
   * type=JAXBElement.class)
   * <pre>
   *   @XmlElementRef(name="ignored-name-root-elem-name-used-instead", type=StringAttribIntValue.class)
     *   private StringAttribIntValue byRef;
     *  </pre>
     * Desired JSON :
     * {
     *   "string-attr-int-value": [{
     *       "attr1": "my string",
     *       "_content": 321
     *     }],
     *   "_jsns": "urn:zimbraTest"
     * }
   */
  @Test
  void elementRefHandling() throws Exception {
    String str = "my string";
    int num = 321;
    ElementRefTester jaxb = new ElementRefTester();
    StringAttribIntValue inner = new StringAttribIntValue(str, num);
    jaxb.setByRef(inner);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    ElementRefTester roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, ElementRefTester.class);
    ElementRefTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, ElementRefTester.class);
    StringAttribIntValue rtByRef = roundtripped.getByRef();
    StringAttribIntValue rtXmlByRef = roundtrippedX.getByRef();
    assertEquals(str, rtXmlByRef.getAttrib1(), "roundtrippedX str");
    assertEquals(num, rtXmlByRef.getMyValue(), "roundtrippedX num");
    assertEquals(str, rtByRef.getAttrib1(), "roundtripped str");
    assertEquals(num, rtByRef.getMyValue(), "roundtripped num");
  }

  /**
   * XmlElementRefs handling
   * Desired JSON :
   * {
   *   "string-attr-int-value": [{
   *       "attr1": "my string",
   *       "_content": 321
   *     }],
   *   "enumEttribs": [{
   *       "fold1": "chat",
   *       "fold2": "remote folder"
   *     }],
   *   "_jsns": "urn:zimbraTest"
   * }
   */
  @Test
  void elementRefsHandling() throws Exception {
    String str = "my string";
    int num = 321;
    List<Object> elems = Lists.newArrayList();
    ElementRefsTester jaxb = new ElementRefsTester();
    StringAttribIntValue inner = new StringAttribIntValue(str, num);
    elems.add(inner);
    EnumAttribs ea = new EnumAttribs();
    ea.setFold1(ViewEnum.CHAT);
    ea.setFold2(ViewEnum.REMOTE_FOLDER);
    elems.add(ea);
    jaxb.setElems(elems);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    ElementRefsTester roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, ElementRefsTester.class);
    ElementRefsTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, ElementRefsTester.class);
    assertEquals(2, roundtrippedX.getElems().size(), "roundtrippedX num elems");
    assertEquals(2, roundtripped.getElems().size(), "roundtripped num elems");
    StringAttribIntValue rtByRef = (StringAttribIntValue) roundtripped.getElems().get(0);
    StringAttribIntValue rtXmlByRef = (StringAttribIntValue) roundtrippedX.getElems().get(0);
    assertEquals(str, rtXmlByRef.getAttrib1(), "roundtrippedX str");
    assertEquals(num, rtXmlByRef.getMyValue(), "roundtrippedX num");
    assertEquals(str, rtByRef.getAttrib1(), "roundtripped str");
    assertEquals(num, rtByRef.getMyValue(), "roundtripped num");
    EnumAttribs rtea = (EnumAttribs) roundtripped.getElems().get(1);
    assertEquals(ViewEnum.CHAT, rtea.getFold1(), "roundtripped fold1");
    assertEquals(ViewEnum.REMOTE_FOLDER, rtea.getFold2(), "roundtripped fold2");
  }

  /**
   * {@link XmlMixed} handling
   * In the places we use XmlMixed, we have either just text or just elements
   * This tests where we have something that maps to a JAXB object.
   * Side note:  Also tests out case for {@link XmlElementRef} where name is derived from root element.
   */
  @Test
  void mixedHandlingWithJaxbAndNoText() throws Exception {
    String str = "my string";
    int num = 321;
    List<Object> elems = Lists.newArrayList();
    MixedTester jaxb = new MixedTester();
    StringAttribIntValue inner = new StringAttribIntValue(str, num);
    elems.add(inner);
    jaxb.setElems(elems);

    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("XmlElement (for comparison) [Mixed has element] ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    MixedTester roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, MixedTester.class);
    assertEquals(1, roundtrippedX.getElems().size(), "roundtrippedX num elems");
    StringAttribIntValue rtXmlByRef = (StringAttribIntValue) roundtrippedX.getElems().get(0);
    assertEquals(str, rtXmlByRef.getAttrib1(), "roundtrippedX [Mixed has element] str");
    assertEquals(num, rtXmlByRef.getMyValue(), "roundtrippedX [Mixed has element] num");
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement from JAXB [Mixed has element] ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    MixedTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, MixedTester.class);
    assertEquals(1, roundtripped.getElems().size(), "roundtripped [Mixed has element] num elems");
    StringAttribIntValue rtByRef = (StringAttribIntValue) roundtripped.getElems().get(0);
    assertEquals(str, rtByRef.getAttrib1(), "roundtripped [Mixed has element] str");
    assertEquals(num, rtByRef.getMyValue(), "roundtripped [Mixed has element] num");
  }

  /**
   * {@link XmlMixed} handling
   * In the places we use XmlMixed, we typically have either just text or just elements
   * This tests where we have just text.
   */
  @Test
  void mixedHandlingJustText() throws Exception {
    String textStr = "text string";
    List<Object> elems = Lists.newArrayList();
    MixedTester jaxb = new MixedTester();
    elems.add(textStr);
    jaxb.setElems(elems);

    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("XmlElement (for comparison) [Mixed has just text] ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    MixedTester roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, MixedTester.class);
    assertEquals(1, roundtrippedX.getElems().size(), "roundtrippedX [Mixed has just text] num elems");
    assertEquals(textStr, (String) roundtrippedX.getElems().get(0), "roundtrippedX [Mixed has just text] str");
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement from JAXB [Mixed has just text] ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    MixedTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, MixedTester.class);
    assertEquals(1, roundtripped.getElems().size(), "roundtripped [Mixed has just text] num elems");
    assertEquals(textStr, (String) roundtripped.getElems().get(0), "roundtripped [Mixed has just text] str");
  }

  /**
   * {@link XmlAnyElement} and {@link XmlMixed} handling
   * In the places we use XmlMixed, we typically have either just text or just elements - this tests with elements
   * that do NOT map to JAXB classes.
   * Desired JSON:
   * {
   *   "alien": {
   *     "myAttr": "myValue",
   *     "child": {
   *       "_content": "Purple beans"
   *     },
   *     "daughter": {
   *       "age": "23",
   *       "name": "Kate"
   *     },
   *     "_jsns": "urn:foreign"
   *   },
   *   "_jsns": "urn:zimbraTest"
   * }
   */
  @Test
  void mixedAndAnyElementHandlingJustElement() throws Exception {
    List<Object> elems = Lists.newArrayList();
    MixedAnyTester jaxb = new MixedAnyTester();
    DocumentBuilder builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
    org.w3c.dom.Document doc = builder.newDocument();
    org.w3c.dom.Element elem = doc.createElementNS("urn:foreign", "alien");
    elem.setAttribute("myAttr", "myValue");
    org.w3c.dom.Element child = doc.createElementNS("urn:foreign", "child");
    child.setTextContent("Purple beans");
    elem.appendChild(child);
    org.w3c.dom.Element child2 = doc.createElementNS("urn:foreign", "daughter");
    child2.setAttribute("name", "Kate");
    child2.setAttribute("age", "23");
    elem.appendChild(child2);
    elems.add(elem);
    jaxb.setElems(elems);

    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("XmlElement (for comparison) [Mixed w3c element] ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    MixedAnyTester roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, MixedAnyTester.class);
    assertEquals(1, roundtrippedX.getElems().size(), "roundtrippedX [Mixed w3c element] num elems");
    org.w3c.dom.Element w3ce = (org.w3c.dom.Element) roundtrippedX.getElems().get(0);
    assertEquals("alien", w3ce.getLocalName(), "roundtrippedX [Mixed w3c element] elem name");
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement from JAXB [Mixed w3c element] ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    MixedAnyTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, MixedAnyTester.class);
    assertEquals(1, roundtripped.getElems().size(), "roundtripped [Mixed w3c element] num elems");
    org.w3c.dom.Element rtElem = (org.w3c.dom.Element) roundtripped.getElems().get(0);
    assertEquals("alien", rtElem.getTagName(), "roundtripped [Mixed w3c element] elem name");
    assertEquals("urn:foreign", rtElem.getNamespaceURI(), "roundtripped [Mixed w3c element] elem namespace");
  }

  /**
   * {@link XmlAnyElement} handling
   * <pre>
   *     @XmlAnyElement
     *     private List<org.w3c.dom.Element> elems = Lists.newArrayList();
     * </pre>
   */
  @Test
  void anyElementHandling() throws Exception {
    String given = "Given information";
    List<Object> elems = Lists.newArrayList();
    AnyTester jaxb = new AnyTester();
    DocumentBuilder builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
    org.w3c.dom.Document doc = builder.newDocument();
    org.w3c.dom.Element elem = doc.createElementNS("urn:foreign", "alien");
    elem.setAttribute("myAttr", "myValue");
    org.w3c.dom.Element child = doc.createElementNS("urn:foreign", "child");
    child.setTextContent("Purple beans");
    elem.appendChild(child);
    org.w3c.dom.Element child2 = doc.createElementNS("urn:foreign", "daughter");
    child2.setAttribute("name", "Kate");
    child2.setAttribute("age", "23");
    elem.appendChild(child2);
    elems.add(elem);
    org.w3c.dom.Element elem2 = doc.createElementNS("urn:wooky", "may");
    elem2.setAttribute("fourth", "be with you");
    jaxb.setGiven(given);
    jaxb.setElems(Lists.newArrayList(elem, elem2));

    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    AnyTester roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, AnyTester.class);
    assertEquals(given, roundtrippedX.getGiven(), "roundtrippedX given");
    assertEquals(2, roundtrippedX.getElems().size(), "roundtrippedX num elems");
    org.w3c.dom.Element w3ce = roundtrippedX.getElems().get(0);
    assertEquals("alien", w3ce.getLocalName(), "roundtrippedX elem name");
    logDebug("STRING from JAXB ---> prettyPrint\n%1$s", getZimbraJsonJaxbString(jaxb));
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    AnyTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, AnyTester.class);
    assertEquals(given, roundtripped.getGiven(), "roundtripped given");
    assertEquals(2, roundtripped.getElems().size(), "roundtripped num elems");
    org.w3c.dom.Element rtElem = roundtripped.getElems().get(0);
    assertEquals("alien", rtElem.getTagName(), "roundtripped elem name");
    assertEquals("urn:foreign", rtElem.getNamespaceURI(), "roundtripped elem namespace");
  }

  /**
   * {@link XmlAnyAttribute} handling - the field with this annotation needs to be a {@link Map}
   * <pre>
   *     @XmlAnyAttribute
     *     private Map<javax.xml.namespace.QName,Object> extraAttributes = Maps.newHashMap();
     * </pre>
     * Desired JSON:
     * {
     *   "given": "Given information",
     *   "attr2": "222",
     *   "attr1": "First attr",
     *   "_jsns": "urn:zimbraTest"
     * }
   */
  @Test
  void anyAttributeHandling() throws Exception {
    String given = "Given information";
    String first = "First attr";
    int second = 222;
    Map<javax.xml.namespace.QName, Object> extras = Maps.newHashMap();
    extras.put(new javax.xml.namespace.QName("attr1"), first);
    // Would expect this to work with integer but the XML JAXB fails saying can't cast Integer to String
    extras.put(new javax.xml.namespace.QName("attr2"), new Integer(second).toString());
    AnyAttrTester jaxb = new AnyAttrTester();
    jaxb.setGiven(given);
    jaxb.setExtraAttributes(extras);

    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    AnyAttrTester roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, AnyAttrTester.class);
    assertEquals(given, roundtrippedX.getGiven(), "roundtrippedX given");
    Map<javax.xml.namespace.QName, Object> rtXextras = roundtrippedX.getExtraAttributes();
    assertEquals(2, rtXextras.size(), "roundtrippedX num extras");

    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    AnyAttrTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, AnyAttrTester.class);
    assertEquals(given, roundtripped.getGiven(), "roundtripped given");
    Map<javax.xml.namespace.QName, Object> rtextras = roundtripped.getExtraAttributes();
    assertEquals(2, rtextras.size(), "roundtripped num extras");
    for (Entry<javax.xml.namespace.QName, Object> attrib : rtextras.entrySet()) {
      if ("attr1".equals(attrib.getKey().getLocalPart())) {
        assertEquals(first, attrib.getValue(), "attr1 attribute has correct value");
      } else if ("attr2".equals(attrib.getKey().getLocalPart())) {
        assertEquals("222", attrib.getValue(), "attr2 attribute has correct value");
      } else {
        fail("Unexpected attribute name for attrib " + attrib.toString());
      }
    }
  }

  /** XmlTransient handling - Need to ignore fields with {@link XmlElementRef} annotation. */
  @Test
  void transientHandling() throws Exception {
    String str = "my string - should NOT be serialized";
    int num = 321;
    TransientTester jaxb = new TransientTester(str, num);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    Element xmlElem = JaxbUtil.jaxbToElement(jaxb, Element.XMLElement.mFactory, true, false);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    logDebug("XmlElement (for comparison) ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    TransientTester roundtrippedX = JaxbUtil.elementToJaxb(xmlElem, TransientTester.class);
    TransientTester roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, TransientTester.class);
    assertEquals(new Integer(num), roundtrippedX.getNummer(), "roundtrippedX num");
    assertNull(roundtrippedX.getToBeIgnored(), "roundtrippedX str");
    assertEquals(new Integer(num), roundtripped.getNummer(), "roundtripped num");
    assertNull(roundtripped.getToBeIgnored(), "roundtripped str");
  }

  /**
   * <p>Demonstrates that CANNOT have more than one attribute with same name, even if using
   * {@link Element.Disposition.CONTENT} to force treating the attribute as an element in XML.</p>
   * Desired serialization to XML:
   * <pre>
   *     &lt;multi-content-attrs xmlns="urn:zimbraTest">
   *       &lt;soapURL>https://soap.example.test&lt;/soapURL>
   *     &lt;/multi-content-attrs>
   * </pre>
   * Desired serialization to JSON:
   * <pre>
   *     {
   *       "soapURL": "https://soap.example.test",
   *       "_jsns": "urn:zimbraTest"
   *     }
   * </pre>
   */
  @Test
  void multipleDispositionCONTENTAttributes() throws Exception {
    final String httpSoap = "http://soap.example.test";
    final String httpsSoap = "https://soap.example.test";
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("multi-content-attrs", "urn:zimbraTest"));
    Element xmlElem = XMLElement.mFactory.createElement(QName.get("multi-content-attrs", "urn:zimbraTest"));
    jsonElem.addAttribute(AccountConstants.E_SOAP_URL, httpSoap, Element.Disposition.CONTENT);
    jsonElem.addAttribute(AccountConstants.E_SOAP_URL, httpsSoap, Element.Disposition.CONTENT);
    xmlElem.addAttribute(AccountConstants.E_SOAP_URL, httpSoap, Element.Disposition.CONTENT);
    xmlElem.addAttribute(AccountConstants.E_SOAP_URL, httpsSoap, Element.Disposition.CONTENT);
    logDebug("MultiContentAttrs XMLElement ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    logDebug("MultiContentAttrs JSONElement ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    assertEquals(httpsSoap, xmlElem.getAttribute(AccountConstants.E_SOAP_URL), "XMLElement soapURL as attribute");
    assertEquals(1, xmlElem.listElements(AccountConstants.E_SOAP_URL).size(), "XMLElement num soapURL elements");
    assertEquals(httpsSoap, xmlElem.getElement(AccountConstants.E_SOAP_URL).getText(), "XMLElement soapURL as element");
    assertEquals(httpsSoap, jsonElem.getAttribute(AccountConstants.E_SOAP_URL), "JSONElement soapURL as attribute");
    // Note difference from XMLElement - for JSON this is Always an attribute but for XML it can be treated as an element
    assertEquals(0, jsonElem.listElements(AccountConstants.E_SOAP_URL).size(), "JSONElement num soapURL elements");
  }

  /**
   * <p>Exercise annotation {@link ZimbraJsonAttribute} which is needed in JAXB in addition to {@link XmlElement} or
   * {@link XmlElementRef} annotations to provide a field which as an analog of something like:
   * <pre>
   *      jsonElem.addAttribute("xml-elem-json-attr", "XML elem but JSON attribute", Element.Disposition.CONTENT);
   * </pre>
   * Desired XML serialization:
   * <pre>
   *     &lt;XmlElemJsonAttr xmlns="urn:zimbraTest">
   *       &lt;xml-elem-json-attr>XML elem but JSON attribute&lt;/xml-elem-json-attr>
   *       &lt;classic-elem>elem for both XML and JSON&lt;/classic-elem>
   *     &lt;/XmlElemJsonAttr>
   * </pre>
   * Desired JSON serialization:
   * <pre>
   *     {
   *       "xml-elem-json-attr": "XML elem but JSON attribute",
   *       "classic-elem": [{
   *           "_content": "elem for both XML and JSON"
   *         }],
   *       "_jsns": "urn:zimbraTest"
   *     }
   * </pre>
   */
  @Test
  void exerciseZimbraJsonAttribute() throws Exception {
    final String str1 = "XML elem but JSON attribute";
    final String str2 = "elem for both XML and JSON";
    Element jsonElem = JSONElement.mFactory.createElement(QName.get("XmlElemJsonAttr", "urn:zimbraTest"));
    Element xmlElem = XMLElement.mFactory.createElement(QName.get("XmlElemJsonAttr", "urn:zimbraTest"));
    jsonElem.addAttribute("xml-elem-json-attr", str1, Element.Disposition.CONTENT);
    jsonElem.addNonUniqueElement("classic-elem").setText(str2);
    xmlElem.addAttribute("xml-elem-json-attr", str1, Element.Disposition.CONTENT);
    xmlElem.addNonUniqueElement("classic-elem").setText(str2);
    logDebug("XMLElement ---> prettyPrint\n%1$s", xmlElem.prettyPrint());
    logDebug("JSONElement ---> prettyPrint\n%1$s", jsonElem.prettyPrint());
    XmlElemJsonAttr jaxb = new XmlElemJsonAttr(str1, str2);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(jsonElem.prettyPrint(), jsonJaxbElem.prettyPrint(), "JSONElement and JSONElement from JAXB");
    XmlElemJsonAttr roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, XmlElemJsonAttr.class);
    assertEquals(str1, roundtripped.getXmlElemJsonAttr(), "roundtripped xml-elem-json-attr");
    assertEquals(str2, roundtripped.getDefaultElem(), "roundtripped classic-elem");
    assertEquals(str1, jsonElem.getAttribute("xml-elem-json-attr"), "JSONElement xml-elem-json-attr as attribute");
    // Note difference from XMLElement - for JSON this is Always an attribute but for XML it can be treated as an element
    assertEquals(0, jsonElem.listElements("xml-elem-json-attr").size(), "JSONElement num xml-elem-json-attr elements");
  }

  @Test
  void systemRetentionPolicyResponse() throws Exception {
    RetentionPolicy rp = new RetentionPolicy((Iterable<Policy>) null, (Iterable<Policy>) null);
    GetSystemRetentionPolicyResponse jaxb = new GetSystemRetentionPolicyResponse(rp);
    Element jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(jsonEmptyGetSystemRetentionPolicyResponse,
        jsonJaxbElem.prettyPrint(),
        "json string from jaxb");
    GetSystemRetentionPolicyResponse roundtripped =
        JaxbUtil.elementToJaxb(jsonJaxbElem, GetSystemRetentionPolicyResponse.class);
    assertEquals(jaxb.getRetentionPolicy().toString(), roundtripped.getRetentionPolicy().toString(), "roundtripped retention policy");

    rp = new RetentionPolicy(
        Collections.singleton(Policy.newUserPolicy("200")),
        Collections.singleton(Policy.newUserPolicy("400")));
    jaxb = new GetSystemRetentionPolicyResponse(rp);
    jsonJaxbElem = JacksonUtil.jaxbToJSONElement(jaxb);
    logDebug("JSONElement from JAXB ---> prettyPrint\n%1$s", jsonJaxbElem.prettyPrint());
    assertEquals(jsonGetSystemRetentionPolicyResponse,
        jsonJaxbElem.prettyPrint(),
        "json string from jaxb");
    roundtripped = JaxbUtil.elementToJaxb(jsonJaxbElem, GetSystemRetentionPolicyResponse.class);
    assertEquals(jaxb.getRetentionPolicy().toString(), roundtripped.getRetentionPolicy().toString(), "roundtripped retention policy");
  }

    public String getZimbraJsonJaxbString(Object obj) {
            try {
                return JacksonUtil.jaxbToJsonString(JacksonUtil.getObjectMapper(), obj);
            } catch (ServiceException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }
    }
    public String getNonZimbraJsonJaxbString(Object obj) {
            try {
                return JacksonUtil.jaxbToJsonString(getSimpleJsonJaxbMapper(), obj);
            } catch (ServiceException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }
    }

    public ObjectMapper getSimpleJsonJaxbMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(
                AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector()));
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

  @BeforeEach
  public void setup(TestInfo testInfo) {
    Optional<Method> testMethod = testInfo.getTestMethod();
    if (testMethod.isPresent()) {
      this.testName = testMethod.get().getName();
    }
  }

}
