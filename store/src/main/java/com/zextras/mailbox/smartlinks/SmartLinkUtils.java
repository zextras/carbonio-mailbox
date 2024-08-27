package com.zextras.mailbox.smartlinks;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.MPartInfo;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.service.mail.message.parser.ParseMimeMessage;
import java.io.IOException;
import javax.mail.MessagingException;

public class SmartLinkUtils {

  private SmartLinkUtils() {
    throw new IllegalStateException("Utility class");
  }

  private static final long APPROXIMATE_SIZE_OF_ONE_SMART_LINK = 250L;

  public static long getSmartLinkAwareMimeMessageSize(Message message) throws ServiceException {
    long size = 0L;
    int totalSmartLinks = 0;
    try {
      var mimeMessage = message.getMimeMessage();
      if (mimeMessage == null) {
        return size;
      }

      var parts = Mime.getParts(mimeMessage);
      for (MPartInfo partInfo : parts) {
        if (partInfo.isMultipart()) {
          continue;
        }
        var part = partInfo.getMimePart();
        var smartLinkHeader = part.getHeader(ParseMimeMessage.SMART_LINK_HEADER, null);
        var requiresSmartLinkConversion = Boolean.parseBoolean(smartLinkHeader);
        if (requiresSmartLinkConversion) {
          totalSmartLinks += 1;
        } else {
          size += part.getSize();
        }
      }
    } catch (MessagingException | IOException e) {
      throw ServiceException.FAILURE("Failed to parse MimeMessage", e);
    }
    return size + (totalSmartLinks * APPROXIMATE_SIZE_OF_ONE_SMART_LINK);
  }
}
