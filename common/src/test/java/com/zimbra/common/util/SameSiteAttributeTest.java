package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;

public class SameSiteAttributeTest {

  @Test
  void addSameSiteAttributeToSetCookieHeaderWhenCalledAddSameSiteAttribute() {
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setHeader("Set-Cookie", "id=a3fWa; Max-Age=2592000");
    SameSiteAttribute.addSameSiteAttribute(response, "Lax");
    assertEquals("id=a3fWa; Max-Age=2592000; SameSite=Lax", response.getHeader("Set-Cookie"));
  }
}
