// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin;

import com.zimbra.soap.admin.message.GetServerRequest;
import com.zimbra.soap.admin.type.ServerSelector;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for {@link GetServerRequest}. Mostly checking that JAXB can cope with immutable objects
 * like {@link ServerSelector}
 */
public class GetServerTest {

  private static Unmarshaller unmarshaller;
  private static Marshaller marshaller;

  @BeforeClass
  public static void init() throws Exception {
    JAXBContext jaxb = JAXBContext.newInstance(GetServerRequest.class);
    unmarshaller = jaxb.createUnmarshaller();
    marshaller = jaxb.createMarshaller();
  }

  @Test
  @Ignore("add required xml files to run")
  public void unmarshallGetServerRequest() throws Exception {
    GetServerRequest result =
        (GetServerRequest)
            unmarshaller.unmarshal(getClass().getResourceAsStream("GetServerRequest.xml"));
    ServerSelector svrSel = result.getServer();
    Assert.assertEquals("server - 'by' attribute", ServerSelector.ServerBy.name, svrSel.getBy());
    Assert.assertEquals("server - value", "fun.example.test", svrSel.getKey());
  }

  @Test
  public void marshallGetServerRequest() throws Exception {
    ServerSelector svrSel = new ServerSelector(ServerSelector.ServerBy.name, "fun.example.net");
    GetServerRequest gsr = new GetServerRequest();
    gsr.setServer(svrSel);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(gsr, out);
    String xml = out.toString("UTF-8");
    Assert.assertTrue(
        "Marshalled XML should end with 'GetServerRequest>'", xml.endsWith("GetServerRequest>"));
  }
}
