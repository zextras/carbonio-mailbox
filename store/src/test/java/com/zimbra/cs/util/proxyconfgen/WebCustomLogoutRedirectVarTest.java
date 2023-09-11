package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WebCustomLogoutRedirectVarTest {

  private Provisioning provisioning;

  @BeforeEach
  void setUp() {
    provisioning = mock(Provisioning.class);
  }

  @Test
  void update_should_setDefaultValue_when_customUrlEmpty() {
    final String keyword = "web.custom.logout.redirect";
    final String attribute = ZAttrProvisioning.A_carbonioAdminUILogoutURL;
    final Object defaultValue = "/static/login/";
    final String description = "Custom logout redirect URL for web proxy";
    final String customUrl = "";

    final WebCustomLogoutRedirectVar var =
        new WebCustomLogoutRedirectVar(
            provisioning, keyword, attribute, defaultValue, description, customUrl);
    var.update();
    assertEquals("return 307 /static/login/", var.mValue);
  }

  @Test
  void update_should_setValue_when_customUrlIsNotEmpty() {
    final String keyword = "web.custom.logout.redirect";
    final String attribute = ZAttrProvisioning.A_carbonioAdminUILogoutURL;
    final Object defaultValue = "/static/login/";
    final String description = "Custom logout redirect URL for web proxy";
    final String customUrl = "https://custom-logout-url.com";

    final WebCustomLogoutRedirectVar var =
        new WebCustomLogoutRedirectVar(
            provisioning, keyword, attribute, defaultValue, description, customUrl);
    var.update();
    assertEquals("return 200", var.mValue);
  }
}
