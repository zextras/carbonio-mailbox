package com.zimbra.cs.account.accesscontrol;

import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_adminLoginAs;
import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_modifyAccount;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HardRulesTest {

  @BeforeEach
  void initializeRights() throws Exception {
    RightManager.getInstance();
  }

  @Test
  void shouldNotGrantLoginAsImplicitlyToGlobalAdmin() throws Exception {
    Account globalAdmin = account(true);

    assertNull(HardRules.checkHardRules(globalAdmin, true, globalAdmin, R_adminLoginAs));
  }

  @Test
  void shouldGrantOtherAdminRightsImplicitlyToGlobalAdmin() throws Exception {
    Account globalAdmin = account(true);

    assertTrue(HardRules.checkHardRules(globalAdmin, true, globalAdmin, R_modifyAccount));
  }

  private Account account(boolean globalAdmin) {
    return new Account(
        "admin@example.com",
        "admin-id",
        Map.of(Provisioning.A_zimbraIsAdminAccount, globalAdmin ? "TRUE" : "FALSE"),
        Map.of(),
        null);
  }
}
