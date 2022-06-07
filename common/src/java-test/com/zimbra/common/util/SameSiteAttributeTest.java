package com.zimbra.common.util;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

public class SameSiteAttributeTest {

  @Test
  public void addSameSiteAttributeToSetCookieHeaderWhenCalledAddSameSiteAttribute() {
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setHeader("Set-Cookie", "id=a3fWa; Max-Age=2592000");
    SameSiteAttribute.addSameSiteAttribute(response, "Lax");
    assertEquals("id=a3fWa; Max-Age=2592000; SameSite=Lax", response.getHeader("Set-Cookie"));
  }
}
