package com.zimbra.cs.service.mail;


import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.account.ZAttrProvisioning.FeatureResetPasswordStatus;
import com.zimbra.common.account.ZAttrProvisioning.PrefPasswordRecoveryAddressStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ForgetPasswordException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.RecoverAccountRequest;
import com.zimbra.soap.mail.message.RecoverAccountResponse;
import com.zimbra.soap.mail.type.RecoverAccountOperation;
import com.zimbra.soap.type.Channel;
import java.util.concurrent.TimeUnit;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Tag("api")
class RecoverAccountTest extends SoapTestSuite {

  private static AccountCreator.Factory accountCreatorFactory;

  @BeforeAll
  static void setUp() throws Exception {
    accountCreatorFactory = new AccountCreator.Factory(Provisioning.getInstance());
  }

  @Test
  @DisplayName("Should throw an exception when unknown account requested")
  void should_throw_exception_if_account_not_known() throws ServiceException {
    Account primaryAccount = accountCreatorFactory.get().create();
    primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

    RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
    recoverAccountRequest.setOp(RecoverAccountOperation.GET_RECOVERY_ACCOUNT);
    recoverAccountRequest.setEmail("break_account" + primaryAccount.getName());

    final ForgetPasswordException forgetPasswordException = Assertions.assertThrows(
        ForgetPasswordException.class,
        () -> new RecoverAccount().handle(JaxbUtil.jaxbToElement(recoverAccountRequest),
            ServiceTestUtil.getRequestContext(primaryAccount)));

    Assertions.assertEquals(
        "service exception: Something went wrong. Please contact your administrator.",
        forgetPasswordException.getMessage());
    Assertions.assertEquals("service.CONTACT_ADMIN", forgetPasswordException.getCode());
  }

  @Test
  @DisplayName("Should throw exception if no recovery account is set and op is GET_RECOVERY_ACCOUNT")
  void should_throw_exception_when_recovery_email_not_set_and_op_is_get_recovery_account()
      throws ServiceException {
    Account primaryAccount = accountCreatorFactory.get().create();
    primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

    RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
    recoverAccountRequest.setOp(RecoverAccountOperation.GET_RECOVERY_ACCOUNT);
    recoverAccountRequest.setEmail(primaryAccount.getName());

    final ForgetPasswordException forgetPasswordException = Assertions.assertThrows(
        ForgetPasswordException.class,
        () -> new RecoverAccount().handle(JaxbUtil.jaxbToElement(recoverAccountRequest),
            ServiceTestUtil.getRequestContext(primaryAccount)));

    Assertions.assertEquals(
        "service exception: Recovery Account is not found. Please contact your administrator.",
        forgetPasswordException.getMessage());
    Assertions.assertEquals("service.CONTACT_ADMIN", forgetPasswordException.getCode());
  }

  @Test
  @DisplayName("Should return masked recovery account name if recovery account is set and verified")
  void should_return_masked_recovery_account_when_recovery_account_set_and_verified()
      throws Exception {
    Account recoveryAccount = accountCreatorFactory.get().create();
    Account primaryAccount = accountCreatorFactory.get().create();
    primaryAccount.setPrefPasswordRecoveryAddress(recoveryAccount.getName());
    primaryAccount.setPrefPasswordRecoveryAddressStatus(
        PrefPasswordRecoveryAddressStatus.verified);
    primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

    RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
    recoverAccountRequest.setOp(RecoverAccountOperation.GET_RECOVERY_ACCOUNT);
    recoverAccountRequest.setEmail(primaryAccount.getName());

    RecoverAccountResponse response = JaxbUtil.elementToJaxb(new RecoverAccount().handle(
        JaxbUtil.jaxbToElement(recoverAccountRequest),
        ServiceTestUtil.getRequestContext(primaryAccount)));

    Assertions.assertNotNull(response);
    Assertions.assertEquals(StringUtil.maskEmail(recoveryAccount.getName()),
        response.getRecoveryAccount());
  }

  @Test
  @DisplayName("Should throw an exception if recovery account is not verified")
  void should_throw_exception_when_recovery_account_is_not_verified()
      throws Exception {
    Account recoveryAccount = accountCreatorFactory.get().create();
    Account primaryAccount = accountCreatorFactory.get().create();
    primaryAccount.setPrefPasswordRecoveryAddress(recoveryAccount.getName());
    primaryAccount.setPrefPasswordRecoveryAddressStatus(
        PrefPasswordRecoveryAddressStatus.pending);
    primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

    RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
    recoverAccountRequest.setOp(RecoverAccountOperation.GET_RECOVERY_ACCOUNT);
    recoverAccountRequest.setEmail(primaryAccount.getName());

    final ForgetPasswordException forgetPasswordException = Assertions.assertThrows(
        ForgetPasswordException.class,
        () -> new RecoverAccount().handle(JaxbUtil.jaxbToElement(recoverAccountRequest),
            ServiceTestUtil.getRequestContext(primaryAccount)));

    Assertions.assertEquals(
        "service exception: Recovery Account is not verified. Please contact your administrator.",
        forgetPasswordException.getMessage());
    Assertions.assertEquals("service.CONTACT_ADMIN",
        forgetPasswordException.getCode());
  }

