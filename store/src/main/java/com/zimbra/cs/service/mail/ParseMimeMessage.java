// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.calendar.ZCalendar;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mailbox.ItemIdentifier;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.mime.ContentType;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.mime.MimeHeader;
import com.zimbra.common.mime.shim.JavaMailInternetAddress;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.common.util.CharsetUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.zmime.ZMimeBodyPart;
import com.zimbra.common.zmime.ZMimeMessage;
import com.zimbra.common.zmime.ZMimeMultipart;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.IDNUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.Fragment;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailSender.SafeSendFailedException;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.calendar.CalendarMailSender;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mime.MailboxBlobDataSource;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.service.UploadDataSource;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.service.formatter.VCard;
import com.zimbra.cs.service.mail.ToXML.EmailType;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.SharedByteArrayInputStream;

/**
 * @since Sep 29, 2004
 */
public final class ParseMimeMessage {

  // by default, no invite allowed
  static final InviteParser NO_INV_ALLOWED_PARSER = new InviteParser() {
    @Override
    public InviteParserResult parseInviteElement(ZimbraSoapContext zsc, OperationContext octxt,
        Account account, Element inviteElem)
        throws ServiceException {
      throw ServiceException.INVALID_REQUEST("No <inv> element allowed for this request", null);
    }
  };
  private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024L;
  private static final Map<String, String> FETCH_CONTACT_PARAMS = new HashMap<>(3);

  static {
    FETCH_CONTACT_PARAMS.put(UserServlet.QP_FMT, "vcf");
  }

  private ParseMimeMessage() {
    throw new IllegalStateException("Utility class");
  }

  public static MimeMessage importMsgSoap(Element msgElem) throws ServiceException {
    /* msgElem == "<m>" E_MSG */
    assert (msgElem.getName().equals(MailConstants.E_MSG));

    Element contentElement = msgElem.getElement(MailConstants.E_CONTENT);

    byte[] content;
    // Convert LF to CRLF because the XML parser normalizes element text to LF.
    String text = StringUtil.lfToCrlf(contentElement.getText());
    content = text.getBytes(StandardCharsets.UTF_8);
    long maxSize = Provisioning.getInstance().getConfig().getLongAttr(
        ZAttrProvisioning.A_zimbraMtaMaxMessageSize, DEFAULT_MAX_SIZE);
    if ((maxSize != 0 /* 0 means "no limit" */) && (content.length > maxSize)) {
      throw ServiceException.INVALID_REQUEST("inline message too large", null);
    }

    InputStream messageStream = new SharedByteArrayInputStream(content);
    try {
      return new Mime.FixedMimeMessage(JMSession.getSession(), messageStream);
    } catch (MessagingException me) {
      throw ServiceException.FAILURE("MessagingException", me);
    }
  }

  public static MimeMessage parseMimeMsgSoap(ZimbraSoapContext zsc, OperationContext octxt,
      Mailbox mbox, Element msgElem, MimeBodyPart[] additionalParts, MimeMessageData out)
      throws ServiceException {
    return parseMimeMsgSoap(zsc, octxt, mbox, msgElem, additionalParts, NO_INV_ALLOWED_PARSER,
        out, false);
  }

  public static MimeMessage parseMimeMsgSoap(ZimbraSoapContext zsc, OperationContext octxt,
      Mailbox mbox,
      Element msgElem, MimeBodyPart[] additionalParts, MimeMessageData out,
      boolean attachMessageFromCache)
      throws ServiceException {
    return parseMimeMsgSoap(zsc, octxt, mbox, msgElem, additionalParts, NO_INV_ALLOWED_PARSER, out,
        attachMessageFromCache);
  }

  public static String getTextPlainContent(Element elem) {
    return getFirstContentByType(elem, MimeConstants.CT_TEXT_PLAIN);
  }

  public static String getTextHtmlContent(Element elem) {
    return getFirstContentByType(elem, MimeConstants.CT_TEXT_HTML);
  }

  // Recursively find and return the content of the first part with the specified content type.
  static String getFirstContentByType(Element elem, String contentType) {
    if (elem == null) {
      return null;
    }

    if (MailConstants.E_MSG.equals(elem.getName())) {
      elem = elem.getOptionalElement(MailConstants.E_MIMEPART);
      if (elem == null) {
        return null;
      }
    }

    String type = elem.getAttribute(MailConstants.A_CONTENT_TYPE, contentType).trim().toLowerCase();
    if (type.equals(contentType)) {
      return elem.getAttribute(MailConstants.E_CONTENT, null);
    } else if (type.startsWith(MimeConstants.CT_MULTIPART_PREFIX)) {
      for (Element childElem : elem.listElements(MailConstants.E_MIMEPART)) {
        String text = getFirstContentByType(childElem, contentType);
        if (text != null) {
          return text;
        }
      }
    }
    return null;
  }

  public static MimeMessage parseMimeMsgSoap(ZimbraSoapContext zsc, OperationContext octxt,
      Mailbox mbox, Element msgElem, MimeBodyPart[] additionalParts, InviteParser inviteParser,
      MimeMessageData out) throws ServiceException {
    return parseMimeMsgSoap(zsc, octxt, mbox, msgElem, additionalParts, inviteParser, out, false);
  }

