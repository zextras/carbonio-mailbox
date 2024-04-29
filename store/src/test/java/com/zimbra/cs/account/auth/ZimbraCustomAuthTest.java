package com.zimbra.cs.account.auth;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.zimbra.cs.account.Account;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.annotations.VisibleForTesting;

public class ZimbraCustomAuthTest {

  /**
   * A Custom Auth class for testing purposes.
   */
  @VisibleForTesting
  public static class TestCustomAuth extends ZimbraCustomAuth {

    @Override
    public void authenticate(Account acct, String password, Map<String, Object> context,
        List<String> args) throws Exception {
        // Huh, I do nothing.
    }
  }

 @Test
 void shouldVerifyCustomAuthHandlerIsRegistered() {
  String testHandler = "testHandler";
  ZimbraCustomAuth.register(testHandler, new TestCustomAuth());
  assertTrue(ZimbraCustomAuth.handlerIsRegistered(testHandler));
 }

 @Test
 void shouldVerifyCustomAuthHandlerNotRegisered() {
  String testHandler = UUID.randomUUID().toString();
  assertFalse(ZimbraCustomAuth.handlerIsRegistered(testHandler));
 }
}
