// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.zimbra.soap.admin.message.MailQueueActionRequest;
import com.zimbra.soap.admin.type.MailQueueAction;
import com.zimbra.soap.admin.type.MailQueueWithAction;
import com.zimbra.soap.admin.type.QueueQuery;
import com.zimbra.soap.admin.type.QueueQueryField;
import com.zimbra.soap.admin.type.ServerWithQueueAction;
import com.zimbra.soap.admin.type.ValueAttrib;

/**
 * Unit test for {@link MailQueueActionRequest}.
 * Mostly checking the handling of @XmlMixed in {@link MailQueueAction}
 * test xml files are hand generated and are not taken from real world data.
 */
public class MailQueueActionTest {

    private static final Logger LOG = Logger.getLogger(MailQueueActionTest.class);
    
    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;

    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        LOG.setLevel(Level.INFO);
    }

    @BeforeAll
    public static void init() throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(MailQueueActionRequest.class);
        unmarshaller = jaxb.createUnmarshaller();
        marshaller = jaxb.createMarshaller();
    }

  @Test
  @Disabled("add required xml files to run")
  void unmarshallMailQueueActionRequestByIdTest()
      throws Exception {
    InputStream is = getClass().getResourceAsStream("MailQueueActionRequestIds.xml");
    MailQueueActionRequest result = (MailQueueActionRequest) unmarshaller.unmarshal(is);
    ServerWithQueueAction svr = result.getServer();
    assertEquals("fun.example.test", svr.getName(), "server - 'name' attribute");
    MailQueueWithAction q = svr.getQueue();
    assertEquals("queueName", q.getName(), "queue - 'name' attribute");
    MailQueueAction action = q.getAction();
    assertEquals(MailQueueAction.QueueAction.requeue, action.getOp(), "action - 'op' attribute");
    assertEquals(MailQueueAction.QueueActionBy.id, action.getBy(), "action - 'by' attribute");
    assertEquals("id1,id2,id3", action.getIds(), "action - content");
    assertNull(action.getQuery(), "action - query");
  }

  @Test
  @Disabled("add required xml files to run")
  void unmarshallMailQueueActionRequestByQueryTest()
      throws Exception {
    InputStream is = getClass().getResourceAsStream("MailQueueActionRequestQuery.xml");
    MailQueueActionRequest result = (MailQueueActionRequest) unmarshaller.unmarshal(is);
    ServerWithQueueAction svr = result.getServer();
    assertEquals("fun.example.test", svr.getName(), "server - 'name' attribute");
    MailQueueWithAction q = svr.getQueue();
    assertEquals("queueName", q.getName(), "queue - 'name' attribute");
    MailQueueAction action = q.getAction();
    assertEquals(MailQueueAction.QueueAction.requeue, action.getOp(), "action - 'op' attribute");
    assertEquals(MailQueueAction.QueueActionBy.query, action.getBy(), "action - 'by' attribute");
    QueueQuery query = action.getQuery();
    assertNotNull(query, "action - query");
    assertNull(query.getLimit(), "query - limit");
    assertNull(query.getOffset(), "query - offset");
    QueueQueryField qqf = query.getFields().get(0);
    assertNotNull(qqf, "field");
    assertEquals("fieldName", qqf.getName(), "field - 'name' attribute");
    ValueAttrib match0 = qqf.getMatches().get(0);
    assertEquals("matchValue", match0.getValue(), "match - 'value' attribute");

    assertNull(action.getIds(), "action - content");
  }

  @Test
  void marshallMailQueueActionRequestByIdTest() throws Exception {
    MailQueueAction action = new MailQueueAction(MailQueueAction.QueueAction.requeue,
        MailQueueAction.QueueActionBy.id, "id1,id2,id3", null);
    MailQueueWithAction mqwa = new MailQueueWithAction("queueName", action);
    ServerWithQueueAction server = new ServerWithQueueAction("fun.example.test", mqwa);
    MailQueueActionRequest gsr = new MailQueueActionRequest(server);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(gsr, out);
    String xml = out.toString("UTF-8");
    if (LOG.isInfoEnabled())
      LOG.info("Xml:\n" + xml);
    assertTrue(xml.endsWith("MailQueueActionRequest>"),
        "Marshalled XML should end with 'MailQueueActionRequest>'");
  }

  @Test
  void marshallMailQueueActionRequestByQueryTest() throws Exception {
    QueueQuery qq = new QueueQuery(null, null);
    QueueQueryField qqf = new QueueQueryField("fieldName");
    qqf.addMatch(new ValueAttrib("matchValue"));
    qq.addField(qqf);
    MailQueueAction action = new MailQueueAction(MailQueueAction.QueueAction.requeue,
        MailQueueAction.QueueActionBy.id, null, qq);
    MailQueueWithAction mqwa = new MailQueueWithAction("queueName", action);
    ServerWithQueueAction server = new ServerWithQueueAction("fun.example.test", mqwa);
    MailQueueActionRequest gsr = new MailQueueActionRequest(server);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    marshaller.marshal(gsr, out);
    String xml = out.toString("UTF-8");
    if (LOG.isInfoEnabled())
      LOG.info("Xml:\n" + xml);
    assertTrue(xml.endsWith("MailQueueActionRequest>"),
        "Marshalled XML should end with 'MailQueueActionRequest>'");
  }
}
