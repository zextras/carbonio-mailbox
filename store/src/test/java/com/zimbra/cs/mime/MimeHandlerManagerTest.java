// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.handler.TextEnrichedHandler;
import com.zimbra.cs.mime.handler.TextHtmlHandler;
import com.zimbra.cs.mime.handler.UnknownTypeHandler;

/**
 * Unit test for {@link MimeHandlerManager}.
 *
 * @author ysasaki
 */
public class MimeHandlerManagerTest {

    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        MockProvisioning prov = new MockProvisioning();
        prov.clearMimeHandlers();

        MockMimeTypeInfo mime = new MockMimeTypeInfo();
        mime.setMimeTypes("text/html");
        mime.setFileExtensions("html", "htm");
        mime.setHandlerClass(TextHtmlHandler.class.getName());
        prov.addMimeType("text/html", mime);

        mime = new MockMimeTypeInfo();
        mime.setMimeTypes("text/enriched");
        mime.setFileExtensions("txe");
        mime.setHandlerClass(TextEnrichedHandler.class.getName());
        prov.addMimeType("text/enriched", mime);

        mime = new MockMimeTypeInfo();
        mime.setHandlerClass(UnknownTypeHandler.class.getName());
        prov.addMimeType("all", mime);

        mime = new MockMimeTypeInfo();
        mime.setMimeTypes("not/exist");
        mime.setFileExtensions("NotExist");
        mime.setHandlerClass("com.zimbra.cs.mime.handler.NotExist");
        prov.addMimeType("not/exist", mime);

        Provisioning.setInstance(prov);
    }

    @Test
    public void html() throws Exception {
        MimeHandler handler = MimeHandlerManager.getMimeHandler(
                "text/html", "filename.html");
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "text/html", null);
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "text/html", "filename.bogus");
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                null, "filename.html");
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "bogus/type", "filename.html");
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());
    }

    @Test
    public void htm() throws Exception {
        MimeHandler handler = MimeHandlerManager.getMimeHandler(
                "text/html", "filename.htm");
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "text/html", null);
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "text/html", "filename.bogus");
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                null, "filename.htm");
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "bogus/type", "filename.htm");
        Assert.assertEquals(TextHtmlHandler.class, handler.getClass());
    }

    @Test
    public void textEnriched() throws Exception {
        MimeHandler handler = MimeHandlerManager.getMimeHandler(
                "text/enriched", "filename.txe");
        Assert.assertEquals(TextEnrichedHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "text/enriched", null);
        Assert.assertEquals(TextEnrichedHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "text/enriched", "filename.bogus");
        Assert.assertEquals(TextEnrichedHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                null, "filename.txe");
        Assert.assertEquals(TextEnrichedHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "bogus/type", "filename.txe");
        Assert.assertEquals(TextEnrichedHandler.class, handler.getClass());
    }

    @Test
    public void applicationOctetStream() throws Exception {
        MimeHandler handler = MimeHandlerManager.getMimeHandler(
                "application/octet-stream", "filename.exe");
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "application/octet-stream", null);
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "application/octet-stream", "filename.bogus");
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                null, "filename.exe");
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(
                "bogus/type", "filename.exe");
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());
    }

    @Test
    public void nil() throws Exception {
        MimeHandler handler = MimeHandlerManager.getMimeHandler(null, null);
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler(null, "filename.bogus");
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler("bogus/type", null);
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());
    }

    @Test
    public void empty() throws Exception {
        MimeHandler handler = MimeHandlerManager.getMimeHandler("", "");
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler("", "filename.bogus");
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());

        handler = MimeHandlerManager.getMimeHandler("bogus/type", "");
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());
    }

    @Test
    public void classNotFound() throws Exception {
        MimeHandler handler = MimeHandlerManager.getMimeHandler(
                "not/exist", null);
        Assert.assertEquals(UnknownTypeHandler.class, handler.getClass());
    }

}
