package com.zextras.mailbox.domain.usecase;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Provisioning;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;

public class DeleteUserUseCase {

  private final Provisioning provisioning;

  @Inject
  public DeleteUserUseCase(@Named("defaultProvisioning") Provisioning provisioning) {
    this.provisioning = provisioning;
  }

  public Try<Void> delete(String userId) {
    return Try.of(
            () ->
                Optional.ofNullable(provisioning.getAccountById(userId))
                    .orElseThrow(() -> new RuntimeException("User " + userId + " doesn't exist")))
        .mapTry(
            account -> {
              provisioning.modifyAccountStatus(
                  account, ZAttrProvisioning.AccountStatus.maintenance.name());
              return null;
            });
  }
}
