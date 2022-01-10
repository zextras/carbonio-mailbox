// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.zimbra.client.ZGetMessageParams;
import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZMessage;
import com.zimbra.client.ZMessage.ZMimePart;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.handler.TextEnrichedHandler;

public class TestGetMsg{

    @Rule
    public TestName testInfo = new TestName();
    private static String USER_NAME = null;
    private static final String NAME_PREFIX = TestGetMsg.class.getSimpleName();
    private String mOriginalContentMaxSize;

    @Before
    public void setUp()
    throws Exception {
        String prefix = NAME_PREFIX + "-" + testInfo.getMethodName() + "-";
        USER_NAME = prefix + "user";
        cleanUp();
        TestUtil.createAccount(USER_NAME);
        mOriginalContentMaxSize = TestUtil.getServerAttr(Provisioning.A_zimbraMailContentMaxSize);
    }

    @Test
    public void testPlainMessageContent()
    throws Exception {
        doTestMessageContent(MimeConstants.CT_TEXT_PLAIN, "This is the body of a plain message.");
    }

    @Test
    public void testHtmlMessageContent()
    throws Exception {
        doTestMessageContent(MimeConstants.CT_TEXT_HTML, "<html><head></head><body>HTML message</body></html>");
    }

    @Test
    public void testEnrichedMessageContent()
    throws Exception {
        doTestMessageContent(MimeConstants.CT_TEXT_ENRICHED,
            "<color><param>red</param>Blood</color> is <bold>thicker</bold> than<color><param>blue</param>water</color>.");
    }

    private void doTestMessageContent(String contentType, String body)
    throws Exception {
        ZMailbox mbox = TestUtil.getZMailbox(USER_NAME);
        MessageBuilder mb = new MessageBuilder();

        String subject = NAME_PREFIX + " testMessageContent " + contentType;
        String raw = mb.withSubject(subject).withBody(body).withContentType(contentType).create();
        String msgId = TestUtil.addRawMessage(mbox, raw);
        if (contentType.equals(MimeConstants.CT_TEXT_ENRICHED)) {
            body = TextEnrichedHandler.convertToHTML(body);
        }

        verifyMessageContent(mbox, msgId, false, null, null, false, body, contentType);
        verifyMessageContent(mbox, msgId, true, null, null, false, body, contentType);
        verifyMessageContent(mbox, msgId, false, 24, 24, true, body, contentType);
        verifyMessageContent(mbox, msgId, true, 24, 24, true, body, contentType);

        // Set zimbraMailMaxContentLength and confirm that the content
        // gets truncated.
        TestUtil.setServerAttr(Provisioning.A_zimbraMailContentMaxSize, "24");
        verifyMessageContent(mbox, msgId, false, null, 24, true, body, contentType);
        verifyMessageContent(mbox, msgId, true, null, 24, true, body, contentType);
    }

    private void verifyMessageContent(ZMailbox mbox, String msgId, boolean wantHtml,
                                      Integer requestMaxLength, Integer expectedLength,
                                      boolean expectedTruncated, String body, String contentType)
    throws Exception {
        ZGetMessageParams params = new ZGetMessageParams();
        params.setId(msgId);
        params.setWantHtml(wantHtml);
        params.setMax(requestMaxLength);
        ZMessage msg = mbox.getMessage(params);
        ZMimePart mp = msg.getMimeStructure();

        Assert.assertEquals(expectedTruncated, mp.wasTruncated());
        String expected = body;
        if (expectedLength != null) {
            expected = body.substring(0, expectedLength);
        }

        if (contentType.equals(MimeConstants.CT_TEXT_ENRICHED)) {
            // HTML conversion in TextEnrichedHandler will drop trailing
            // characters when the enriched data is malformed (tags not
            // closed, etc.).
            Assert.assertTrue(mp.getContent().length() > 0);
            Assert.assertTrue(expected.startsWith(mp.getContent()));
        } else {
            Assert.assertEquals(expected, mp.getContent());
        }
    }

    @After
    public void tearDown()
    throws Exception {
        cleanUp();
        TestUtil.setServerAttr(Provisioning.A_zimbraMailContentMaxSize, mOriginalContentMaxSize);
    }

    private void cleanUp()
    throws Exception {
        TestUtil.deleteAccountIfExists(USER_NAME);
    }

    public static void main(String[] args)
    throws Exception {
        TestUtil.cliSetup();
        TestUtil.runTest(TestGetMsg.class);
    }
}
