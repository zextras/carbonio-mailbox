// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @BeforeAll
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
 void html() throws Exception {
  MimeHandler handler = MimeHandlerManager.getMimeHandler(
    "text/html", "filename.html");
  assertEquals(TextHtmlHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "text/html", null);
  assertEquals(TextHtmlHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "text/html", "filename.bogus");
  assertEquals(TextHtmlHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    null, "filename.html");
  assertEquals(TextHtmlHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "bogus/type", "filename.html");
  assertEquals(TextHtmlHandler.class, handler.getClass());
 }

 @Test
 void htm() throws Exception {
  MimeHandler handler = MimeHandlerManager.getMimeHandler(
    "text/html", "filename.htm");
  assertEquals(TextHtmlHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "text/html", null);
  assertEquals(TextHtmlHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "text/html", "filename.bogus");
  assertEquals(TextHtmlHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    null, "filename.htm");
  assertEquals(TextHtmlHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "bogus/type", "filename.htm");
  assertEquals(TextHtmlHandler.class, handler.getClass());
 }

 @Test
 void textEnriched() throws Exception {
  MimeHandler handler = MimeHandlerManager.getMimeHandler(
    "text/enriched", "filename.txe");
  assertEquals(TextEnrichedHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "text/enriched", null);
  assertEquals(TextEnrichedHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "text/enriched", "filename.bogus");
  assertEquals(TextEnrichedHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    null, "filename.txe");
  assertEquals(TextEnrichedHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "bogus/type", "filename.txe");
  assertEquals(TextEnrichedHandler.class, handler.getClass());
 }

 @Test
 void applicationOctetStream() throws Exception {
  MimeHandler handler = MimeHandlerManager.getMimeHandler(
    "application/octet-stream", "filename.exe");
  assertEquals(UnknownTypeHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "application/octet-stream", null);
  assertEquals(UnknownTypeHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "application/octet-stream", "filename.bogus");
  assertEquals(UnknownTypeHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    null, "filename.exe");
  assertEquals(UnknownTypeHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(
    "bogus/type", "filename.exe");
  assertEquals(UnknownTypeHandler.class, handler.getClass());
 }

 @Test
 void nil() throws Exception {
  MimeHandler handler = MimeHandlerManager.getMimeHandler(null, null);
  assertEquals(UnknownTypeHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler(null, "filename.bogus");
  assertEquals(UnknownTypeHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler("bogus/type", null);
  assertEquals(UnknownTypeHandler.class, handler.getClass());
 }

 @Test
 void empty() throws Exception {
  MimeHandler handler = MimeHandlerManager.getMimeHandler("", "");
  assertEquals(UnknownTypeHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler("", "filename.bogus");
  assertEquals(UnknownTypeHandler.class, handler.getClass());

  handler = MimeHandlerManager.getMimeHandler("bogus/type", "");
  assertEquals(UnknownTypeHandler.class, handler.getClass());
 }

 @Test
 void classNotFound() throws Exception {
  MimeHandler handler = MimeHandlerManager.getMimeHandler(
    "not/exist", null);
  assertEquals(UnknownTypeHandler.class, handler.getClass());
 }

}
