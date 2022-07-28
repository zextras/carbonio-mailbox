// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.HttpUtil;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.L10nUtil.MsgKey;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.html.DefangFactory;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.util.TagUtil;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.cs.util.AccountUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpException;

/**
 * The content servlet returns an attachment document in its original format. If attachment needs to
 * be converted, the control is passed down the filter chain to ConversionServlet.
 */
public class ContentServlet extends ZimbraServlet {
  private static final long serialVersionUID = 6466028729668217319L;

  public static final String SERVLET_PATH =
      AccountConstants.CONTENT_SERVLET_PATH; /* "/service/content" */

  public static final String PREFIX_GET = "/get";
  protected static final String PREFIX_PROXY = "/proxy";

  public static final String PARAM_MSGID = "id";
  protected static final String PARAM_UPLOAD_ID = "aid";
  protected static final String PARAM_PART = "part";
  protected static final String PARAM_FORMAT = "fmt";
  protected static final String PARAM_DUMPSTER = "dumpster";
  protected static final String PARAM_SYNC = "sync";
  protected static final String PARAM_EXPUNGE = "expunge";

  protected static final String FORMAT_RAW = "raw";
  protected static final String FORMAT_DEFANGED_HTML = "htmldf";
  protected static final String FORMAT_DEFANGED_HTML_NOT_IMAGES = "htmldfi";

  protected static final String CONVERSION_PATH = "/extension/convertd";
  protected static final String ATTR_MIMEPART = "mimepart";
  protected static final String ATTR_MSGDIGEST = "msgdigest";
  protected static final String ATTR_CONTENTURL = "contenturl";

  protected static final String MSGPAGE_BLOCK = "errorpage.attachment.blocked";
  private String mBlockPage = null;

  private static final Log mLog = LogFactory.getLog(ContentServlet.class);

