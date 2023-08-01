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
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void testCustomUrlEmpty() {
    String keyword = "web.custom.logout.redirect";
    String attribute = ZAttrProvisioning.A_carbonioAdminUILogoutURL;
    Object defaultValue = "/static/login/";
    String description = "Custom logout redirect URL for web proxy";
    String customUrl = "";

    WebCustomLogoutRedirectVar var =
        new WebCustomLogoutRedirectVar(keyword, attribute, defaultValue, description, customUrl);

    assertEquals("return 307 /static/login/", var.mValue);
  }

  @Test
  void testCustomUrlNotEmpty() {
    String keyword = "web.custom.logout.redirect";
    String attribute = ZAttrProvisioning.A_carbonioAdminUILogoutURL;
    Object defaultValue = "/static/login/";
    String description = "Custom logout redirect URL for web proxy";
    String customUrl = "https://custom-logout-url.com";

    WebCustomLogoutRedirectVar var =
        new WebCustomLogoutRedirectVar(keyword, attribute, defaultValue, description, customUrl);

    assertEquals("return 200", var.mValue);
  }
}
