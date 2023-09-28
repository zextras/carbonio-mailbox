package com.zimbra.cs.service.admin;

import static com.zextras.mailbox.usecase.MailboxTestUtil.DEFAULT_DOMAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.zextras.mailbox.usecase.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AddAccountLoggerRequest;
import com.zimbra.soap.admin.message.AddAccountLoggerResponse;
import com.zimbra.soap.admin.type.LoggerInfo;
import com.zimbra.soap.type.AccountBy;
import com.zimbra.soap.type.AccountSelector;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AddAccountLoggerTest {
  private Provisioning provisioning;
  private Account account;
  private String email;

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.setUp();
    provisioning = Provisioning.getInstance();
    email = "admin_mail@" + DEFAULT_DOMAIN;
    final Map<String, Object> attributes = new HashMap<>();
    attributes.put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
    account = provisioning.createAccount(email, "password", attributes);
  }

  @AfterEach
  void tearDown() {
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldReturnResponseWithProperAttributes() throws ServiceException {
    final String category = "zimbra.soap";
    final String level = "debug";
    final AccountSelector accountSelector = new AccountSelector(AccountBy.name, email);
    final LoggerInfo loggerInfo = LoggerInfo.createForCategoryAndLevelString(category, level);
    final AddAccountLoggerRequest addAccountLoggerRequest =
        AddAccountLoggerRequest.createForAccountAndLogger(accountSelector, loggerInfo);
    final Element request = JaxbUtil.jaxbToElement(addAccountLoggerRequest);
    final Map<String, Object> context = new HashMap<>();
    final ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(account, true),
            account.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    final AddAccountLogger addAccountLogger = new AddAccountLogger(provisioning);
    final AddAccountLoggerResponse response =
        JaxbUtil.elementToJaxb(addAccountLogger.handle(request, context));
    assertNotNull(response);
    assertEquals(1, response.getLoggers().size());
    assertEquals(level, response.getLoggers().get(0).getLevel().toString());
    assertEquals(category, response.getLoggers().get(0).getCategory());
  }
}