  private void getCommand(HttpServletRequest req, HttpServletResponse resp, AuthToken token)
      throws ServletException, IOException {
    ItemId itemId = getItemId(req, resp);
    if (itemId == null) {
      return;
    }

    String part = req.getParameter(PARAM_PART);
    String fmt = req.getParameter(PARAM_FORMAT);
    String dumpsterParam = req.getParameter(PARAM_DUMPSTER);

    try {
      // need to proxy the fetch if the mailbox lives on another server
      if (!itemId.isLocal()) {
        // wrong server; proxy to the right one...
        proxyServletRequest(req, resp, itemId.getAccountId());
        return;
      }

      String authId = token.getAccountId();
      String accountId = itemId.getAccountId() != null ? itemId.getAccountId() : authId;
      AccountUtil.addAccountToLogContext(
          Provisioning.getInstance(), accountId, ZimbraLog.C_NAME, ZimbraLog.C_ID, token);
      if (!accountId.equalsIgnoreCase(authId)) {
        ZimbraLog.addToContext(ZimbraLog.C_AID, authId);
      }

      Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(accountId);
      if (mbox == null) {
        resp.sendError(
            HttpServletResponse.SC_BAD_REQUEST,
            L10nUtil.getMessage(MsgKey.errMailboxNotFound, req));
        return;
      }
      ZimbraLog.addMboxToContext(mbox.getId());

      MailItem item =
          mbox.getItemById(
              new OperationContext(token),
              itemId.getId(),
              MailItem.Type.UNKNOWN,
              isFromDumpster(dumpsterParam));
      if (item == null) {
        resp.sendError(
            HttpServletResponse.SC_BAD_REQUEST,
            L10nUtil.getMessage(MsgKey.errMessageNotFound, req));
        return;
      }

      try {
        if (part == null) {
          // they want the entire message...
          boolean sync = "1".equals(req.getParameter(PARAM_SYNC));
          StringBuilder header = new StringBuilder();
          if (sync) {
            // for sync, return metadata as headers to avoid extra SOAP round-trips
            resp.addHeader("X-Zimbra-Tags", TagUtil.getTagIdString(item));
            resp.addHeader("X-Zimbra-Tag-Names", TagUtil.encodeTags(item.getTags()));
            resp.addHeader("X-Zimbra-Flags", item.getFlagString());
            resp.addHeader("X-Zimbra-Received", Long.toString(item.getDate()));
            resp.addHeader("X-Zimbra-Modified", Long.toString(item.getChangeDate()));
            // also return metadata inline in the message content for now
            header.append("X-Zimbra-Tags: ").append(TagUtil.getTagIdString(item)).append("\n");
            header.append("X-Zimbra-Tag-Names: ").append(TagUtil.encodeTags(item.getTags()));
            header.append("X-Zimbra-Flags: ").append(item.getFlagString()).append("\n");
            header.append("X-Zimbra-Received: ").append(item.getDate()).append("\n");
            header.append("X-Zimbra-Modified: ").append(item.getChangeDate()).append("\n");
          }

          if (item instanceof Message) {
            Message msg = (Message) item;
            if (sync) {
              resp.addHeader("X-Zimbra-Conv", Integer.toString(msg.getConversationId()));
              header.append("X-Zimbra-Conv: ").append(msg.getConversationId()).append("\n");
              resp.getOutputStream().write(header.toString().getBytes());
            }
            resp.setContentType(MimeConstants.CT_TEXT_PLAIN);
            ByteUtil.copy(msg.getContentStream(), true, resp.getOutputStream(), false);
          } else if (item instanceof CalendarItem) {
            CalendarItem calItem = (CalendarItem) item;
            if (sync) {
              resp.getOutputStream().write(header.toString().getBytes());
            }

            resp.setContentType(MimeConstants.CT_TEXT_PLAIN);
            if (itemId.hasSubpart()) {
              int invId = itemId.getSubpartId();
              MimeMessage mm = calItem.getSubpartMessage(invId);
              if (mm != null) mm.writeTo(resp.getOutputStream());
            } else {
              InputStream is = calItem.getRawMessage();
              if (is != null) ByteUtil.copy(is, true, resp.getOutputStream(), false);
            }
          }
        } else {
          MimePart mp = null;
          if (item instanceof Message) {
            mp = getMimePart((Message) item, part);
          } else {
            CalendarItem calItem = (CalendarItem) item;
            if (itemId.hasSubpart()) {
              MimeMessage mbp = calItem.getSubpartMessage(itemId.getSubpartId());
              if (mbp != null) mp = Mime.getMimePart(mbp, part);
            } else {
              mp = getMimePart(calItem, part);
            }
          }
          if (mp != null) {
            String contentType = mp.getContentType();
            if (contentType == null) {
              contentType = MimeConstants.CT_APPLICATION_OCTET_STREAM;
            }
            if (contentType.toLowerCase().startsWith(MimeConstants.CT_TEXT_HTML)
                && (FORMAT_DEFANGED_HTML.equals(fmt)
                    || FORMAT_DEFANGED_HTML_NOT_IMAGES.equals(fmt))) {
              sendbackDefangedHtml(mp, contentType, resp, fmt);
            } else {
              if (!isTrue(
                  ZAttrProvisioning.A_zimbraAttachmentsViewInHtmlOnly, mbox.getAccountId())) {
                sendbackOriginalDoc(mp, contentType, req, resp);
              } else {
                req.setAttribute(ATTR_MIMEPART, mp);
                req.setAttribute(ATTR_MSGDIGEST, item.getDigest());
                req.setAttribute(ATTR_CONTENTURL, req.getRequestURL().toString());
                RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher(CONVERSION_PATH);
                dispatcher.forward(req, resp);
              }
            }
            return;
          }
          resp.sendError(
              HttpServletResponse.SC_BAD_REQUEST, L10nUtil.getMessage(MsgKey.errPartNotFound, req));
        }
      } catch (MessagingException e) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    } catch (NoSuchItemException e) {
      resp.sendError(
          HttpServletResponse.SC_NOT_FOUND, L10nUtil.getMessage(MsgKey.errNoSuchItem, req));
    } catch (ServiceException e) {
      returnError(resp, e);
    } catch (HttpException e) {
      throw new IOException("Unknown error", e);
    } finally {
      ZimbraLog.clearContext();
    }
  }

