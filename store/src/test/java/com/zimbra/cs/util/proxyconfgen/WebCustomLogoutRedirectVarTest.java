package com.zimbra.cs.util.proxyconfgen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WebCustomLogoutRedirectVarTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  void constructor_should_setDefaultValue_when_customUrlEmpty() {
    final String keyword = "web.custom.logout.redirect";
    final String attribute = ZAttrProvisioning.A_carbonioAdminUILogoutURL;
    final Object defaultValue = "/static/login/";
    final String description = "Custom logout redirect URL for web proxy";
    final String customUrl = "";

    final WebCustomLogoutRedirectVar var =
        new WebCustomLogoutRedirectVar(keyword, attribute, defaultValue, description, customUrl);

    assertEquals("return 307 /static/login/", var.mValue);
  }

  @Test
  void constructor_should_setValue_when_customUrlIsNotEmpty() {
    final String keyword = "web.custom.logout.redirect";
    final String attribute = ZAttrProvisioning.A_carbonioAdminUILogoutURL;
    final Object defaultValue = "/static/login/";
    final String description = "Custom logout redirect URL for web proxy";
    final String customUrl = "https://custom-logout-url.com";

    final WebCustomLogoutRedirectVar var =
        new WebCustomLogoutRedirectVar(keyword, attribute, defaultValue, description, customUrl);

    assertEquals("return 200", var.mValue);
  }
}
