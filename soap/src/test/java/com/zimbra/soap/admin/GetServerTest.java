// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin;

import com.zimbra.soap.admin.message.GetServerRequest;
import com.zimbra.soap.admin.type.ServerSelector;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link GetServerRequest}.
 * Mostly checking that JAXB can cope with immutable objects like {@link ServerSelector}
 */
public class GetServerTest {

    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;

    @BeforeAll
    public static void init() throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(GetServerRequest.class);
        unmarshaller = jaxb.createUnmarshaller();
        marshaller = jaxb.createMarshaller();
    }

  @Test
  @Disabled("add required xml files to run")
  void unmarshallGetServerRequest() throws Exception {
    GetServerRequest result = (GetServerRequest) unmarshaller.unmarshal(
        getClass().getResourceAsStream("GetServerRequest.xml"));
    ServerSelector svrSel = result.getServer();
    assertEquals(ServerSelector.ServerBy.name, svrSel.getBy(), "server - 'by' attribute");
    assertEquals("fun.example.test", svrSel.getKey(), "server - value");
  }

  @Test
  void marshallGetServerRequest() throws Exception {
    ServerSelector svrSel = new ServerSelector(ServerSelector.ServerBy.name, "fun.example.net");
    GetServerRequest gsr = new GetServerRequest();
    gsr.setServer(svrSel);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(gsr, out);
    String xml = out.toString("UTF-8");
    assertTrue(xml.endsWith("GetServerRequest>"), "Marshalled XML should end with 'GetServerRequest>'");
  }
}