  @Test
  @DisplayName("Should throw ForgotPasswordException when zimbraFeatureResetPasswordStatus is not enabled")
  void should_throw_exception_when_feature_is_not_enabled() throws Exception {
    Account primaryAccount = accountCreatorFactory.get().create();

    RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
    recoverAccountRequest.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
    recoverAccountRequest.setEmail(primaryAccount.getName());
    recoverAccountRequest.setChannel(Channel.EMAIL);

    final ForgetPasswordException forgetPasswordException = Assertions.assertThrows(
        ForgetPasswordException.class,
        () -> new RecoverAccount().handle(JaxbUtil.jaxbToElement(recoverAccountRequest),
            ServiceTestUtil.getRequestContext(primaryAccount)));

    Assertions.assertEquals("service exception: Password reset feature is disabled.",
        forgetPasswordException.getMessage());
    Assertions.assertEquals("service.FEATURE_RESET_PASSWORD_DISABLED",
        forgetPasswordException.getCode());
  }

  @Test
  @DisplayName("Should throw exception if recovery account is not set on primary account")
  void should_throw_exception_when_recovery_account_is_not_set() throws Exception {
    Account primaryAccount = accountCreatorFactory.get().create();
    primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
    RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
    recoverAccountRequest.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
    recoverAccountRequest.setEmail(primaryAccount.getName());
    recoverAccountRequest.setChannel(Channel.EMAIL);

    final ForgetPasswordException forgetPasswordException = Assertions.assertThrows(
        ForgetPasswordException.class, () -> new RecoverAccount().handle(
            JaxbUtil.jaxbToElement(recoverAccountRequest),
            ServiceTestUtil.getRequestContext(primaryAccount)));

    Assertions.assertEquals(
        "service exception: Recovery Account is not found. Please contact your administrator.",
        forgetPasswordException.getMessage());
    Assertions.assertEquals("service.CONTACT_ADMIN", forgetPasswordException.getCode());
  }

