package com.zimbra.cs.account.accesscontrol;

import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_adminLoginAs;
import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_modifyAccount;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RightBearerTest {

  @Test
  void shouldRejectGlobalAdminForOtherAdminRight() {
    Account globalAdmin = account(true);

    assertFalse(
        RightBearer.isValidGranteeForRight(
            GranteeType.GT_USER, globalAdmin, R_modifyAccount));
  }

  @Test
  void shouldRejectRegularUserForLoginAsRight() {
    Account regularUser = account(false);

    assertFalse(
        RightBearer.isValidGranteeForRight(
            GranteeType.GT_USER, regularUser, R_adminLoginAs));
  }

  private Account account(boolean globalAdmin) {
    return new Account(
        "user@example.com",
        "user-id",
        Map.of(Provisioning.A_zimbraIsAdminAccount, globalAdmin ? "TRUE" : "FALSE"),
        Map.of(),
        null);
  }
}
