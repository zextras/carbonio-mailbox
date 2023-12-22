package com.zimbra.cs.account.ldap;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.ProvisioningValidator;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DomainMaxAccountsValidatorTest {

  @BeforeEach
  void setUp() throws Exception {
    MailboxTestUtil.setUp();
  }

  @AfterEach
  void tearDown() throws ServiceException {
    MailboxTestUtil.tearDown();
  }

  @Test
  void should_return_without_failing_when_not_expected_action() {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    Assertions.assertDoesNotThrow(() ->validator.validate(Provisioning.getInstance(), ProvisioningValidator.DELETE_ACCOUNT_SUCCEEDED));
  }

  @Test
  void should_return_without_failing_when_no_condition_arguments_are_passed() {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    Assertions.assertDoesNotThrow(() ->validator.validate(
        Provisioning.getInstance(),
        ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE));
  }

  @Test
  @DisplayName(
      "should throw exception if conditional argument second element is not map of attributes")
  void should_throw_exception_if_passed_args_are_not_correct() {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();
    final Object[] conditionArguments =
        new Object[] {
          "0", "0", "0",
        };
    final Provisioning provisioning = Provisioning.getInstance();

    Assertions.assertThrows(
        ClassCastException.class,
        () ->
            validator.validate(
                provisioning,
                ProvisioningValidator.RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
                conditionArguments));
  }

  @Test
  void should_return_without_failing_when_email_null() {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();
    final Object[] conditionArguments =
        new Object[] {
          null, "0", "0",
        };

    Assertions.assertDoesNotThrow(() ->validator.validate(
        Provisioning.getInstance(),
        ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments));
  }

  @Test
  void should_return_without_failing_if_is_system_property() {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();
    final Object[] conditionArguments =
        new Object[] {
          "test@domain.com",
          Map.of(
              Provisioning.A_objectClass, new String[] {AttributeClass.OC_zimbraCalendarResource})
        };

    Assertions.assertDoesNotThrow(() ->validator.validate(
        Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments));
  }

  @Test
  void should_return_without_failing_if_is_external_account() {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();
    final Object[] conditionArguments =
        new Object[] {
          "test@domain.com", Map.of(Provisioning.A_zimbraIsExternalVirtualAccount, true)
        };

    Assertions.assertDoesNotThrow(() ->validator.validate(
        Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments));
  }

  @Test
  void should_return_without_failing_if_email_without_domain_name() {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();
    final Account account = Mockito.mock(Account.class);
    final Object[] conditionArguments = new Object[] {"test", Map.of(), account};

    Assertions.assertDoesNotThrow(() ->validator.validate(
        Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments));
  }

  @Test
  void should_return_without_failing_if_domain_does_not_exist() {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final Account account = Mockito.mock(Account.class);

    final Object[] conditionArguments = new Object[] {"test@domain.com", Map.of(), account};

    Assertions.assertDoesNotThrow(() ->validator.validate(
        Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments));
  }

  @Test
  @DisplayName("should set 'default' COS id if domain zimbraDomainDefaultCOSId is null")
  void should_pass_without_failing_if_domain_cos_id_null() throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final AccountCreator.Factory accountCreatorFactory =
        new AccountCreator.Factory(Provisioning.getInstance());
    final Account account =
        accountCreatorFactory
            .get()
            .withDomain(MailboxTestUtil.DEFAULT_DOMAIN)
            .withUsername("user")
            .create();
    final Object[] conditionArguments =
        new Object[] {String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of(), account};

    Assertions.assertDoesNotThrow(() ->validator.validate(
        Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments));
  }

  @Test
  void should_return_without_failing_if_cosLimit_and_featureLimit_are_empty()
      throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final AccountCreator.Factory accountCreatorFactory =
        new AccountCreator.Factory(Provisioning.getInstance());
    final Account account =
        accountCreatorFactory
            .get()
            .withDomain(MailboxTestUtil.DEFAULT_DOMAIN)
            .withUsername("user")
            .create();
    final Object[] conditionArguments =
        new Object[] {String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of(), account};

    Assertions.assertDoesNotThrow(() ->validator.validate(
        Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments));
  }

  // validate limits for action create
  @Test
  void should_throw_if_reaches_feature_max_accounts_limit_when_action_create() throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final Domain domain =
        Provisioning.getInstance().getDomain(DomainBy.name, MailboxTestUtil.DEFAULT_DOMAIN, false);
    domain.setDomainFeatureMaxAccounts(new String[] {"zimbraFeatureChatEnabled:0"});

    final Object[] conditionArguments =
        new Object[] {String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of("zimbraFeatureChatEnabled", Boolean.TRUE)};

    final AccountServiceException accountServiceException = Assertions.assertThrows(
        AccountServiceException.class,
        () ->
            validator.validate(
                Provisioning.getInstance(),
                ProvisioningValidator.CREATE_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
                conditionArguments));
    Assertions.assertEquals("number of accounts reached the limit: domain=test.com[zimbraFeatureChatEnabled,count=0,limit=0]", accountServiceException.getMessage());
  }

  @Test
  void should_throw_if_reaches_cos_max_accounts_limit_when_action_create() throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final String cosId = Provisioning.getInstance().getCosByName(Provisioning.DEFAULT_COS_NAME).getId();

    final Domain domain =
        Provisioning.getInstance().getDomain(DomainBy.name, MailboxTestUtil.DEFAULT_DOMAIN, false);
    domain.setDomainDefaultCOSId(cosId);
    domain.setDomainCOSMaxAccounts(new String[] {cosId + ":0"});

    final Object[] conditionArguments =
        new Object[] {String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of("zimbraCOSId", cosId)};

    final AccountServiceException accountServiceException = Assertions.assertThrows(
        AccountServiceException.class,
        () ->
            validator.validate(
                Provisioning.getInstance(),
                ProvisioningValidator.CREATE_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
                conditionArguments));
    Assertions.assertEquals("number of accounts reached the limit: domain=test.com[cos=e00428a1-0c00-11d9-836a-000d93afea2a,count=0,limit=0]", accountServiceException.getMessage());
  }

  @Test
  void should_pass_without_failing_if_feature_max_accounts_limit_not_reached_when_action_create() throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final Domain domain =
        Provisioning.getInstance().getDomain(DomainBy.name, MailboxTestUtil.DEFAULT_DOMAIN, false);
    domain.setDomainFeatureMaxAccounts(new String[] {"zimbraFeatureChatEnabled:1"});

    final Object[] conditionArguments =
        new Object[] {String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of("zimbraFeatureChatEnabled", Boolean.TRUE)};

    Assertions.assertDoesNotThrow(() ->
        validator.validate(
            Provisioning.getInstance(),
            ProvisioningValidator.CREATE_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
            conditionArguments));
  }

  @Test
  void should_pass_without_failing_if_cos_max_accounts_limit_not_reached_when_action_create() throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final String cosId = Provisioning.getInstance().getCosByName(Provisioning.DEFAULT_COS_NAME).getId();

    final Domain domain =
        Provisioning.getInstance().getDomain(DomainBy.name, MailboxTestUtil.DEFAULT_DOMAIN, false);
    domain.setDomainDefaultCOSId(cosId);
    domain.setDomainCOSMaxAccounts(new String[] {cosId + ":1"});

    final Object[] conditionArguments =
        new Object[] {String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of("zimbraCOSId", cosId)};

    Assertions.assertDoesNotThrow(() ->
            validator.validate(
                Provisioning.getInstance(),
                ProvisioningValidator.CREATE_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
                conditionArguments));
  }



  // rename account action
  @Test
  @DisplayName("Test passing when 3 args and action rename account")
  void should_pass_without_failing_if_Domain_COS_Max_Accounts_set() throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final String cosId = Provisioning.getInstance().getCosByName(Provisioning.DEFAULT_COS_NAME).getId();
    final Domain domain =
        Provisioning.getInstance().getDomain(DomainBy.name, MailboxTestUtil.DEFAULT_DOMAIN, false);
    domain.setDomainDefaultCOSId(cosId);
    domain.setDomainCOSMaxAccounts(new String[] {cosId + ":1"});

    final AccountCreator.Factory accountCreatorFactory =
        new AccountCreator.Factory(Provisioning.getInstance());
    final Account account =
        accountCreatorFactory
            .get()
            .withDomain(MailboxTestUtil.DEFAULT_DOMAIN)
            .withUsername("user")
            .create();
    final Object[] conditionArguments =
        new Object[] {String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of(), account};

    validator.validate(
        Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments);
  }

  @Test
  @DisplayName("Test passing when 3 args and action rename account")
  void should_pass_without_failing_if_Domain_Feature_Max_Accounts_set() throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final AccountCreator.Factory accountCreatorFactory =
        new AccountCreator.Factory(Provisioning.getInstance());
    final Domain domain =
        Provisioning.getInstance().getDomain(DomainBy.name, MailboxTestUtil.DEFAULT_DOMAIN, false);
    domain.setDomainFeatureMaxAccounts(new String[] {"zimbraFeatureChatEnabled:1"});
    final Account account =
        accountCreatorFactory
            .get()
            .withDomain(MailboxTestUtil.DEFAULT_DOMAIN)
            .withUsername("user")
            .create();
    final Object[] conditionArguments =
        new Object[] {String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of(), account};

    validator.validate(
        Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments);
  }

  @Test
  @DisplayName(
      "throwing exception if conditional arguments size is not 3 and action rename account")
  void should_throw_exception_if_passed_args_not_equals_to_three() throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator =
        new Validators.DomainMaxAccountsValidator();

    final AccountCreator.Factory accountCreatorFactory =
        new AccountCreator.Factory(Provisioning.getInstance());
    final Domain domain =
        Provisioning.getInstance().getDomain(DomainBy.name, MailboxTestUtil.DEFAULT_DOMAIN, false);
    domain.setDomainCOSMaxAccounts(new String[] {"default:30"});
    final Account account =
        accountCreatorFactory
            .get()
            .withDomain(MailboxTestUtil.DEFAULT_DOMAIN)
            .withUsername("user")
            .create();
    final Object[] conditionArguments =
        new Object[] {
          String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN),
          Map.of(),
          account,
          "unexpected arg"
        };

    Assertions.assertThrows(
        ServiceException.class,
        () ->
            validator.validate(
                Provisioning.getInstance(),
                ProvisioningValidator.RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
                conditionArguments));
  }
}