  @Test
  @DisplayName("Should throw ForgotPasswordException when zimbraPrefPasswordRecoveryAddressStatus is not verified")
  void should_throw_exception_when_recovery_email_is_not_verified() throws Exception {
    GreenMail greenMail = new GreenMail(new ServerSetup[]{
        new ServerSetup(
            SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
    });

    try {
      greenMail.start();

      Account recoveryAccount = accountCreatorFactory.get().create();
      Account primaryAccount = accountCreatorFactory.get().create();
      primaryAccount.setPrefPasswordRecoveryAddress(recoveryAccount.getName());
      primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
      primaryAccount.setPrefPasswordRecoveryAddressStatus(
          PrefPasswordRecoveryAddressStatus.pending);

      RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
      recoverAccountRequest.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
      recoverAccountRequest.setEmail(primaryAccount.getName());
      recoverAccountRequest.setChannel(Channel.EMAIL);

      final ForgetPasswordException forgetPasswordException = Assertions.assertThrows(
          ForgetPasswordException.class, () -> new RecoverAccount().handle(
              JaxbUtil.jaxbToElement(recoverAccountRequest),
              ServiceTestUtil.getRequestContext(primaryAccount)));

      Assertions.assertEquals(
          "service exception: Recovery Account is not verified. Please contact your administrator.",
          forgetPasswordException.getMessage());
      Assertions.assertEquals("service.CONTACT_ADMIN", forgetPasswordException.getCode());

    } finally {
      greenMail.stop();
    }
  }

  @Test
  @DisplayName("Should reduce number of send recovery code attempts on each sendCode request")
  void should_reduce_number_of_attempts_on_each_sendCode_request() throws Exception {
    GreenMail greenMail = new GreenMail(new ServerSetup[]{
        new ServerSetup(
            SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
    });

    try {
      greenMail.start();

      Account recoveryAccount = accountCreatorFactory.get().create();
      Account primaryAccount = accountCreatorFactory.get().create();
      primaryAccount.setPrefPasswordRecoveryAddress(recoveryAccount.getName());
      primaryAccount.setPrefPasswordRecoveryAddressStatus(
          PrefPasswordRecoveryAddressStatus.verified);
      primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

      RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
      recoverAccountRequest.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
      recoverAccountRequest.setEmail(primaryAccount.getName());
      recoverAccountRequest.setChannel(Channel.EMAIL);

      RecoverAccountResponse response = JaxbUtil.elementToJaxb(new RecoverAccount().handle(
          JaxbUtil.jaxbToElement(recoverAccountRequest),
          ServiceTestUtil.getRequestContext(primaryAccount)));

      Assertions.assertNotNull(response);
      Assertions.assertEquals(10, (int) response.getRecoveryAttemptsLeft());

      response = JaxbUtil.elementToJaxb(new RecoverAccount().handle(
          JaxbUtil.jaxbToElement(recoverAccountRequest),
          ServiceTestUtil.getRequestContext(primaryAccount)));

      Assertions.assertNotNull(response);
      Assertions.assertEquals(9, (int) response.getRecoveryAttemptsLeft());

    } finally {
      greenMail.stop();
    }
  }

  @Test
  @DisplayName("Should use the default channel i.e, email when not specified in request")
  void should_fallback_to_email_channel_when_not_specified_in_request() throws Exception {
    GreenMail greenMail = new GreenMail(new ServerSetup[]{
        new ServerSetup(
            SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
    });

    try {
      greenMail.start();

      Account recoveryAccount = accountCreatorFactory.get().create();
      Account primaryAccount = accountCreatorFactory.get().create();
      primaryAccount.setPrefPasswordRecoveryAddress(recoveryAccount.getName());
      primaryAccount.setPrefPasswordRecoveryAddressStatus(
          PrefPasswordRecoveryAddressStatus.verified);
      primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

      RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
      recoverAccountRequest.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
      recoverAccountRequest.setEmail(primaryAccount.getName());

      new RecoverAccount().handle(
          JaxbUtil.jaxbToElement(recoverAccountRequest),
          ServiceTestUtil.getRequestContext(primaryAccount));

      Assertions.assertEquals(1, greenMail.getReceivedMessages().length);

    } catch (ServiceException se) {
      Assertions.fail(
          "Exception should not be thrown when channel is not specified in the request");
    } finally {
      greenMail.stop();
    }
  }

  @Test
  @DisplayName("Should send email with recovery code when FeatureResetPasswordStatus is set enabled")
  void should_send_recovery_code_when_requested() throws Exception {
    GreenMail greenMail = new GreenMail(new ServerSetup[]{
        new ServerSetup(
            SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
    });

    try {
      greenMail.start();

      Account recoveryAccount = accountCreatorFactory.get().create();
      recoveryAccount.setLocale("ta");
      Account primaryAccount = accountCreatorFactory.get().create();
      primaryAccount.setLocale("ta");
      primaryAccount.setPrefPasswordRecoveryAddress(recoveryAccount.getName());
      primaryAccount.setPrefPasswordRecoveryAddressStatus(
          PrefPasswordRecoveryAddressStatus.verified);
      primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);

      RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
      recoverAccountRequest.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
      recoverAccountRequest.setEmail(primaryAccount.getName());
      recoverAccountRequest.setChannel(Channel.EMAIL);

      final RecoverAccountResponse response = JaxbUtil.elementToJaxb(new RecoverAccount().handle(
          JaxbUtil.jaxbToElement(recoverAccountRequest),
          ServiceTestUtil.getRequestContext(primaryAccount)));

      Assertions.assertNotNull(response);

      MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
      Assertions.assertEquals(1, receivedMessages.length);

      final MimeMessage receivedMessage = receivedMessages[0];
      final String expectedSender = receivedMessage.getFrom()[0].toString();
      Assertions.assertEquals(expectedSender, primaryAccount.getName());

      final String expectedRecipient = receivedMessage.getAllRecipients()[0].toString();
      Assertions.assertEquals(expectedRecipient, recoveryAccount.getName());

      Assertions.assertEquals(
          String.format("Temporary code to access your %s account", primaryAccount.getDomainName()),
          receivedMessage.getSubject(), "Subject of the received message");

      final Object messageContent = ((MimeMultipart) receivedMessage.getContent()).getBodyPart(0)
          .getContent();
      Assertions.assertTrue(messageContent.toString()
              .contains(
                  "We have received a request for a temporary access code to facilitate account access"),
          "Body of the received message contains");

    } finally {
      greenMail.stop();
    }
  }

  @Test
  @DisplayName("Should throw ForgetPasswordException when exceeds passwordRecoveryMaxAttempts limit")
  void should_throw_exception_when_exceeds_passwordRecoveryMaxAttempts_limit() throws Exception {
    GreenMail greenMail = new GreenMail(new ServerSetup[]{
        new ServerSetup(
            SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
    });

    try {
      greenMail.start();

      Account recoveryAccount = accountCreatorFactory.get().create();
      Account primaryAccount = accountCreatorFactory.get().create();
      primaryAccount.setPrefPasswordRecoveryAddress(recoveryAccount.getName());
      primaryAccount.setPrefPasswordRecoveryAddressStatus(
          PrefPasswordRecoveryAddressStatus.verified);
      primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
      primaryAccount.setPasswordRecoveryMaxAttempts(3);

      RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
      recoverAccountRequest.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
      recoverAccountRequest.setEmail(primaryAccount.getName());
      recoverAccountRequest.setChannel(Channel.EMAIL);

      final Element recoverAccountRequestElement = JaxbUtil.jaxbToElement(recoverAccountRequest);

      for (int i = 0; i < 4; i++) {
        new RecoverAccount().handle(recoverAccountRequestElement,
            ServiceTestUtil.getRequestContext(primaryAccount));
      }

      final ForgetPasswordException forgetPasswordException = Assertions.assertThrows(
          ForgetPasswordException.class, () -> new RecoverAccount().handle(
              recoverAccountRequestElement, ServiceTestUtil.getRequestContext(primaryAccount)));

      Assertions.assertEquals(
          "service exception: Max re-send attempts reached, feature is suspended.",
          forgetPasswordException.getMessage());
      Assertions.assertEquals(FeatureResetPasswordStatus.suspended,
          Provisioning.getInstance().getAccount(primaryAccount.getId())
              .getFeatureResetPasswordStatus());
    } finally {
      greenMail.stop();
    }
  }

  @Test
  @DisplayName("Should re-enable the recover account feature (zimbraFeatureResetPasswordStatus) after suspension duration timeout")
  void should_re_enable_feature_when_suspension_timeouts() throws Exception {
    GreenMail greenMail = new GreenMail(new ServerSetup[]{
        new ServerSetup(
            SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
    });

    try {
      greenMail.start();

      Account recoveryAccount = accountCreatorFactory.get().create();
      Account primaryAccount = accountCreatorFactory.get().create();
      primaryAccount.setPrefPasswordRecoveryAddress(recoveryAccount.getName());
      primaryAccount.setPrefPasswordRecoveryAddressStatus(
          PrefPasswordRecoveryAddressStatus.verified);
      primaryAccount.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
      primaryAccount.setPasswordRecoveryMaxAttempts(2);
      primaryAccount.setFeatureResetPasswordSuspensionTime("2s");

      RecoverAccountRequest recoverAccountRequest = new RecoverAccountRequest();
      recoverAccountRequest.setOp(RecoverAccountOperation.SEND_RECOVERY_CODE);
      recoverAccountRequest.setEmail(primaryAccount.getName());
      recoverAccountRequest.setChannel(Channel.EMAIL);

      final Element recoverAccountRequestElement = JaxbUtil.jaxbToElement(recoverAccountRequest);

      for (int i = 0; i < 3; i++) {
        new RecoverAccount().handle(recoverAccountRequestElement,
            ServiceTestUtil.getRequestContext(primaryAccount));
      }

      final ForgetPasswordException forgetPasswordException = Assertions.assertThrows(
          ForgetPasswordException.class, () -> new RecoverAccount().handle(
              recoverAccountRequestElement, ServiceTestUtil.getRequestContext(primaryAccount)));

      Assertions.assertEquals(
          "service exception: Max re-send attempts reached, feature is suspended.",
          forgetPasswordException.getMessage());

      Assertions.assertEquals(FeatureResetPasswordStatus.suspended,
          Provisioning.getInstance().getAccount(primaryAccount.getId())
              .getFeatureResetPasswordStatus());

      Assertions.assertEquals(3, greenMail.getReceivedMessages().length);

      Awaitility.await()
          .atMost(3, TimeUnit.SECONDS)
          .pollInterval(2, TimeUnit.SECONDS)
          .until(() -> {
            new RecoverAccount().handle(recoverAccountRequestElement,
                ServiceTestUtil.getRequestContext(primaryAccount));
            return Provisioning.getInstance().getAccount(primaryAccount.getId())
                .getFeatureResetPasswordStatus() == FeatureResetPasswordStatus.enabled;
          });

      Assertions.assertEquals(FeatureResetPasswordStatus.enabled,
          Provisioning.getInstance().getAccount(primaryAccount.getId())
              .getFeatureResetPasswordStatus());

    } finally {
      greenMail.stop();
    }
  }

}