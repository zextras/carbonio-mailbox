package com.zimbra.cs.account.ldap;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
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
  void should_return_without_failing_when_no_condition_arguments_are_passed()
      throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator = new Validators.DomainMaxAccountsValidator();

    validator.validate(Provisioning.getInstance(),
        ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE);
  }

  @Test
  @DisplayName("should throw exception if conditional argument second element is not map of attributes")
  void should_throw_exception_if_passed_args_are_not_correct() {
    final Validators.DomainMaxAccountsValidator validator = new Validators.DomainMaxAccountsValidator();
    final Object[] conditionArguments = new Object[]{
        "0",
        "0",
        "0",
    };
    final Provisioning provisioning = Provisioning.getInstance();

    Assertions.assertThrows(ClassCastException.class,
        () -> validator.validate(provisioning,
            ProvisioningValidator.RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE, conditionArguments));

  }


  @Test
  void should_return_without_failure_if_is_system_property()
      throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator = new Validators.DomainMaxAccountsValidator();
    final Object[] conditionArguments = new Object[]{
        "test@domain.com",
        Map.of(Provisioning.A_objectClass, new String[]{AttributeClass.OC_zimbraCalendarResource})
    };

    validator.validate(Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments);
  }

  @Test
  void should_return_without_failure_if_is_external_account()
      throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator = new Validators.DomainMaxAccountsValidator();
    final Object[] conditionArguments = new Object[]{
        "test@domain.com",
        Map.of(Provisioning.A_zimbraIsExternalVirtualAccount, true)
    };

    validator.validate(Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments);
  }

  @Test
  void should_return_without_failing_if_domain_is_null()
      throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator = new Validators.DomainMaxAccountsValidator();
    final Account account = Mockito.mock(Account.class);
    final Object[] conditionArguments = new Object[]{
        "test", Map.of(), account
    };

    validator.validate(Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments);
  }

  @Test
  void should_return_without_failing_if_domain_does_not_exists()
      throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator = new Validators.DomainMaxAccountsValidator();

    Account account = Mockito.mock(Account.class);

    final Object[] conditionArguments = new Object[]{
        "test@domain.com", Map.of(), account
    };

    validator.validate(Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments);
  }


  @Test
  void should_return_without_failing_if_cosLimit_and_featureLimit_are_zero()
      throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator = new Validators.DomainMaxAccountsValidator();

    final AccountCreator.Factory accountCreatorFactory = new AccountCreator.Factory(
        Provisioning.getInstance());
    final Account account = accountCreatorFactory.get().withDomain(MailboxTestUtil.DEFAULT_DOMAIN)
        .withUsername("user").create();
    final Object[] conditionArguments = new Object[]{
        String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of(), account
    };

    validator.validate(Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.MODIFY_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments);
  }

  @Test
  void test()
      throws ServiceException {
    final Validators.DomainMaxAccountsValidator validator = new Validators.DomainMaxAccountsValidator();

    final AccountCreator.Factory accountCreatorFactory = new AccountCreator.Factory(
        Provisioning.getInstance());
    final Domain domain = Provisioning.getInstance()
        .getDomain(DomainBy.name, MailboxTestUtil.DEFAULT_DOMAIN, false);
    domain.setDomainCOSMaxAccounts(new String[]{"default:30"});
    final Account account = accountCreatorFactory.get().withDomain(MailboxTestUtil.DEFAULT_DOMAIN)
        .withUsername("user").create();
    final Object[] conditionArguments = new Object[]{
        String.format("user@%s", MailboxTestUtil.DEFAULT_DOMAIN), Map.of(), account
    };

    validator.validate(Provisioning.getInstance(),
        Provisioning.ProvisioningValidator.RENAME_ACCOUNT_CHECK_DOMAIN_COS_AND_FEATURE,
        conditionArguments);
  }


}