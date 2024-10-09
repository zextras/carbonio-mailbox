package com.zimbra.cs.service;

import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.mail.internet.MimePart;

/**
 * Interface for attachment content provider (get).
 *
 * @author davidefrison
 * @since 4.0.7
 */
public interface AttachmentService {

  /**
   * Get attachment for a mail message given account and authorization info.
   *
   * @param accountId id of the account that is requesting the attachment
   * @param token     authorization token for the account
   * @param messageId id of the email/message where attachment lies
   * @param part      part number that identifies attachment in the mail
   * @return try of {@link javax.mail.internet.MimePart} representing the attachment
   */
  Try<MimePart> getAttachment(String accountId, AuthToken token, int messageId, String part);

  /**
   * Get attachment for a mail message/Appointment given account and authorization info and ItemId information.
   *
   * @param accountId id of the account that is requesting the attachment
   * @param token     authorization token for the account
   * @param itemId    {@link ItemId} object for attachment
   * @param part      part number that identifies attachment in the mail
   * @return try of {@link javax.mail.internet.MimePart} representing the attachment
   */
  Try<MimePart> getAttachmentByItemId(String accountId, AuthToken token, ItemId itemId, String part);

}
