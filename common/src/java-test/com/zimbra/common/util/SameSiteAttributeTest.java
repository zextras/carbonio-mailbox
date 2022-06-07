package com.zimbra.common.util;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

public class SameSiteAttributeTest {

  @Test
  public void addSameSiteAttributeToSetCookieHeaderWhenCalledAddSameSiteAttribute() {
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setHeader("Set-Cookie", "id=a3fWa; Max-Age=2592000");
    SameSiteAttribute.addSameSiteAttribute(response, "Lax");
    assertThat(response.getHeader("Set-Cookie"), containsString("SameSite=Lax"));
  }
}
