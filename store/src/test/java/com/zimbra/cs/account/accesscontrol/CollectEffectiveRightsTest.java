package com.zimbra.cs.account.accesscontrol;

import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_adminLoginAs;
import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_adminLoginCalendarResourceAs;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollectEffectiveRightsTest {

  @BeforeEach
  void initializeRights() throws Exception {
    RightManager.getInstance();
  }

  @Test
  void shouldExcludeUnassignedLoginAsRightsForGlobalAdmin() throws Exception {
    Account globalAdmin = account();
    RightBearer rightBearer = RightBearer.newRightBearer(globalAdmin);
    RightCommand.EffectiveRights result =
        new RightCommand.EffectiveRights(
            "account",
            globalAdmin.getId(),
            globalAdmin.getName(),
            globalAdmin.getId(),
            globalAdmin.getName());

    CollectEffectiveRights.getEffectiveRights(
        rightBearer, globalAdmin, TargetType.account, false, false, result);

    assertFalse(result.presetRights().contains(R_adminLoginAs.getName()));
    assertFalse(result.presetRights().contains(R_adminLoginCalendarResourceAs.getName()));
    assertTrue(result.canSetAllAttrs());
    assertTrue(result.canGetAllAttrs());
  }

  private Account account() {
    return new Account(
        "admin@example.com",
        "admin-id",
        Map.of(Provisioning.A_zimbraIsAdminAccount, "TRUE"),
        Map.of(),
        null);
  }
}
