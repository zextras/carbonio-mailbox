// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

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
    @Rule public TestName testName = new TestName();

    private static final Logger LOG = Logger.getLogger(SendMsgRequestTest.class);

    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        LOG.setLevel(Level.INFO);
    }

    private void logInfo(String format, Object ... objects) {
        if (LOG.isInfoEnabled()) {
            LOG.info(testName.getMethodName() + ":" + String.format(format, objects));
        }
    }

    @BeforeClass
    public static void init() throws Exception {
    }

    @Test
    public void marshal() throws Exception {
        SendMsgRequest req = new SendMsgRequest();
        MsgToSend msg = new MsgToSend();
        msg.setHeaders(ImmutableList.of(new Msg.Header("name1", "value1"), new Msg.Header("name2", "value2")));
        req.setMsg(msg);
        Element jaxbElem = JaxbUtil.jaxbToElement(req);
        logInfo("XML Element from JAXB:" + jaxbElem.toString());
        Assert.assertEquals("SendMsgRequest elem name", MailConstants.E_SEND_MSG_REQUEST, jaxbElem.getName());
        Assert.assertEquals("SendMsgRequest elem ns",
                MailConstants.NAMESPACE_STR, jaxbElem.getQName().getNamespaceURI());
        List<Element> hdrs = jaxbElem.getElement(MailConstants.E_MSG).listElements(MailConstants.E_HEADER);
        Assert.assertEquals("SendMsgRequest header 1 name", "name1", hdrs.get(0).getAttribute("name"));
        Assert.assertEquals("SendMsgRequest header 2 name", "name2", hdrs.get(1).getAttribute("name"));
        Assert.assertEquals("SendMsgRequest header 1 value", "value1", hdrs.get(0).getText());
        Assert.assertEquals("SendMsgRequest header 2 value", "value2", hdrs.get(1).getText());
    }

}
