// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.KeyValuePair;
import com.zimbra.soap.admin.message.GetAllConfigResponse;
import com.zimbra.soap.admin.type.Attr;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for {@link GetAllConfigResponse}. com.zimbra.soap.admin.WSDLAdminTest.getAllConfigTest
 * currently failing due to what looks like metro issue : http://java.net/jira/browse/JAX_WS-807
 * This test uses a capture of the response which appeared to cause issues to make sure that JAXB
 * unmarshalling is ok.
 */
public class GetAllConfigTest {

  private static final Logger LOG = Logger.getLogger(GetAllConfigTest.class);

  private static Unmarshaller unmarshaller;

  static {
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.INFO);
    LOG.setLevel(Level.INFO);
  }

  @BeforeClass
  public static void init() throws Exception {
    JAXBContext jaxb = JAXBContext.newInstance(GetAllConfigResponse.class);
    unmarshaller = jaxb.createUnmarshaller();
  }

  @Test
  @Ignore("add required xml files to run")
  public void unmarshallGetAllConfigResponseTest() throws Exception {
    InputStream is = getClass().getResourceAsStream("GetAllConfigResponse.xml");
    Element elem = Element.parseXML(is);
    List<KeyValuePair> kvps = elem.listKeyValuePairs();
    is.close();
    is = getClass().getResourceAsStream("GetAllConfigResponse.xml");
    GetAllConfigResponse resp = (GetAllConfigResponse) unmarshaller.unmarshal(is);
    Assert.assertNotNull("Response", resp);
    List<Attr> attrs = resp.getAttrs();
    LOG.info(
        "unmarshallGetAllConfigResponseTest:KVPS from elem="
            + kvps.size()
            + " from jaxb="
            + attrs.size());
    Assert.assertTrue("Have some attrs", attrs.size() > 20);
    Assert.assertEquals("Number of attrs from elem and from jaxb agree", kvps.size(), attrs.size());
  }
}
