// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.SoapParseException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.soap.JaxbUtil;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class JaxbRequestJsonParsingCharacterizationTest {

  private final JaxbSampleFactory sampleFactory = new JaxbSampleFactory();
  private final CharacterizationSnapshots snapshots = new CharacterizationSnapshots("requests");

  @TestFactory
  Stream<DynamicTest> parsedRequestMatchesSnapshot() {
    return JaxbUtil.getJaxbRequestAndResponseClasses().stream()
        .filter(type -> type.getSimpleName().endsWith("Request"))
        .map(type -> DynamicTest.dynamicTest(type.getName(), () -> assertParsedRequestMatchesSnapshot(type)));
  }

  private void assertParsedRequestMatchesSnapshot(Class<?> requestType) throws Exception {
    String inputFile = requestType.getName() + ".json";
    if (!snapshots.exists(inputFile)) {
      snapshots.write(inputFile, sampleRequestEnvelope(requestType));
    }
    String parsed = describeParsed(snapshots.read(inputFile));
    snapshots.assertMatches(requestType.getName() + ".xml", parsed, requestType.getName());
  }

  private String sampleRequestEnvelope(Class<?> requestType) throws ServiceException {
    Object request = sampleFactory.create(requestType);
    assertNotNull(request, "cannot build a sample of " + requestType.getName());
    Element requestElement = JaxbUtil.jaxbToElement(request, JSONElement.mFactory);
    return SoapProtocol.SoapJS.soapEnvelope(requestElement).prettyPrint() + "\n";
  }

  private String describeParsed(String envelopeJson) {
    try {
      return toXml(parse(envelopeJson));
    } catch (Exception e) {
      return CharacterizationSnapshots.describeFailure("parsing", e);
    }
  }

  private Object parse(String envelopeJson) throws ServiceException, SoapParseException {
    Element envelope = Element.parseJSON(envelopeJson);
    Element request = SoapProtocol.SoapJS.getBodyElement(envelope);
    return JaxbUtil.elementToJaxb(request);
  }

  private String toXml(Object request) throws ServiceException {
    return JaxbUtil.jaxbToElement(request, XMLElement.mFactory).prettyPrint() + "\n";
  }
}
