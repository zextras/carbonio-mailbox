package com.zimbra.cs.service.servlet.preauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AutoProvisioningParamsTest {

  @BeforeAll
  static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void autoProvisioningParams_should_setPropertiesCorrectly_when_usingBuilder() {
    final String accountIdentifier = "one@test.com";
    final AccountBy accountBy = AccountBy.name;

    final boolean isAdmin = false;
    final long timestamp = 1627545650L;
    final long expires = 1627645650L;
    final String preAuth = "abc123";
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final Map<String, Object> authContext = Utils.createAuthContext(accountIdentifier, request);

    // Build the AutoProvisioningParams using the builder
    final AutoProvisioningParams params =
        new AutoProvisioningParams.AutoProvisioningParamsBuilder()
            .withAccountIdentifier(accountIdentifier)
            .withAccountBy(accountBy)
            .withProvisioning(Provisioning.getInstance())
            .withIsAdmin(isAdmin)
            .withTimestamp(timestamp)
            .withExpires(expires)
            .withPreAuth(preAuth)
            .withAuthContext(authContext)
            .build();

    // Verify the properties of AutoProvisioningParams
    assertEquals(accountIdentifier, params.getAccountIdentifier());
    assertEquals(accountBy, params.getAccountBy());
    assertEquals(Provisioning.getInstance(), params.getProvisioning());
    assertFalse(params.isAdmin());
    assertEquals(timestamp, params.getTimestamp());
    assertEquals(expires, params.getExpires());
    assertEquals(preAuth, params.getPreAuth());
    assertEquals(authContext, params.getAuthContext());
  }

  @Test
  void autoProvisioningParamsBuilder_should_setDefaultAdminValue_when_notProvided() {
    final String accountIdentifier = "one@test.com";
    final AccountBy accountBy = AccountBy.id;
    final long timestamp = 1627545650L;
    final long expires = 1627645650L;
    final String preAuth = "xyz789";

    // Build the AutoProvisioningParams using the builder without setting authContext
    final AutoProvisioningParams params =
        new AutoProvisioningParams.AutoProvisioningParamsBuilder()
            .withAccountIdentifier(accountIdentifier)
            .withAccountBy(accountBy)
            .withProvisioning(Provisioning.getInstance())
            .withIsAdmin(true)
            .withTimestamp(timestamp)
            .withExpires(expires)
            .withPreAuth(preAuth)
            .build();

    // Verify the properties of AutoProvisioningParams
    assertEquals(accountIdentifier, params.getAccountIdentifier());
    assertEquals(accountBy, params.getAccountBy());
    assertEquals(Provisioning.getInstance(), params.getProvisioning());
    assertTrue(params.isAdmin());
    assertEquals(timestamp, params.getTimestamp());
    assertEquals(expires, params.getExpires());
    assertEquals(preAuth, params.getPreAuth());
    assertNull(params.getAuthContext());
  }
}