  private ItemId getItemId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ItemId itemId;
    try {
      itemId = new ItemId(req.getParameter(PARAM_MSGID), (String) null);
    } catch (ServiceException e) {
      resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST, L10nUtil.getMessage(MsgKey.errInvalidId, req));
      return null;
    }
    return itemId;
  }

  private boolean isFromDumpster(String dumpsterParam) {
    return dumpsterParam != null
        && !dumpsterParam.equals("0")
        && !dumpsterParam.equalsIgnoreCase("false");
  }

  private void retrieveUpload(HttpServletRequest req, HttpServletResponse resp, AuthToken authToken)
      throws IOException {
    // if it's another server fetching an already-uploaded file, just do that
    String uploadId = req.getParameter(PARAM_UPLOAD_ID);
    if (uploadId == null) {
      resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST, L10nUtil.getMessage(MsgKey.errMissingUploadId, req));
      return;
    }

    try {
      if (!FileUploadServlet.isLocalUpload(uploadId)) {
        // wrong server; proxy to the right one...
        String serverId = FileUploadServlet.getUploadServerId(uploadId);
        Server server = Provisioning.getInstance().get(Key.ServerBy.id, serverId);
        proxyServletRequest(req, resp, server, null);
        return;
      }

      Upload up = FileUploadServlet.fetchUpload(authToken.getAccountId(), uploadId, authToken);
      if (up == null) {
        resp.sendError(
            HttpServletResponse.SC_BAD_REQUEST, L10nUtil.getMessage(MsgKey.errNoSuchUpload, req));
        return;
      }

      String filename = up.getName();
      ContentDisposition cd =
          new ContentDisposition(Part.ATTACHMENT)
              .setParameter("filename", filename == null ? "unknown" : filename);
      resp.addHeader("Content-Disposition", cd.toString());
      sendbackOriginalDoc(up.getInputStream(), up.getContentType(), resp);

      boolean expunge =
          "true".equalsIgnoreCase(req.getParameter(PARAM_EXPUNGE))
              || "1".equals(req.getParameter(PARAM_EXPUNGE));
      if (expunge) FileUploadServlet.deleteUpload(up);
    } catch (ServiceException e) {
      returnError(resp, e);
    } catch (HttpException e) {
      throw new IOException("Unknown error", e);
    }
  }

  private boolean isTrue(String attr, String accountId) throws ServletException {
    Provisioning prov = Provisioning.getInstance();
    try {
      Account account = prov.get(AccountBy.id, accountId);
      return prov.getConfig().getBooleanAttr(attr, false) || account.getBooleanAttr(attr, false);
    } catch (ServiceException e) {
      throw new ServletException(e);
    }
  }

  public static MimePart getMimePart(CalendarItem calItem, String part)
      throws IOException, MessagingException, ServiceException {
    return Mime.getMimePart(calItem.getMimeMessage(), part);
  }

  public static MimePart getMimePart(Message msg, String part)
      throws IOException, MessagingException, ServiceException {
    return Mime.getMimePart(msg.getMimeMessage(), part);
  }

  public static void sendbackOriginalDoc(
      MimePart mp, String contentType, HttpServletRequest req, HttpServletResponse resp)
      throws IOException, MessagingException {
    String filename = Mime.getFilename(mp);
    if (filename == null) filename = "unknown";
    String cd = HttpUtil.createContentDisposition(req, Part.ATTACHMENT, filename);
    resp.addHeader("Content-Disposition", cd);
    String desc = mp.getDescription();
    if (desc != null) resp.addHeader("Content-Description", desc);
    sendbackOriginalDoc(mp.getInputStream(), contentType, resp);
  }

  public static void sendbackOriginalDoc(
      InputStream is, String contentType, HttpServletResponse resp) throws IOException {
    resp.setContentType(contentType);
    ByteUtil.copy(is, true, resp.getOutputStream(), false);
  }

  static void sendbackDefangedHtml(
      MimePart mp, String contentType, HttpServletResponse resp, String fmt)
      throws IOException, MessagingException {
    resp.setContentType(contentType);
    String html;
    try (InputStream is = mp.getInputStream()) {
      html = DefangFactory.getDefanger(contentType).defang(is, FORMAT_DEFANGED_HTML.equals(fmt));
    }
    try (ByteArrayInputStream bais =
        new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8))) {
      ByteUtil.copy(bais, false, resp.getOutputStream(), false);
    }
  }

  private void sendbackBlockMessage(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(mBlockPage);
    if (dispatcher != null) {
      dispatcher.forward(req, resp);
      return;
    }
    resp.sendError(
        HttpServletResponse.SC_FORBIDDEN,
        L10nUtil.getMessage(MsgKey.errAttachmentDownloadDisabled, req));
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    ZimbraLog.clearContext();
    addRemoteIpToLoggingContext(req);

    mLog.debug("request url: %s, path info: ", req.getRequestURL(), req.getPathInfo());

    AuthToken authToken = getAuthTokenFromCookie(req, resp);
    if (authToken == null) {
      mLog.info("Auth token not present.", HttpServletResponse.SC_UNAUTHORIZED);
      resp.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          L10nUtil.getMessage(MsgKey.errMustAuthenticate, req));
      return;
    }

    if (isTrue(ZAttrProvisioning.A_zimbraAttachmentsBlocked, authToken.getAccountId())) {
      sendbackBlockMessage(req, resp);
      return;
    }
    String pathInfo = req.getPathInfo();
    if (pathInfo != null) {
      if (pathInfo.equals(PREFIX_GET)) {
        getCommand(req, resp, authToken);
      } else if (pathInfo.equals(PREFIX_PROXY)) {
        retrieveUpload(req, resp, authToken);
      }
    } else {
      resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST, L10nUtil.getMessage(MsgKey.errInvalidRequest, req));
    }
  }

  @Override
  public void init() throws ServletException {
    String name = getServletName();
    mLog.info("Servlet " + name + " starting up");
    super.init();
    mBlockPage = getInitParameter(MSGPAGE_BLOCK);
  }

  @Override
  public void destroy() {
    String name = getServletName();
    mLog.info("Servlet " + name + " shutting down");
    super.destroy();
  }
}
