package com.zimbra.cs.account.auth;


import static org.junit.Assert.*;

import com.zimbra.cs.account.Account;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

public class ZimbraCustomAuthTest {

  /**
   * A Custom Auth class for testing purposes.
   */
  private class TestCustomAuth extends ZimbraCustomAuth {

    @Override
    public void authenticate(Account acct, String password, Map<String, Object> context,
        List<String> args) throws Exception {
        // Huh, I do nothing.
    }
  }

  @Test
  public void shouldVerifyCustomAuthHandlerIsRegistered() {
    String testHandler = "testHandler";
    ZimbraCustomAuth.register(testHandler, new TestCustomAuth());
    assertTrue(ZimbraCustomAuth.handlerIsRegistered(testHandler));
  }

  @Test
  public void shouldVerifyCustomAuthHandlerNotRegisered() {
    String testHandler = UUID.randomUUID().toString();
    assertFalse(ZimbraCustomAuth.handlerIsRegistered(testHandler));
  }
}
