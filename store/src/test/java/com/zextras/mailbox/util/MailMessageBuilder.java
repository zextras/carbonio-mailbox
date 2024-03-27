package com.zextras.mailbox.util;

import com.zimbra.cs.mime.ParsedMessage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class MailMessageBuilder {
  private final List<Address> recipients = new ArrayList<>();
  private String from = "random@account.value";
  private String subject = "Subject";
  private MimeBodyPart body = createMimeBodyPart("Body Text");
  private final List<MimeBodyPart> attachments = new ArrayList<>();

  public MailMessageBuilder() throws MessagingException {
  }

  public MailMessageBuilder from(String value) {
    from = value;
    return this;
  }

  public MailMessageBuilder addRecipient(String value) throws AddressException {
    recipients.add(new InternetAddress(value));
    return this;
  }

  public MailMessageBuilder addAttachmentFromResources(String filePath) throws MessagingException, IOException {
    String absolutePath = getClass().getResource(filePath).getFile();
    return addAttachment(new File(absolutePath));
  }

  public MailMessageBuilder addAttachment(File value) throws MessagingException, IOException {
    attachments.add(createAttachment(value));
    return this;
  }

  public MailMessageBuilder subject(String value) {
    subject = value;
    return this;
  }

  public MailMessageBuilder body(MimeBodyPart value) {
    body = value;
    return this;
  }

  public MailMessageBuilder body(String value) throws MessagingException {
    body = createMimeBodyPart(value);
    return this;
  }
  public MailMessageBuilder addAttachment(String content, String fileName, String contentType) throws Exception {
    attachments.add(createAttachment(content, fileName, contentType));
    return this;
  }

  public ParsedMessage build() throws Exception {
    final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    mimeMessage.setFrom(new InternetAddress(from));
    mimeMessage.setSender(new InternetAddress(from));
    mimeMessage.setRecipients(RecipientType.TO, recipients.toArray(new Address[]{}));
    mimeMessage.setSubject(subject);
    mimeMessage.setContent(buildMultipartBody());
    return new ParsedMessage(mimeMessage, false);
  }

  private Multipart buildMultipartBody() throws MessagingException {
    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(body);

    for (var attachment : attachments) {
      multipart.addBodyPart(attachment);
    }

    return multipart;
  }

  private static MimeBodyPart createMimeBodyPart(String value) throws MessagingException {
    MimeBodyPart part = new MimeBodyPart();
    part.setText(value);
    return part;
  }

  private static MimeBodyPart createAttachment(File value) throws MessagingException, IOException {
    MimeBodyPart part = new MimeBodyPart();
    part.attachFile(value);
    return part;
  }

  private MimeBodyPart createAttachment(String content, String fileName, String contentType)
      throws IOException, MessagingException {
    MimeBodyPart part = new MimeBodyPart();
    DataSource ds = new ByteArrayDataSource(content, contentType);
    part.setFileName(fileName);
    part.setDataHandler(new DataHandler(ds));
    return part;
  }
}
