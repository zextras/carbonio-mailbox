// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.google.common.collect.ImmutableList;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.SendMsgRequest;
import com.zimbra.soap.mail.type.Msg;
import com.zimbra.soap.mail.type.MsgToSend;

/**
 * Unit test for {@link SendMsgRequest}.
 *
 * @author ysasaki
 */
public final class SendMsgRequestTest {
     public String testName;

  private static final Logger LOG = LogManager.getLogger(SendMsgRequestTest.class);

  static {
    com.zimbra.common.util.LogManager.setThisLogAndRootToLevel(LOG, Level.INFO);
  }

    private void logInfo(String format, Object ... objects) {
        if (LOG.isInfoEnabled()) {
            LOG.info( testName + ":" + String.format(format, objects));
        }
    }

    @BeforeAll
    public static void init() throws Exception {
    }

  @Test
  void marshal() throws Exception {
    SendMsgRequest req = new SendMsgRequest();
    MsgToSend msg = new MsgToSend();
    msg.setHeaders(ImmutableList.of(new Msg.Header("name1", "value1"), new Msg.Header("name2", "value2")));
    req.setMsg(msg);
    Element jaxbElem = JaxbUtil.jaxbToElement(req);
    logInfo("XML Element from JAXB:" + jaxbElem.toString());
    assertEquals(MailConstants.E_SEND_MSG_REQUEST, jaxbElem.getName(), "SendMsgRequest elem name");
    assertEquals(MailConstants.NAMESPACE_STR, jaxbElem.getQName().getNamespaceURI(), "SendMsgRequest elem ns");
    List<Element> hdrs = jaxbElem.getElement(MailConstants.E_MSG).listElements(MailConstants.E_HEADER);
    assertEquals("name1", hdrs.get(0).getAttribute("name"), "SendMsgRequest header 1 name");
    assertEquals("name2", hdrs.get(1).getAttribute("name"), "SendMsgRequest header 2 name");
    assertEquals("value1", hdrs.get(0).getText(), "SendMsgRequest header 1 value");
    assertEquals("value2", hdrs.get(1).getText(), "SendMsgRequest header 2 value");
  }

  @BeforeEach
  public void setup(TestInfo testInfo) {
    Optional<Method> testMethod = testInfo.getTestMethod();
    if (testMethod.isPresent()) {
      this.testName = testMethod.get().getName();
    }
  }

}
