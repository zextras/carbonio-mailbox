package com.zimbra.cs.service.servlet.preauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PreAuthParamsTest {

  @Test
  void testGetParamName() {
    assertEquals("preauth", PreAuthParams.PARAM_PRE_AUTH.getParamName());
    assertEquals("authtoken", PreAuthParams.PARAM_AUTHTOKEN.getParamName());
    assertEquals("account", PreAuthParams.PARAM_ACCOUNT.getParamName());
    assertEquals("admin", PreAuthParams.PARAM_ADMIN.getParamName());
    assertEquals("isredirect", PreAuthParams.PARAM_IS_REDIRECT.getParamName());
    assertEquals("by", PreAuthParams.PARAM_BY.getParamName());
    assertEquals("redirectURL", PreAuthParams.PARAM_REDIRECT_URL.getParamName());
    assertEquals("timestamp", PreAuthParams.PARAM_TIMESTAMP.getParamName());
    assertEquals("expires", PreAuthParams.PARAM_EXPIRES.getParamName());
  }

  @Test
  void testGetPreAuthParams() {
    final Set<String> preAuthParams = PreAuthParams.getPreAuthParams();
    assertTrue(preAuthParams.contains("preauth"));
    assertTrue(preAuthParams.contains("authtoken"));
    assertTrue(preAuthParams.contains("account"));
    assertTrue(preAuthParams.contains("admin"));
    assertTrue(preAuthParams.contains("isredirect"));
    assertTrue(preAuthParams.contains("by"));
    assertTrue(preAuthParams.contains("redirectURL"));
    assertTrue(preAuthParams.contains("timestamp"));
    assertTrue(preAuthParams.contains("expires"));
    assertEquals(9, preAuthParams.size()); // Ensure all parameters are included
  }
}
