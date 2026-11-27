package com.zimbra.cs.account.accesscontrol;

import static com.zimbra.cs.account.accesscontrol.Rights.Admin.R_adminLoginAs;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ACLAccessManagerTest {

  @Test
  void shouldAllowGlobalAdminToDelegateLoginAsRight() throws Exception {
    Account globalAdmin = account();
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

  private Account account() {
    return new Account(
        "admin@example.com",
        "admin-id",
        Map.of(Provisioning.A_zimbraIsAdminAccount, "TRUE"),
        Map.of(),
        null);
  }
}
