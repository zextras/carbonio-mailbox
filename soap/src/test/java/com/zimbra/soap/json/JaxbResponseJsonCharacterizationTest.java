// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.soap.JaxbUtil;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class JaxbResponseJsonCharacterizationTest {

  private final JaxbSampleFactory sampleFactory = new JaxbSampleFactory();
  private final CharacterizationSnapshots snapshots = new CharacterizationSnapshots("responses");

  @TestFactory
  Stream<DynamicTest> responseJsonMatchesSnapshot() {
    return JaxbUtil.getJaxbRequestAndResponseClasses().stream()
        .filter(type -> type.getSimpleName().endsWith("Response"))
        .map(type -> DynamicTest.dynamicTest(type.getName(), () -> assertMatchesSnapshot(type)));
  }

  private void assertMatchesSnapshot(Class<?> responseType) throws Exception {
    snapshots.assertMatches(responseType.getName() + ".json", toJson(responseType), responseType.getName());
  }

  private String toJson(Class<?> responseType) {
    Object response = sampleFactory.create(responseType);
    assertNotNull(response, "cannot build a sample of " + responseType.getName());
    try {
      return JaxbUtil.jaxbToElement(response, JSONElement.mFactory).prettyPrint() + "\n";
    } catch (ServiceException e) {
      return CharacterizationSnapshots.describeFailure("serialization", e);
    }
  }
}
