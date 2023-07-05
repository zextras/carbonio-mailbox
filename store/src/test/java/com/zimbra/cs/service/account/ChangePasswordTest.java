package com.zimbra.cs.service.account;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.common.account.Key;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.message.ChangePasswordRequest;
import com.zimbra.soap.type.AccountSelector;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChangePasswordTest {

  private Provisioning provisioning;

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.initServer();
    this.provisioning = spy(Provisioning.getInstance());
    provisioning.createAccount("test@zextras.com", "secret", new HashMap<String, Object>());
  }

 /**
  * Test for CO-329. When change password is invoked, it should forward the request context to
  * {@link com.zimbra.cs.account.Provisioning#changePassword(Account, String, String, boolean,
   * Map)}
  */
 @Test
 void shouldPassContextToProvisioningChangePassword() throws Exception {
  ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
  final String oldPassword = "secret";
  final String newPassword = "newSecret";
  final String user = "test@zextras.com";
  final boolean dryRun = false;
  changePasswordRequest.setOldPassword(oldPassword);
  changePasswordRequest.setPassword(newPassword);
  changePasswordRequest.setDryRun(dryRun);
  changePasswordRequest.setAccount(AccountSelector.fromName(user));
  Account acct = provisioning.get(Key.AccountBy.name, user);
  final Element request = JaxbUtil.jaxbToElement(changePasswordRequest);
  // prepare request
  final HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
  final Map<String, Object> ctx = new HashMap<String, Object>();
  final ZimbraSoapContext zsc =
    new ZimbraSoapContext(
      AuthProvider.getAuthToken(acct),
      acct.getId(),
      SoapProtocol.Soap12,
      SoapProtocol.Soap12);
  ctx.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
  ctx.put(SoapServlet.SERVLET_REQUEST, mockHttpRequest);
  when(mockHttpRequest.getScheme()).thenReturn("https");
  // ChangePassword and verify context is passwd down to provisioning
  new ChangePassword(provisioning).handle(request, ctx);
  verify(provisioning, times(1)).changePassword(acct, oldPassword, newPassword, dryRun, ctx);
 }
}
