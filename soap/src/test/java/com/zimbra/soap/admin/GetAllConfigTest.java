// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.KeyValuePair;
import com.zimbra.soap.admin.message.GetAllConfigResponse;
import com.zimbra.soap.admin.type.Attr;

/**
 * Unit test for {@link GetAllConfigResponse}.
 * com.zimbra.soap.admin.WSDLAdminTest.getAllConfigTest currently failing
 * due to what looks like metro issue : http://java.net/jira/browse/JAX_WS-807
 * This test uses a capture of the response which appeared to cause issues
 * to make sure that JAXB unmarshalling is ok.
 */
public class GetAllConfigTest {

  private static final Logger LOG = LogManager.getLogger(GetAllConfigTest.class);

    private static Unmarshaller unmarshaller;

  static {
    com.zimbra.common.util.LogManager.setThisLogAndRootToLevel(LOG, Level.INFO);
  }

    @BeforeAll
    public static void init() throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(GetAllConfigResponse.class);
        unmarshaller = jaxb.createUnmarshaller();
    }

  @Test
  @Disabled("add required xml files to run")
  void unmarshallGetAllConfigResponseTest()
      throws Exception {
    InputStream is = getClass().getResourceAsStream("GetAllConfigResponse.xml");
    Element elem = Element.parseXML(is);
    List<KeyValuePair> kvps = elem.listKeyValuePairs();
    is.close();
    is = getClass().getResourceAsStream("GetAllConfigResponse.xml");
    GetAllConfigResponse resp = (GetAllConfigResponse) unmarshaller.unmarshal(is);
    assertNotNull(resp, "Response");
    List<Attr> attrs = resp.getAttrs();
    LOG.info("unmarshallGetAllConfigResponseTest:KVPS from elem=" + kvps.size() + " from jaxb=" + attrs.size());
    assertTrue(attrs.size() > 20, "Have some attrs");
    assertEquals(kvps.size(), attrs.size(), "Number of attrs from elem and from jaxb agree");
  }
}
