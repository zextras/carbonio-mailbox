package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AddAccountLoggerTest extends MailboxTestSuite {
  private static Provisioning provisioning;
  private static Account account;
  private static String email;

  @BeforeAll
  static void setUp() throws Exception {
    provisioning = Provisioning.getInstance();
    email = "admin_mail@" + mailboxTestExtension.getDefaultDomain();
    final Map<String, Object> attributes = new HashMap<>();
    attributes.put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
    account = provisioning.createAccount(email, "password", attributes);
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