  /**
   * Given an {@code <m>} element from SOAP, return us a parsed {@link MimeMessage}, and also fill
   * in the {@link MimeMessageData} structure with information we parsed out of it (e.g. contained
   * Invite, msgids, etc etc)
   *
   * @param msgElem         the {@code <m>} element
   * @param additionalParts MimeBodyParts that we want to have added to the {@link MimeMessage} (ie
   *                        things the server is adding onto the message)
   * @param inviteParser    Callback which handles {@code <inv>} embedded invite components
   * @param out             Holds info about things we parsed out of the message that the caller
   *                        might want to know about
   */
  public static MimeMessage parseMimeMsgSoap(ZimbraSoapContext zsc, OperationContext octxt,
      Mailbox mbox,
      Element msgElem, MimeBodyPart[] additionalParts, InviteParser inviteParser,
      MimeMessageData out, boolean attachMessageFromCache)
      throws ServiceException {
    assert (msgElem.getName().equals(MailConstants.E_MSG)); // msgElem == "<m>" E_MSG

    Account target = DocumentHandler.getRequestedAccount(zsc);
    ParseMessageContext ctxt = new ParseMessageContext();
    ctxt.out = out;
    ctxt.zsc = zsc;
    ctxt.octxt = octxt;
    ctxt.mbox = mbox;
    ctxt.use2231 = target.isPrefUseRfc2231();
    ctxt.defaultCharset = target.getPrefMailDefaultCharset();
    if (Strings.isNullOrEmpty(ctxt.defaultCharset)) {
      ctxt.defaultCharset = MimeConstants.P_CHARSET_UTF8;
    }

    try {
      MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSmtpSession(target));
      MimeMultipart mmp = null;

      Element partElem = msgElem.getOptionalElement(MailConstants.E_MIMEPART);
      Element attachElem = msgElem.getOptionalElement(MailConstants.E_ATTACH);
      Element inviteElem = msgElem.getOptionalElement(MailConstants.E_INVITE);

      final boolean isSmimeSigned = msgElem.getParent()
          .getAttributeBool(SmimeConstants.A_SIGN, false);
      final boolean isEncrypted = msgElem.getParent()
          .getAttributeBool(SmimeConstants.A_ENCRYPT, false);

      // isQPencodeRequired is used to ensure RFC8551 Section 3.1.2/3.1.3.
      boolean isQPencodeRequired = isSmimeSigned && !isEncrypted;

      boolean hasContent = (partElem != null || inviteElem != null || additionalParts != null);
      boolean isMultipart = (attachElem
          != null); // || inviteElem != null || additionalParts!=null);
      if (isMultipart) {
        mmp = new ZMimeMultipart("mixed");  // may need to change to "digest" later
        mm.setContent(mmp);
      }

      // Grab the <inv> part now so we can stick it in a multipart/alternative if necessary
      MimeBodyPart[] alternatives;

      if (inviteElem != null) {
        int additionalLen = 0;
        if (additionalParts != null) {
          additionalLen += additionalParts.length;
        }
        alternatives = new MimeBodyPart[additionalLen + 1];
        int curAltPart = 0;

        // goes into the "content" subpart
        InviteParserResult result = inviteParser.parse(zsc, octxt, mbox.getAccount(), inviteElem);

        if (partElem != null && result.mCal != null) {
          // If textual content is provided and there's an invite,
          // set the text as DESCRIPTION of the iCalendar.  This helps
          // clients that ignore alternative text content and only
          // displays the DESCRIPTION specified in the iCalendar part.
          // (e.g. MS Entourage for Mac)
          String desc = getTextPlainContent(partElem);
          String html = getTextHtmlContent(partElem);
          result.mCal.addDescription(desc, html);

          // Only use the desc/html from MIME parts if at least one of them is non-empty.
          // It's possible the notes were given in <inv> node only, with no corresponding MIME parts.
          if (result.mInvite != null && ((desc != null && desc.length() > 0) || (html != null
              && html.length() > 0))) {
            result.mInvite.setDescription(desc, html);
            if (desc != null && desc.length() > 0) {
              result.mInvite.setFragment(Fragment.getFragment(desc, true));
            }
          }
        }
        MimeBodyPart mbp = CalendarMailSender.makeICalIntoMimePart(result.mCal);
        alternatives[curAltPart++] = mbp;

        if (additionalParts != null) {
          for (MimeBodyPart additionalPart : additionalParts) {
            alternatives[curAltPart++] = additionalPart;
          }
        }
        mm.addHeader("Content-class", "urn:content-classes:calendarmessage");
      } else {
        alternatives = additionalParts;
      }

      // handle the content from the client, if any
      if (hasContent) {
        final Element elem = partElem != null ? partElem : inviteElem;
        setContent(mm, mmp, elem, alternatives, ctxt,
            isQPencodeRequired);
      }
      // attachments go into the toplevel "mixed" part
      if (isMultipart) {
        handleAttachments(attachElem, mmp, ctxt, null, Part.ATTACHMENT, attachMessageFromCache);
      }

      // <m> attributes: id, f[lags], s[ize], d[ate], cid(conv-id), l(parent folder)
      // <m> child elements: <e> (email), <s> (subject), <f> (fragment), <mp>, <attach>
      MessageAddresses maddrs = new MessageAddresses();
      Set<String> headerNames = ImmutableSet.copyOf(
          Provisioning.getInstance().getConfig().getCustomMimeHeaderNameAllowed());
      for (Element elem : msgElem.listElements()) {
        String eName = elem.getName();
        switch (eName) {
          case MailConstants.E_ATTACH:
            // ignore it...
            break;
          case MailConstants.E_MIMEPART:  /* <mp> */
            // processMessagePart(mm, elem);
            break;
          case MailConstants.E_EMAIL:  /* <e> */
            maddrs.add(elem, ctxt.defaultCharset);
            break;
          case MailConstants.E_IN_REPLY_TO:  /* <irt> */
            // mm.setHeader("In-Reply-To", elem.getText());
            break;
          case MailConstants.E_SUBJECT:  /* <su> */
            // mm.setSubject(elem.getText(), "utf-8");
            break;
          case MailConstants.E_FRAG:  /* <f> */
            ZimbraLog.soap.debug("Ignoring message fragment data");
            break;
          case MailConstants.E_INVITE:  /* <inv> */
            // Already processed above.  Ignore it.
            break;
          case MailConstants.E_CAL_TZ:  /* <tz> */
            // Ignore as a special case.
            break;
          case MailConstants.E_HEADER:  // <h>
            String name = elem.getAttribute(MailConstants.A_NAME);
            if (headerNames.contains(name)) {
              mm.addHeader(name,
                  MimeHeader.escape(elem.getText(), StandardCharsets.UTF_8, true));
            } else {
              throw ServiceException.INVALID_REQUEST(
                  "header '" + name + "' not allowed", null);
            }
            break;
          default:
            ZimbraLog.soap.warn("unsupported child element '%s' under parent %s",
                elem.getName(), msgElem.getName());
            break;
        }
      }

      // deal with things that can be either <m> attributes or subelements
      String subject = msgElem.getAttribute(MailConstants.E_SUBJECT, "");
      mm.setSubject(subject, CharsetUtil.checkCharset(subject, ctxt.defaultCharset));

      String irt = cleanReference(msgElem.getAttribute(MailConstants.E_IN_REPLY_TO, null));
      if (irt != null) {
        mm.setHeader("In-Reply-To", irt);
      }

      // can have no addresses specified if it's a draft...
      if (!maddrs.isEmpty()) {
        addAddressHeaders(mm, maddrs);
      }

      if (!hasContent && !isMultipart) {
        mm.setText("", MimeConstants.P_CHARSET_DEFAULT);
      }

      String flagStr = msgElem.getAttribute(MailConstants.A_FLAGS, "");
      if (flagStr.indexOf(Flag.toChar(Flag.ID_HIGH_PRIORITY)) != -1) {
        mm.addHeader("X-Priority", "1");
        mm.addHeader("Importance", "high");
      } else if (flagStr.indexOf(Flag.toChar(Flag.ID_LOW_PRIORITY)) != -1) {
        mm.addHeader("X-Priority", "5");
        mm.addHeader("Importance", "low");
      }

      // JavaMail tip: don't forget to call this, it is REALLY confusing.
      mm.saveChanges();
      return mm;
    } catch (UnsupportedEncodingException e) {
      throw ServiceException.FAILURE("UnsupportedEncodingException", e);
    } catch (SendFailedException e) {
      SafeSendFailedException ssfe = new SafeSendFailedException(e);
      throw ServiceException.FAILURE("SendFailure", ssfe);
    } catch (MessagingException e) {
      throw ServiceException.FAILURE("MessagingException", e);
    } catch (IOException e) {
      throw ServiceException.FAILURE("IOExecption", e);
    }
  }

  private static void handleAttachments(Element attachElem, MimeMultipart mmp,
      ParseMessageContext ctxt, String contentID, String contentDisposition)
      throws ServiceException, MessagingException, IOException {
    handleAttachments(attachElem, mmp, ctxt, contentID, contentDisposition, false);
  }

  private static void handleAttachments(Element attachElem, MimeMultipart mmp,
      ParseMessageContext ctxt, String contentID, String contentDisposition,
      boolean attachFromMessageCache)
      throws ServiceException, MessagingException, IOException {
    if (contentID != null) {
      contentID = '<' + contentID + '>';
    }

    String attachIds = attachElem.getAttribute(MailConstants.A_ATTACHMENT_ID, null);
    if (attachIds != null) {
      for (String uploadId : attachIds.split(FileUploadServlet.UPLOAD_DELIMITER)) {
        Upload up = FileUploadServlet.fetchUpload(ctxt.zsc.getAuthtokenAccountId(), uploadId,
            ctxt.zsc.getAuthToken());
        if (up == null) {
          throw MailServiceException.NO_SUCH_UPLOAD(uploadId);
        }
        attachUpload(mmp, up, contentID, ctxt, null, null, contentDisposition);
        ctxt.out.addUpload(up);
      }
    }

    for (Element elem : attachElem.listElements()) {
      String attachType = elem.getName();
      boolean optional = elem.getAttributeBool(MailConstants.A_OPTIONAL, false);
      try {
        switch (attachType) {
          case MailConstants.E_MIMEPART: {
            String mid = elem.getAttribute(MailConstants.A_MESSAGE_ID);
            if (mid.indexOf(ItemIdentifier.ACCOUNT_DELIMITER) == -1) {
              mid = ctxt.zsc.getAuthToken().getAccount().getId()
                  + ItemIdentifier.ACCOUNT_DELIMITER + mid;
            }
            ItemId iid = new ItemId(mid, ctxt.zsc);
            String part = elem.getAttribute(MailConstants.A_PART);
            attachPart(mmp, iid, part, contentID, ctxt, contentDisposition);
            break;
          }
          case MailConstants.E_MSG: {
            ItemId iid = new ItemId(elem.getAttribute(MailConstants.A_ID), ctxt.zsc);
            attachMessage(mmp, iid, contentID, ctxt, attachFromMessageCache);
            break;
          }
          case MailConstants.E_CONTACT: {
            ItemId iid = new ItemId(elem.getAttribute(MailConstants.A_ID), ctxt.zsc);
            attachContact(mmp, iid, contentID, ctxt);
            break;
          }
          default:
            break;
        }
      } catch (NoSuchItemException nsie) {
        if (!optional) {
          throw nsie;
        }
        ZimbraLog.soap.info("skipping missing optional attachment: " + elem);
      }
    }
  }

  /**
   * The <mp>'s from the client and the MimeBodyParts in alternatives[] all want to be "content" of
   * this MimeMessage.  The alternatives[] all need to be "alternative" to whatever the client sends
   * us....but we want to be careful so that we do NOT create a nested multipart/alternative
   * structure within another one (that would be very tacky)....so this is a bit complicated.
   */
  private static void setContent(MimeMessage mm, MimeMultipart mmp, Element elem,
      MimeBodyPart[] alternatives,
      ParseMessageContext ctxt)
      throws MessagingException, ServiceException, IOException {
    setContent(mm, mmp, elem, alternatives, ctxt, false);
  }

  private static void setContent(MimeMessage mm, MimeMultipart mmp, Element elem,
      MimeBodyPart[] alternatives,
      ParseMessageContext ctxt, boolean isQPencodeRequired)
      throws MessagingException, ServiceException, IOException {
    String type = elem.getAttribute(MailConstants.A_CONTENT_TYPE, MimeConstants.CT_DEFAULT).trim();
    ContentType ctype = new ContentType(type, ctxt.use2231).cleanup();

    // is the client passing us a multipart?
    if (ctype.getPrimaryType().equals("multipart")) {
      // handle multipart content separately...
      setMultipartContent(ctype, mm, mmp, elem, alternatives, ctxt, isQPencodeRequired);
      return;
    }

    Element inline = elem.getOptionalElement(MailConstants.E_ATTACH);
    if (inline != null) {
      handleAttachments(inline, mmp, ctxt, elem.getAttribute(MailConstants.A_CONTENT_ID, null),
          Part.INLINE);
      return;
    }

    // a single part from the client...we might still have to create a multipart/alternative if
    // there are alternatives[] passed-in, but still this is fairly straightforward...

    if (alternatives != null) {
      // create a multipart/alternative to hold all the alternatives
      MimeMultipart mmpNew = new ZMimeMultipart("alternative");
      if (mmp == null) {
        mm.setContent(mmpNew);
      } else {
        MimeBodyPart mbpWrapper = new ZMimeBodyPart();
        mbpWrapper.setContent(mmpNew);
        mmp.addBodyPart(mbpWrapper);
      }
      mmp = mmpNew;
    }

    // once we get here, mmp is either NULL, a multipart/mixed from the toplevel,
    // or a multipart/alternative created just above....either way we are safe to stick
    // the client's nice and simple body right here
    String text = elem.getAttribute(MailConstants.E_CONTENT, "");
    boolean isAscii = StringUtil.isAsciiString(text);

    byte[] raw = text.getBytes(StandardCharsets.UTF_8);
    if (raw.length > 0 || !LC.mime_exclude_empty_content.booleanValue() || ctype.getPrimaryType()
        .equals("text")) {
      ctxt.incrementSize("message body", raw.length);

      // if the user has specified an alternative charset, make sure it exists and can encode the content
      String charset = CharsetUtil.checkCharset(text, ctxt.defaultCharset);
      ctype.setCharset(charset).setParameter(MimeConstants.P_CHARSET, charset);

      Object content = ctype.getContentType().equals(ContentType.MESSAGE_RFC822) ?
          new ZMimeMessage(JMSession.getSession(), new SharedByteArrayInputStream(raw)) : text;
      if (mmp != null) {
        if (isQPencodeRequired) {
          if (!isAscii) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final OutputStream encodedOut = MimeUtility.encode(baos,
                MimeConstants.ET_QUOTED_PRINTABLE);
            encodedOut.write(text.getBytes(charset));
            text = baos.toString();
            content = ctype.getContentType().equals(ContentType.MESSAGE_RFC822) ?
                new ZMimeMessage(JMSession.getSession(), new SharedByteArrayInputStream(raw))
                : text;
            String mbpHeaders = "Content-Type: " + ctype
                + "\r\nContent-Transfer-Encoding: " + MimeConstants.ET_QUOTED_PRINTABLE
                + "\r\n";
            mmp.addBodyPart(new MimeBodyPart(
                new InternetHeaders(new ByteArrayInputStream(mbpHeaders.getBytes())),
                content.toString().getBytes()));
          } else {
            final MimeBodyPart mbp = new ZMimeBodyPart();
            mbp.setContent(content, ctype.toString());
            mmp.addBodyPart(mbp);
          }
        } else {
          final MimeBodyPart mbp = new ZMimeBodyPart();
          mbp.setContent(content, ctype.toString());
          mmp.addBodyPart(mbp);
        }
      } else {
        if (isQPencodeRequired) {
          if (!isAscii) {
            mm.setContent(content, ctype.toString());
            mm.setHeader("Content-Transfer-Encoding", "quoted-printable");
          } else {
            mm.setContent(content, ctype.toString());
          }
        } else {
          mm.setContent(content, ctype.toString());
        }
      }
    }

    if (alternatives != null) {
      for (MimeBodyPart alternative : alternatives) {
        ctxt.incrementSize("alternative body", alternative.getSize());
        mmp.addBodyPart(alternative);
      }
    }
  }

  private static void setMultipartContent(ContentType contentType, MimeMessage mm,
      MimeMultipart mmp, Element elem, MimeBodyPart[] alternatives, ParseMessageContext ctxt,
      boolean isQPencodeRequired)
      throws MessagingException, ServiceException, IOException {
    // do we need to add a multipart/alternative for the alternatives?
    if (alternatives == null || contentType.getSubType().equals("alternative")) {
      // no need to add an extra multipart/alternative!

      // create the MimeMultipart and attach it to the existing structure:
      MimeMultipart mmpNew = new ZMimeMultipart(contentType);
      if (mmp == null) {
        // there were no multiparts at all, we need to create one
        mm.setContent(mmpNew);
      } else {
        // there was already a multipart/mixed at the top of the mm
        MimeBodyPart mbpWrapper = new ZMimeBodyPart();
        mbpWrapper.setContent(mmpNew);
        mmp.addBodyPart(mbpWrapper);
      }

      // add each part in turn (recursively) below
      final Optional<Element> plainTextPart = getFirstElementFromMimePartByType(elem,
          MimeConstants.CT_TEXT_PLAIN);

      // add plainText part as first element
      if (plainTextPart.isPresent()) {
        setContent(mm, mmpNew, plainTextPart.get(), null, ctxt, isQPencodeRequired);
      }

      // add all other mime parts but plainText and htmlTextPart
      for (Element subpart : elem.listElements()) {
        if (subpart == plainTextPart.orElse(null)) {
          continue;
        }
        setContent(mm, mmpNew, subpart, null, ctxt, isQPencodeRequired);
      }

      // finally, add the alternatives if there are any...
      if (alternatives != null) {
        for (MimeBodyPart alternative : alternatives) {
          ctxt.incrementSize("alternative", alternative.getSize());
          mmpNew.addBodyPart(alternative);
        }
      }
    } else {
      // create a multipart/alternative to hold all the client's struct + the alternatives
      MimeMultipart mmpNew = new ZMimeMultipart("alternative");
      if (mmp == null) {
        mm.setContent(mmpNew);
      } else {
        MimeBodyPart mbpWrapper = new ZMimeBodyPart();
        mbpWrapper.setContent(mmpNew);
        mmp.addBodyPart(mbpWrapper);
      }

      // add the entire client's multipart/whatever here inside our multipart/alternative
      setContent(mm, mmpNew, elem, null, ctxt, isQPencodeRequired);

      // add all the alternatives
      for (MimeBodyPart alternative : alternatives) {
        ctxt.incrementSize("alternative", alternative.getSize());
        mmpNew.addBodyPart(alternative);
      }
    }
  }

  private static void attachUpload(MimeMultipart mmp, Upload up, String contentID,
      ParseMessageContext ctxt, ContentType ctypeOverride, String contentDescription,
      String contentDisposition)
      throws ServiceException, MessagingException {
    // make sure we haven't exceeded the max size
    ctxt.incrementSize("upload " + up.getName(), (long) (up.getSize() * 1.33));

    // scan upload for viruses
    StringBuffer info = new StringBuffer();
    UploadScanner.Result result = UploadScanner.accept(up, info);
    if (result == UploadScanner.REJECT) {
      throw MailServiceException.UPLOAD_REJECTED(up.getName(), info.toString());
    } else if (result == UploadScanner.ERROR) {
      throw MailServiceException.SCAN_ERROR(up.getName());
    }
    String filename = up.getName();

    // create the part and override the DataSource's default ctype, if required
    MimeBodyPart mbp = new ForceBase64MimeBodyPart();

    UploadDataSource uds = new UploadDataSource(up);
    if (ctypeOverride != null && !ctypeOverride.equals("")) {
      uds.setContentType(ctypeOverride);
    }
    mbp.setDataHandler(new DataHandler(uds));

    // set headers -- ctypeOverride non-null has magical properties that I'm going to regret tomorrow
    ContentType ctype = ctypeOverride;
    ContentDisposition cdisp;
    if (Part.INLINE.equalsIgnoreCase(contentDisposition)) {
      cdisp = new ContentDisposition(Part.INLINE, ctxt.use2231);
    } else {
      cdisp = new ContentDisposition(Part.ATTACHMENT, ctxt.use2231);
    }
    if (ctype == null) {
      ctype = new ContentType(
          up.getContentType() == null ? MimeConstants.CT_APPLICATION_OCTET_STREAM
              : up.getContentType());
      ctype.cleanup().setParameter("name", filename);
      cdisp.setParameter("filename", filename);
    }
    mbp.setHeader("Content-Type", ctype.setCharset(ctxt.defaultCharset).toString());
    mbp.setHeader("Content-Disposition", cdisp.setCharset(ctxt.defaultCharset).toString());
    if (contentDescription != null) {
      mbp.setHeader("Content-Description", contentDescription);
    }
    if (ctype.getContentType().equals(MimeConstants.CT_APPLICATION_PDF)) {
      mbp.setHeader("Content-Transfer-Encoding", "base64");
    }
    mbp.setContentID(contentID);

    // add to the parent part
    mmp.addBodyPart(mbp);
  }

  private static void attachRemoteItem(MimeMultipart mmp, ItemId iid, String contentID,
      ParseMessageContext ctxt,
      Map<String, String> params, ContentType ctypeOverride)
      throws ServiceException, MessagingException {
    try {
      Upload up = UserServlet.getRemoteResourceAsUpload(ctxt.zsc.getAuthToken(), iid, params);
      ctxt.out.addFetch(up);
      attachUpload(mmp, up, contentID, ctxt, ctypeOverride, null, "");
    } catch (IOException ioe) {
      throw ServiceException.FAILURE("can't serialize remote item", ioe);
    }
  }

  @SuppressWarnings("unchecked")
  private static void attachMessage(MimeMultipart mmp, ItemId iid, String contentID,
      ParseMessageContext ctxt) throws MessagingException, ServiceException {
    attachMessage(mmp, iid, contentID, ctxt, false);
  }

  @SuppressWarnings("unchecked")
  private static void attachMessage(MimeMultipart mmp, ItemId iid, String contentID,
      ParseMessageContext ctxt, boolean attachMessageFromCache)
      throws MessagingException, ServiceException {
    if (!iid.isLocal()) {
      attachRemoteItem(mmp, iid, contentID, ctxt, Collections.emptyMap(),
          new ContentType(MimeConstants.CT_MESSAGE_RFC822));
      return;
    }

    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(iid.getAccountId());
    Message msg = mbox.getMessageById(ctxt.octxt, iid.getId());
    ctxt.incrementSize("attached message", msg.getSize());

    MimeBodyPart mbp = new ZMimeBodyPart();
    if (attachMessageFromCache && mbox.getAccount().isFeatureSMIMEEnabled()
        && (Mime.isEncrypted(msg.getMimeMessage(false).getContentType())
        || Mime.isPKCS7Signed(msg.getMimeMessage(false).getContentType()))) {
      MimeMessage cachedMimeMessage = msg.getMimeMessage(true);
      mbp.setContent(cachedMimeMessage, MimeConstants.CT_MESSAGE_RFC822);
    } else {
      mbp.setDataHandler(new DataHandler(new MailboxBlobDataSource(msg.getBlob())));
      mbp.setHeader("Content-Type", MimeConstants.CT_MESSAGE_RFC822);
      mbp.setHeader("Content-Disposition", Part.ATTACHMENT);
    }
    mbp.setContentID(contentID);
    mmp.addBodyPart(mbp);
  }

  private static void attachContact(MimeMultipart mmp, ItemId iid, String contentID,
      ParseMessageContext ctxt)
      throws MessagingException, ServiceException {
    if (!iid.isLocal()) {
      attachRemoteItem(mmp, iid, contentID, ctxt, FETCH_CONTACT_PARAMS, null);
      return;
    }

    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(iid.getAccountId());
    VCard vcf = VCard.formatContact(mbox.getContactById(ctxt.octxt, iid.getId()));

    ctxt.incrementSize("contact", vcf.getFormatted().length());
    String filename = vcf.fn + ".vcf";
    String charset = CharsetUtil.checkCharset(vcf.getFormatted(), ctxt.defaultCharset);

    MimeBodyPart mbp = new ZMimeBodyPart();
    mbp.setText(vcf.getFormatted(), charset);
    mbp.setHeader("Content-Type",
        new ContentType("text/x-vcard", ctxt.use2231).setCharset(ctxt.defaultCharset)
            .setParameter("name", filename).setParameter("charset", charset).toString());
    mbp.setHeader("Content-Disposition",
        new ContentDisposition(Part.ATTACHMENT, ctxt.use2231).setCharset(ctxt.defaultCharset)
            .setParameter("filename", filename).toString());
    mbp.setContentID(contentID);
    mmp.addBodyPart(mbp);
  }

  private static void attachPart(MimeMultipart mmp, ItemId iid, String part, String contentID,
      ParseMessageContext ctxt, String contentDisposition)
      throws IOException, MessagingException, ServiceException {
    if (!iid.isLocal()) {
      Map<String, String> params = new HashMap<>(3);
      params.put(UserServlet.QP_PART, part);
      attachRemoteItem(mmp, iid, contentID, ctxt, params, null);
      return;
    }

    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(iid.getAccountId());
    MimeMessage mm;
    if (iid.hasSubpart()) {
      mm = mbox.getCalendarItemById(ctxt.octxt, iid.getId()).getSubpartMessage(iid.getSubpartId());
    } else {
      mm = mbox.getMessageById(ctxt.octxt, iid.getId()).getMimeMessage();
    }
    MimePart mp = Mime.getMimePart(mm, part);
    if (mp == null) {
      throw MailServiceException.NO_SUCH_PART(part);
    }

    String filename = Mime.getFilename(mp);
    String ctypeHdr = mp.getContentType();
    String contentType = null;
    if (ctypeHdr != null) {
      contentType = new ContentType(ctypeHdr, ctxt.use2231).cleanup()
          .setCharset(ctxt.defaultCharset).setParameter("name", filename).toString();
    }

    // bug 70015: two concurrent SaveDrafts each reference the same attachment in the original draft
    //   -- avoid race condition by copying attached part to FileUploadServlet, so
    //      deleting original blob doesn't lead to stale BlobInputStream references
    Upload up = FileUploadServlet.saveUpload(mp.getInputStream(), filename, contentType,
        DocumentHandler.getRequestedAccount(ctxt.zsc).getId());
    ctxt.out.addFetch(up);
    String[] contentDesc = mp.getHeader("Content-Description");
    attachUpload(mmp, up, contentID, ctxt, null,
        (contentDesc == null || contentDesc.length == 0) ? null : contentDesc[0],
        contentDisposition);
  }

  private static void addAddressHeaders(MimeMessage mm, MessageAddresses maddrs)
      throws MessagingException {
    InternetAddress[] addrs = maddrs.get(EmailType.TO.toString());
    if (addrs != null && addrs.length > 0) {
      mm.addRecipients(javax.mail.Message.RecipientType.TO, addrs);
    }

    addrs = maddrs.get(EmailType.CC.toString());
    if (addrs != null && addrs.length > 0) {
      mm.addRecipients(javax.mail.Message.RecipientType.CC, addrs);
    }

    addrs = maddrs.get(EmailType.BCC.toString());
    if (addrs != null && addrs.length > 0) {
      mm.addRecipients(javax.mail.Message.RecipientType.BCC, addrs);
    }

    addrs = maddrs.get(EmailType.FROM.toString());
    if (addrs != null && addrs.length == 1) {
      mm.setFrom(addrs[0]);
    }

    addrs = maddrs.get(EmailType.SENDER.toString());
    if (addrs != null && addrs.length == 1) {
      mm.setSender(addrs[0]);
    }

    addrs = maddrs.get(EmailType.REPLY_TO.toString());
    if (addrs != null && addrs.length > 0) {
      mm.setReplyTo(addrs);
    }

    addrs = maddrs.get(EmailType.READ_RECEIPT.toString());
    if (addrs != null && addrs.length > 0) {
      mm.addHeader("Disposition-Notification-To", InternetAddress.toString(addrs));
    }
  }

  /**
   * Strips leading and trailing whitespace from the given message-id and adds the surrounding angle
   * brackets if absent.  If the message-id was {@code null} or just whitespace, returns {@code
   * null}.
   */
  private static String cleanReference(String refStr) {
    if (refStr == null) {
      return null;
    }
    String reference = refStr.trim();
    if (reference.isEmpty()) {
      return null;
    }

    if (!reference.startsWith("<")) {
      reference = "<" + reference;
    }
    if (!reference.endsWith(">")) {
      reference = reference + ">";
    }
    return reference;
  }

  /**
   * Returns first mime part element by matching the passed content type
   *
   * @param parentElement parent element where search will take place expect at least one {@link
   *                      MailConstants#E_MIMEPART} element
   * @param contentType   content type(ct) attribute used to search element
   * @return matching first element by given content type
   * @author Keshav Bhatt
   * @since 23.4.0
   */
  static Optional<Element> getFirstElementFromMimePartByType(Element parentElement,
      String contentType) {
    if (MailConstants.E_MSG.equals(parentElement.getName())) {
      parentElement = parentElement.getOptionalElement(MailConstants.E_MIMEPART);
      if (parentElement == null) {
        return Optional.empty();
      }
    }

    String parentElementContentType = parentElement.getAttribute(MailConstants.A_CONTENT_TYPE, "")
        .trim().toLowerCase();

    if (parentElementContentType.equals(contentType)) {
      return Optional.of(parentElement);
    } else if (parentElementContentType.startsWith(MimeConstants.CT_MULTIPART_PREFIX)) {
      return parentElement.listElements(MailConstants.E_MIMEPART)
          .stream()
          .map(childElem -> getFirstElementFromMimePartByType(childElem, contentType))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .filter(
              element -> element.getAttribute(MailConstants.A_CONTENT_TYPE, "").equals(contentType))
          .findFirst();
    } else {
      return Optional.empty();
    }
  }

  /**
   * Overrides the default transfer encoding and sets the encoding of all non-message attachments to
   * base64, so that we preserve line endings of text attachments (bugs 45858 and 53405).
   */
  private static class ForceBase64MimeBodyPart extends ZMimeBodyPart {

    public ForceBase64MimeBodyPart() {
    }

    @Override
    protected void updateHeaders() throws MessagingException {
      super.updateHeaders();
      if (LC.text_attachments_base64.booleanValue()) {
        String ct = Mime.getContentType(this);
        if (!(ct.startsWith(MimeConstants.CT_MESSAGE_PREFIX) || ct.startsWith(
            MimeConstants.CT_MULTIPART_PREFIX))) {
          setHeader("Content-Transfer-Encoding", "base64");
        }
      }
    }
  }

  /**
   * Callback routine for parsing the <inv> element and building a iCal4j Calendar from it
   * <p>
   * We use a callback b/c there are differences in the parsing depending on the operation: Replying
   * to an invite is different than Creating or Modifying one, etc etc...
   */
  abstract static class InviteParser {

    private InviteParserResult mResult;

    protected abstract InviteParserResult parseInviteElement(ZimbraSoapContext zsc,
        OperationContext octxt, Account account, Element invElement) throws ServiceException;

    public final InviteParserResult parse(ZimbraSoapContext zsc, OperationContext octxt,
        Account account, Element invElement) throws ServiceException {
      mResult = parseInviteElement(zsc, octxt, account, invElement);
      return mResult;
    }

    public InviteParserResult getResult() {
      return mResult;
    }
  }

  static class InviteParserResult {

    public ZCalendar.ZVCalendar mCal;
    public String mUid;
    public String mSummary;
    public Invite mInvite;
  }

  /**
   * Wrapper class for data parsed out of the mime message
   */
  public static class MimeMessageData {

    public List<Upload> fetches; // NULL unless we fetched messages from another server
    public List<Upload> uploads; // NULL unless there are uploaded attachments
    public String iCalUUID; // NULL unless there is an iCal part

    void addUpload(Upload up) {
      if (uploads == null) {
        uploads = new ArrayList<>(4);
      }
      uploads.add(up);
    }

    void addFetch(Upload up) {
      if (fetches == null) {
        fetches = new ArrayList<>(4);
      }
      fetches.add(up);
    }
  }

  /**
   * Class encapsulating common data passed among methods.
   */
  private static class ParseMessageContext {

    MimeMessageData out;
    ZimbraSoapContext zsc;
    OperationContext octxt;
    Mailbox mbox;
    boolean use2231;
    String defaultCharset;
    long size;
    long maxSize;

    ParseMessageContext() {
      try {
        Config config = Provisioning.getInstance().getConfig();
        maxSize = config.getLongAttr(Provisioning.A_zimbraMtaMaxMessageSize, -1);
      } catch (ServiceException e) {
        ZimbraLog.soap.warn("Unable to determine max message size.  Disabling limit check.", e);
      }
      if (maxSize < 0) {
        maxSize = Long.MAX_VALUE;
      }
    }

    void incrementSize(String name, long numBytes) throws MailServiceException {
      size += numBytes;
      ZimbraLog.soap.debug("Adding %s, incrementing size by %d to %d.", name, numBytes, size);
      if ((maxSize != 0 /* 0 means "no limit" */) && (size > maxSize)) {
        throw MailServiceException.MESSAGE_TOO_BIG(maxSize, size);
      }
    }
  }

  static final class MessageAddresses {

    private final HashMap<String, Object> addrs = new HashMap<>();

    public void add(Element elem, String defaultCharset)
        throws ServiceException, UnsupportedEncodingException {
      String emailAddress = IDNUtil.toAscii(elem.getAttribute(MailConstants.A_ADDRESS));
      String personalName = elem.getAttribute(MailConstants.A_PERSONAL, null);
      String addressType = elem.getAttribute(MailConstants.A_ADDRESS_TYPE);

      InternetAddress addr = new JavaMailInternetAddress(emailAddress, personalName,
          CharsetUtil.checkCharset(personalName, defaultCharset));

      Object content = addrs.get(addressType);
      if (content == null || addressType.equals(EmailType.FROM.toString()) ||
          addressType.equals(EmailType.SENDER.toString())) {
        addrs.put(addressType, addr);
      } else if (content instanceof List) {
        @SuppressWarnings("unchecked")
        List<InternetAddress> list = (List<InternetAddress>) content;
        list.add(addr);
      } else {
        List<InternetAddress> list = new ArrayList<>();
        list.add((InternetAddress) content);
        list.add(addr);
        addrs.put(addressType, list);
      }
    }

    @SuppressWarnings("unchecked")
    public InternetAddress[] get(String addressType) {
      Object content = addrs.get(addressType);
      if (content == null) {
        return null;
      } else if (content instanceof InternetAddress) {
        return new InternetAddress[]{(InternetAddress) content};
      } else {
        return ((List<InternetAddress>) content).toArray(new InternetAddress[0]);
      }
    }

    public boolean isEmpty() {
      return addrs.isEmpty();
    }
  }
}
