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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutoProvisioningParamsTest {

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void testAutoProvisioningParams() {
    String accountIdentifier = "one@test.com";
    AccountBy accountBy = AccountBy.name;

    boolean isAdmin = false;
    long timestamp = 1627545650L;
    long expires = 1627645650L;
    String preAuth = "abc123";
    HttpServletRequest request = mock(HttpServletRequest.class);
    Map<String, Object> authContext = Utils.createAuthContext(accountIdentifier, request);

    // Build the AutoProvisioningParams using the builder
    AutoProvisioningParams params =
        new AutoProvisioningParams.AutoProvisioningParamsBuilder(
                accountIdentifier,
                accountBy,
                Provisioning.getInstance(),
                isAdmin,
                timestamp,
                expires,
                preAuth)
            .authContext(authContext)
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
  void testAutoProvisioningParamsBuilder() {
    String accountIdentifier = "one@test.com";
    AccountBy accountBy = AccountBy.id;
    long timestamp = 1627545650L;
    long expires = 1627645650L;
    String preAuth = "xyz789";

    // Build the AutoProvisioningParams using the builder without setting authContext
    AutoProvisioningParams params =
        new AutoProvisioningParams.AutoProvisioningParamsBuilder(
                accountIdentifier,
                accountBy,
                Provisioning.getInstance(),
                true,
                timestamp,
                expires,
                preAuth)
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
