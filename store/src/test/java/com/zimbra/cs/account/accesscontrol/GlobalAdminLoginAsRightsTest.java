package com.zimbra.cs.account.accesscontrol;

import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_adminLoginAs;
import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_adminLoginCalendarResourceAs;
import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_modifyAccount;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GlobalAdminLoginAsRightsTest {

  @BeforeEach
  void initializeRights() throws Exception {
    RightManager.getInstance();
  }

  @Test
  void shouldIdentifyBothLoginAsAdminRights() {
    assertTrue(RightBearer.isLoginAsRight(R_adminLoginAs));
    assertTrue(RightBearer.isLoginAsRight(R_adminLoginCalendarResourceAs));
    assertFalse(RightBearer.isLoginAsRight(R_modifyAccount));
  }

  @Test
  void shouldAllowGlobalAdminAsLoginAsGrantee() {
    Account globalAdmin = accountWithAttribute(Provisioning.A_zimbraIsAdminAccount, true);

    assertTrue(
        RightBearer.isValidGranteeForRight(
            GranteeType.GT_USER, globalAdmin, R_adminLoginAs));
  }

  @Test
  void shouldNotAllowGlobalAdminAsGranteeForOtherAdminRights() {
    Account globalAdmin = accountWithAttribute(Provisioning.A_zimbraIsAdminAccount, true);

    assertFalse(
        RightBearer.isValidGranteeForRight(
            GranteeType.GT_USER, globalAdmin, R_modifyAccount));
  }

  @Test
  void shouldNotApplyGlobalAdminHardRuleToLoginAs() throws Exception {
    Account globalAdmin = accountWithAttribute(Provisioning.A_zimbraIsAdminAccount, true);

    assertNull(HardRules.checkHardRules(globalAdmin, true, globalAdmin, R_adminLoginAs));
  }

  @Test
  void shouldKeepGlobalAdminHardRuleForOtherRights() throws Exception {
    Account globalAdmin = accountWithAttribute(Provisioning.A_zimbraIsAdminAccount, true);

    assertTrue(HardRules.checkHardRules(globalAdmin, true, globalAdmin, R_modifyAccount));
  }

  @Test
  void shouldAllowGlobalAdminToDelegateLoginAs() throws Exception {
    Account globalAdmin = accountWithAttribute(Provisioning.A_zimbraIsAdminAccount, true);
    ACLAccessManager accessManager = new ACLAccessManager();

    assertTrue(
        accessManager.canPerform(
            globalAdmin,
            globalAdmin,
            R_adminLoginAs,
            true,
            new HashMap<>(),
            true,
            null));
  }

  @Test
  void shouldExcludeUnassignedLoginAsRightsFromGlobalAdminEffectiveRights() throws Exception {
    Account globalAdmin = accountWithAttribute(Provisioning.A_zimbraIsAdminAccount, true);
    RightBearer rightBearer = RightBearer.newRightBearer(globalAdmin);
    RightCommand.EffectiveRights result =
        new RightCommand.EffectiveRights("account", globalAdmin.getId(), globalAdmin.getName(),
            globalAdmin.getId(), globalAdmin.getName());

    CollectEffectiveRights.getEffectiveRights(
        rightBearer, globalAdmin, TargetType.account, false, false, result);

    assertFalse(result.presetRights().contains(R_adminLoginAs.getName()));
    assertFalse(result.presetRights().contains(R_adminLoginCalendarResourceAs.getName()));
    assertTrue(result.canSetAllAttrs());
    assertTrue(result.canGetAllAttrs());
  }

  private Account accountWithAttribute(String attribute, boolean value) {
    return new Account(
        "admin@example.com",
        "admin-id",
        Map.of(attribute, value ? "TRUE" : "FALSE"),
        Map.of(),
        null);
  }
}
