package com.zextras.mailbox.domain.usecase;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxManager;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class performs deletion of a user
 *
 * @author Davide Polonio
 * @since 23.10.0
 */
public class DeleteUserUseCase {

  private final Provisioning provisioning;
  private final MailboxManager mailboxManager;
  private final Log log;

  @Inject
  public DeleteUserUseCase(
      @Named("defaultProvisioning") Provisioning provisioning,
      @Named("defaultMailboxManager") MailboxManager mailboxManager,
      @Named("zimbraLogSecurity") Log log) {
    this.provisioning = provisioning;
    this.mailboxManager = mailboxManager;
    this.log = log;
  }

  /**
   * Perform the deletion of a user given its id.
   *
   * @param userId the {@link String} that represents an id of a user
   * @return a {@link Try} of kind {@link Void}, stating if the output was successful or not
   */
  public Try<Void> delete(String userId) {
    return Try.of(
            () ->
                Optional.ofNullable(provisioning.getAccountById(userId))
                    .orElseThrow(() -> new RuntimeException("User " + userId + " doesn't exist")))
        .mapTry(
            account -> {
              provisioning.modifyAccountStatus(
                  account, ZAttrProvisioning.AccountStatus.maintenance.name());

              if (provisioning.onLocalServer(account)) {
                mailboxManager.getMailboxByAccount(account, false).deleteMailbox();
              }

              provisioning.deleteAccount(userId);

              log.info(
                  ZimbraLog.encodeAttrs(
                      new String[] {
                        "cmd", "DeleteAccount", "name", account.getName(), "id", account.getId()
                      }));

              return null;
            });
  }
}
