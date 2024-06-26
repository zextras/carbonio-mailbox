// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import com.google.common.base.Strings;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.mime.MimeDetect;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.HttpUtil;
import com.zimbra.common.util.ImageUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.extension.ExtensionUtil;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Contact;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.MPartInfo;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.mime.ParsedDocument;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.UserServletException;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;
import com.zimbra.cs.service.mail.UploadScanner;
import com.zimbra.cs.servlet.ETagHeaderFilter;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.StoreManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class NativeFormatter extends Formatter {

    private static final String CONVERSION_PATH = "/extension/convertd";
    public static final String ATTR_INPUTSTREAM = "inputstream";
    public static final String ATTR_MSGDIGEST  = "msgdigest";
    public static final String ATTR_FILENAME  = "filename";
    public static final String ATTR_CONTENTURL = "contenturl";
    public static final String ATTR_CONTENTTYPE = "contenttype";
    public static final String ATTR_CONTENTLENGTH = "contentlength";
    public static final String ATTR_LOCALE  = "locale";
    public static final String RETURN_CODE_NO_RESIZE = "NO_RESIZE";

    private static final Log log = LogFactory.getLog(NativeFormatter.class);

    private final Set<String> scriptableContentTypes = Set.of(MimeConstants.CT_TEXT_HTML,
        MimeConstants.CT_APPLICATION_XHTML,
        MimeConstants.CT_TEXT_XML,
        MimeConstants.CT_APPLICATION_ZIMBRA_DOC,
        MimeConstants.CT_APPLICATION_ZIMBRA_SLIDES,
        MimeConstants.CT_APPLICATION_ZIMBRA_SPREADSHEET,
        MimeConstants.CT_IMAGE_SVG,
        MimeConstants.CT_TEXT_XML_LEGACY,
        MimeConstants.CT_APPLICATION_SHOCKWAVE_FLASH);

    @Override
    public FormatType getType() {
        return FormatType.HTML_CONVERTED;
    }

    @Override
    public Set<MailItem.Type> getDefaultSearchTypes() {
        return EnumSet.of(MailItem.Type.MESSAGE);
    }

    @Override
    public void formatCallback(UserServletContext context) throws IOException, ServiceException, UserServletException, ServletException {
        try {
            sendZimbraHeaders(context, context.resp, context.target);
            HttpUtil.Browser browser = HttpUtil.guessBrowser(context.req);
            if (browser == HttpUtil.Browser.IE) {
                context.resp.addHeader("X-Content-Type-Options", "nosniff"); // turn off content detection..
            }
            if (context.target instanceof Message) {
                handleMessage(context, (Message) context.target);
            } else if (context.target instanceof CalendarItem) {
                // Don't return private appointments/tasks if the requester is not the mailbox owner.
                CalendarItem calItem = (CalendarItem) context.target;
                if (calItem.isPublic() || calItem.allowPrivateAccess(
                        context.getAuthAccount(), context.isUsingAdminPrivileges())) {
                    handleCalendarItem(context, calItem);
                } else {
                    context.resp.sendError(HttpServletResponse.SC_FORBIDDEN, "permission denied");
                }
            } else if (context.target instanceof Contact) {
                handleContact(context, (Contact) context.target);
            } else {
                throw UserServletException.notImplemented("can only handle messages/appointments/tasks/documents");
            }
        } catch (MessagingException me) {
            throw ServiceException.FAILURE(me.getMessage(), me);
        }
    }

    private void handleMessage(UserServletContext context, Message msg) throws IOException, ServiceException, MessagingException, ServletException {
        if (context.hasBody()) {
            List<MPartInfo> parts = Mime.getParts(msg.getMimeMessage());
            MPartInfo body = Mime.getTextBody(parts, false);
            if (body != null) {
                handleMessagePart(context, body.getMimePart(), msg);
            } else {
                context.resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "body not found");
            }
        } else if (context.hasPart()) {
            MimePart mp = getMimePart(msg, context.getPart());
            handleMessagePart(context, mp, msg);
        } else {
            context.resp.setContentType(MimeConstants.CT_TEXT_PLAIN);
            long size = msg.getSize();
            if (size > 0)
                context.resp.setContentLength((int)size);
            InputStream is = msg.getContentStream();
            ByteUtil.copy(is, true, context.resp.getOutputStream(), false);
        }
    }

    private void handleCalendarItem(UserServletContext context, CalendarItem calItem) throws IOException, ServiceException, MessagingException, ServletException {
        if (context.hasPart()) {
            MimePart mp;
            if (context.itemId.hasSubpart()) {
                MimeMessage mbp = calItem.getSubpartMessage(context.itemId.getSubpartId());
                mp = Mime.getMimePart(mbp, context.getPart());
            } else {
                mp = getMimePart(calItem, context.getPart());
            }
            handleMessagePart(context, mp, calItem);
        } else {
            context.resp.setContentType(MimeConstants.CT_TEXT_PLAIN);
            InputStream is = calItem.getRawMessage();
            if (is != null)
                ByteUtil.copy(is, true, context.resp.getOutputStream(), false);
        }
    }

    private void handleContact(UserServletContext context, Contact con) throws IOException, ServiceException, MessagingException, ServletException {
        if (!con.hasAttachment()) {
            context.resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "body not found");
        } else if (context.hasPart()) {
            MimePart mp = Mime.getMimePart(con.getMimeMessage(false), context.getPart());
            handleMessagePart(context, mp, con);
        } else {
            context.resp.setContentType(MimeConstants.CT_TEXT_PLAIN);
            InputStream is = new ByteArrayInputStream(con.getContent());
            ByteUtil.copy(is, true, context.resp.getOutputStream(), false);
        }
    }

    private static final String HTML_VIEW = "html";
    private static final String TEXT_VIEW = "text";

    private void handleMessagePart(UserServletContext context, MimePart mp, MailItem item) throws IOException, MessagingException, ServletException {
        if (mp == null) {
            context.resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "part not found");
        } else {
            String contentType = mp.getContentType();
            String shortContentType = Mime.getContentType(mp);

            if (contentType == null) {
                contentType = MimeConstants.CT_TEXT_PLAIN;
            } else if (shortContentType.equalsIgnoreCase(MimeConstants.CT_APPLICATION_OCTET_STREAM)) {
                if ((contentType = MimeDetect.getMimeDetect().detect(Mime.getFilename(mp), mp.getInputStream())) == null)
                    contentType = MimeConstants.CT_APPLICATION_OCTET_STREAM;
                else
                    shortContentType = contentType;
            }
            // CR or LF in Content-Type causes Chrome to barf, unfortunately
            contentType = contentType.replace('\r', ' ').replace('\n', ' ');

            // IE displays garbage if the content-type header is too long
            HttpUtil.Browser browser = HttpUtil.guessBrowser(context.req);
            if (browser == HttpUtil.Browser.IE && contentType.length() > 80)
                contentType = shortContentType;

            // useful for show original of message attachment
            boolean simpleText = (context.hasView() && context.getView().equals(TEXT_VIEW) &&
                    MimeConstants.CT_MESSAGE_RFC822.equals(contentType));
            if (simpleText) {
                contentType = MimeConstants.CT_TEXT_PLAIN;
            }
            boolean html = checkGlobalOverride(ZAttrProvisioning.A_zimbraAttachmentsViewInHtmlOnly,
                    context.getAuthAccount()) || (context.hasView() && context.getView().equals(HTML_VIEW));
            InputStream in = null;
            try {
                if (!html || ExtensionUtil.getExtension("convertd") == null ||
                        contentType.startsWith(MimeConstants.CT_TEXT_HTML) || contentType.matches(MimeConstants.CT_IMAGE_WILD)) {
                    byte[] data = null;

                    // If this is an image that exceeds the max size, resize it.  Don't resize
                    // gigantic images because ImageIO reads image content into memory.
                    if ((context.hasMaxWidth() || context.hasMaxHeight()) &&
                        (Mime.getSize(mp) < LC.max_image_size_to_resize.intValue())) {
                        try {
                            data = getResizedImageData(mp.getInputStream(), Mime.getContentType(mp),
                                mp.getFileName(), context.getMaxWidth(), context.getMaxHeight());
                        } catch (Exception e) {
                            log.info("Unable to resize image.  Returning original content.", e);
                        }
                    }

                    // Return the data, or resized image if available.
                    long size;
                    String returnCode = null;
                    if (data != null) {
                        returnCode = new String(Arrays.copyOfRange(data, 0,
                            RETURN_CODE_NO_RESIZE.length()), StandardCharsets.UTF_8);
                    }
                    if (data != null && !RETURN_CODE_NO_RESIZE.equals(returnCode)) {
                        in = new ByteArrayInputStream(data);
                        size = data.length;
                    } else {
                        in = mp.getInputStream();
                        String enc = mp.getEncoding();
                        if (enc != null) {
                            enc = enc.toLowerCase();
                        }
                        size = enc == null || "7bit".equals(enc) || "8bit".equals(enc) || "binary".equals(enc) ? mp.getSize() : 0;
                    }
                    if (simpleText) {
                        sendBackBinaryData(context.req, context.resp, in, contentType, Part.INLINE,
                                null, size, true);
                    } else {
                        sendBackOriginalDoc(in, contentType, Mime.getFilename(mp), mp.getDescription(),
                                size, context.req, context.resp);
                    }
                } else {
                    in = mp.getInputStream();
                    handleConversion(context, in, Mime.getFilename(mp), contentType, item.getDigest(), mp.getSize());
                }
            } finally {
                ByteUtil.closeStream(in);
            }
        }
    }

    /**
     * If the image stored in the {@code MimePart} exceeds the given width,
     * shrinks the image and returns the shrunk data.  If the
     * image width is smaller than {@code maxWidth} or resizing is not supported,
     * returns {@code null}.
     */
    public static byte[] getResizedImageData(InputStream in, String contentType, String fileName, Integer maxWidth, Integer maxHeight)
        throws IOException {

        if (maxWidth == null) {
            maxWidth = LC.max_image_size_to_resize.intValue();
        }

        if (maxHeight == null) {
            maxHeight = LC.max_image_size_to_resize.intValue();
        }

        try (ImageInputStream iis = new MemoryCacheImageInputStream(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ImageReader reader = ImageUtil.getImageReader(contentType, fileName);
            if (reader == null) {
                log.debug("No ImageReader available.");
                return null;
            }

            reader.setInput(iis);
            BufferedImage img = reader.read(0);
            int width = img.getWidth();
            int height = img.getHeight();

            if (width <= maxWidth && height <= maxHeight) {
                log.debug("Image %dx%d is less than max %dx%d.  Not resizing.",
                    width, height, maxWidth, maxHeight);
                return RETURN_CODE_NO_RESIZE.getBytes();
            }

            double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
            width *= ratio;
            height *= ratio;

            BufferedImage small = ImageUtil.resize(img, width, height);

            ImageWriter writer = ImageIO.getImageWriter(reader);
            if (writer == null) {
                log.debug("No ImageWriter available.");
                return null;
            }

            try (ImageOutputStream ios = new MemoryCacheImageOutputStream(out)) {
                writer.setOutput(ios);
                writer.write(small);
            } finally {
                writer.dispose();
            }

            return out.toByteArray();
        } finally {
            ByteUtil.closeStream(in);
        }
    }


    private void handleConversion(UserServletContext ctxt, InputStream is, String filename, String ct, String digest, long length) throws IOException, ServletException {
        try {
            ctxt.req.setAttribute(ATTR_INPUTSTREAM, is);
            ctxt.req.setAttribute(ATTR_MSGDIGEST, digest);
            ctxt.req.setAttribute(ATTR_FILENAME, filename);
            ctxt.req.setAttribute(ATTR_CONTENTTYPE, ct);
            ctxt.req.setAttribute(ATTR_CONTENTURL, ctxt.req.getRequestURI());
            ctxt.req.setAttribute(ATTR_CONTENTLENGTH, length);
            Account authAcct = ctxt.getAuthAccount();
            if (null != authAcct) {
                String locale = authAcct.getPrefLocale();
                if (locale != null) {
                    ctxt.req.setAttribute(ATTR_LOCALE, locale);
                }
            }
            RequestDispatcher dispatcher = ctxt.req.getRequestDispatcher(CONVERSION_PATH);
            dispatcher.forward(ctxt.req, ctxt.resp);
        } finally {
            ByteUtil.closeStream(is);
        }
    }

    public static MimePart getMimePart(CalendarItem calItem, String part) throws IOException, MessagingException, ServiceException {
        return Mime.getMimePart(calItem.getMimeMessage(), part);
    }

    public static MimePart getMimePart(Message msg, String part) throws IOException, MessagingException, ServiceException {
        return Mime.getMimePart(msg.getMimeMessage(), part);
    }

    public void sendBackOriginalDoc(InputStream is, String contentType, String filename,
        String desc, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendBackOriginalDoc(is, contentType, filename, desc, 0, req, resp);
    }

    private void sendBackOriginalDoc(InputStream is, String contentType, String filename,
        String desc, long size, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String disp = req.getParameter(UserServlet.QP_DISP);
        disp = (disp == null || disp.toLowerCase().startsWith("i")) ? Part.INLINE : Part.ATTACHMENT;
        if (desc != null && desc.length() <= 2048) { // do not return ridiculously long header.
            if (desc.contains(" ") && !(desc.startsWith("\"") && desc.endsWith("\""))) {
                desc = "\"" + desc.trim() +"\"";
            }
            resp.addHeader("Content-Description", desc);
        }
        resp.setContentType(contentType);
        if (disp.equals(Part.INLINE) && isScriptableContent(contentType)) {
            sendBackBinaryData(req, resp, is, contentType, Part.ATTACHMENT, filename, size);
        } else {
            sendBackBinaryData(req, resp, is, contentType, disp, filename, size);
        }
    }


    @Override
    public boolean supportsSave() {
        return true;
    }

    @Override
    public void saveCallback(UserServletContext context, String contentType, Folder folder, String filename)
            throws IOException, ServiceException, UserServletException
    {
        if (filename == null) {
            Mailbox mbox = folder.getMailbox();
            try {
                ParsedMessage pm = new ParsedMessage(context.getPostBody(), mbox.attachmentsIndexingEnabled());
                DeliveryOptions dopt = new DeliveryOptions().setFolderId(folder).setNoICal(true);
                mbox.addMessage(context.opContext, pm, dopt, null);
                return;
            } catch (ServiceException e) {
                throw new UserServletException(HttpServletResponse.SC_BAD_REQUEST, "error parsing message");
            }
        }

      try (InputStream is = context.getRequestInputStream()) {
        Blob blob = StoreManager.getInstance().storeIncoming(is);
        saveDocument(blob, context, contentType, folder, filename, is);
      }
    }

    private void saveDocument(Blob blob, UserServletContext context, String contentType, Folder folder, String filename, InputStream is)
        throws IOException, ServiceException {
        Mailbox mbox = folder.getMailbox();
        MailItem item = null;

        String creator = context.getAuthAccount() == null ? null : context.getAuthAccount().getName();
        ParsedDocument pd = null;

        try {
            if (contentType == null) {
                contentType = MimeDetect.getMimeDetect().detect(filename);
                if (contentType == null)
                    contentType = MimeConstants.CT_APPLICATION_OCTET_STREAM;
            }

            pd = new ParsedDocument(blob, filename, contentType, System.currentTimeMillis(), creator,
                    context.req.getHeader("X-Zimbra-Description"), true);

            item = mbox.getItemByPath(context.opContext, filename, folder.getId());
            // XXX: should we just overwrite here instead?
            // scan upload for viruses
            StringBuffer info = new StringBuffer();
            UploadScanner.Result result = UploadScanner.acceptStream(is, info);
            if (result == UploadScanner.REJECT)
                throw MailServiceException.UPLOAD_REJECTED(filename, info.toString());
            if (result == UploadScanner.ERROR)
                throw MailServiceException.SCAN_ERROR(filename);

        } catch (NoSuchItemException ignored) {
        }

        sendZimbraHeaders(context, context.resp, item);
    }

    @SuppressWarnings("UastIncorrectHttpHeaderInspection")
    public static void sendZimbraHeaders(UserServletContext context, HttpServletResponse resp, MailItem item) {
        if (resp == null || item == null)
            return;

        if (context.wantCustomHeaders) {
            resp.addHeader("X-Zimbra-ItemId", item.getId() + "");
            resp.addHeader("X-Zimbra-Version", item.getVersion() + "");
            resp.addHeader("X-Zimbra-Modified", item.getChangeDate() + "");
            resp.addHeader("X-Zimbra-Change", item.getModifiedSequence() + "");
            resp.addHeader("X-Zimbra-Revision", item.getSavedSequence() + "");
            resp.addHeader("X-Zimbra-ItemType", item.getType().toString());
            try {
                String val = item.getName();
                if (!StringUtil.isAsciiString(val)) {
                    val = MimeUtility.encodeText(val, "utf-8", "B");
                }
                resp.addHeader("X-Zimbra-ItemName", val);
                val = item.getPath();
                if (!StringUtil.isAsciiString(val)) {
                    val = MimeUtility.encodeText(val, "utf-8", "B");
                }
                resp.addHeader("X-Zimbra-ItemPath", val);
            } catch (UnsupportedEncodingException | ServiceException ignored) {
            }
        }

        // set Last-Modified header to date when item's content was last modified
        resp.addDateHeader("Last-Modified", item.getDate());
        // set ETag header to item's mod_content value
        resp.addHeader("ETag", String.valueOf(item.getSavedSequence()));
        resp.addHeader(ETagHeaderFilter.ZIMBRA_ETAG_HEADER, String.valueOf(item.getSavedSequence()));
    }

    private static final int READ_AHEAD_BUFFER_SIZE = 256;
    private static final byte[][] SCRIPT_PATTERN = {
        { '<', 's', 'c', 'r', 'i', 'p', 't' },
        { '<', 'S', 'C', 'R', 'I', 'P', 'T' }
    };

    public void sendBackBinaryData(HttpServletRequest req, HttpServletResponse resp, InputStream in,
                                          String contentType, String disposition, String filename, long size)
    throws IOException {
        sendBackBinaryData(req, resp, in, contentType, disposition, filename, size, false);
    }

    public void sendBackBinaryData(HttpServletRequest req, HttpServletResponse resp, InputStream in,
                                          String contentType, String disposition, String filename,
                                          long size, boolean ignoreContentDisposition)
    throws IOException {
        resp.setContentType(contentType);
        if (disposition == null) {
            String disp = req.getParameter(UserServlet.QP_DISP);
            disposition = (disp == null || disp.toLowerCase().startsWith("i") ) ? Part.INLINE : Part.ATTACHMENT;
        }
        PushbackInputStream pis = new PushbackInputStream(in, READ_AHEAD_BUFFER_SIZE);
        boolean isSafe = false;
        HttpUtil.Browser browser = HttpUtil.guessBrowser(req);
        if (browser != HttpUtil.Browser.IE) {
            isSafe = true;
        } else if (disposition.equals(Part.ATTACHMENT)) {
            isSafe = true;
            if (isScriptableContent(contentType)) {
                //noinspection UastIncorrectHttpHeaderInspection
                resp.addHeader("X-Download-Options", "noopen"); // ask it to save the file
            }
        }

        if (!isSafe) {
            byte[] buf = new byte[READ_AHEAD_BUFFER_SIZE];
            int bytesRead = pis.read(buf, 0, READ_AHEAD_BUFFER_SIZE);
            boolean hasScript;
            for (int i = 0; i < bytesRead; i++) {
                if (buf[i] == SCRIPT_PATTERN[0][0] || buf[i] == SCRIPT_PATTERN[1][0]) {
                    hasScript = true;
                    for (int pos = 1; pos < 7 && (i + pos) < bytesRead; pos++) {
                        if (buf[i+pos] != SCRIPT_PATTERN[0][pos] &&
                                buf[i+pos] != SCRIPT_PATTERN[1][pos]) {
                            hasScript = false;
                            break;
                        }
                    }
                    if (hasScript) {
                        resp.addHeader("Cache-Control", "no-transform");
                        disposition = Part.ATTACHMENT;
                        break;
                    }
                }
            }
            if (bytesRead > 0)
                pis.unread(buf, 0, bytesRead);
        }
        if (!ignoreContentDisposition) {
            String cd = HttpUtil.createContentDisposition(req, disposition, filename == null ? "unknown" : filename);
            resp.addHeader("Content-Disposition", cd);
        }
        if (size > 0)
            resp.setContentLength((int)size);
        ByteUtil.copy(pis, true, resp.getOutputStream(), false);
    }
    /**
     * Determines whether the contentType passed might contain script or other unsavory tags.
     * @param contentType The content type to check
     * @return true if there's a possibility that <script> is valid, false if not
     */
    private boolean isScriptableContent(String contentType) {
        if (Strings.isNullOrEmpty(contentType)) {
            return false;
        }
        contentType = Mime.getContentType(contentType).toLowerCase();
        // only set no-open for 'script type content'
        return scriptableContentTypes.contains(contentType);

    }
}
