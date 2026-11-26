package com.zextras.mailbox.quota;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.util.AccountUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ZimbraMailQuotaCheckTest extends MailboxTestSuite {

  @AfterEach
  void restoreFlag() {
    LC.bypass_carbonio_storages.setDefault(false);
  }

  @Test
  void quotaIsNotEnforcedWhenBypassIsDisabled() throws Exception {
    final Account account = createAccount().create();
    setMailQuota(account, 1);

    assertEquals(0, AccountUtil.getEffectiveQuota(account));
    assertDoesNotThrow(() -> addMessage(account));
  }

  @Test
  void addingMessageOverAccountQuotaIsDeniedWhenBypassIsEnabled() throws Exception {
    final Account account = createAccount().create();
    setMailQuota(account, 1);
    bypassCarbonioStorages(true);

    assertQuotaExceeded(() -> addMessage(account));
  }

  @Test
  void changingTheFlagAppliesToTheNextOperation() throws Exception {
    final Account account = createAccount().create();
    setMailQuota(account, 1);

    bypassCarbonioStorages(true);
    assertQuotaExceeded(() -> addMessage(account));

    bypassCarbonioStorages(false);
    assertDoesNotThrow(() -> addMessage(account));
  }

  @Test
  void effectiveQuotaIsTheLowerBetweenAccountAndDomainQuota() throws Exception {
    bypassCarbonioStorages(true);

    assertEquals(100, effectiveQuota(500, 100));
    assertEquals(100, effectiveQuota(100, 500));
  }

  @Test
  void effectiveQuotaFallsBackToTheConfiguredOne() throws Exception {
    bypassCarbonioStorages(true);

    assertEquals(500, effectiveQuota(500, 0));
    assertEquals(100, effectiveQuota(0, 100));
    assertEquals(0, effectiveQuota(0, 0));
  }

  @Test
  void addingMessageOverDomainQuotaIsDenied() throws Exception {
    bypassCarbonioStorages(true);
    final Account account = accountInDomainWithQuota(0, 1);

    assertQuotaExceeded(() -> addMessage(account));
  }

  @Test
  void deletingMessageIsAllowedWhenOverQuota() throws Exception {
    final Account account = createAccount().create();
    final Message message = addMessage(account);
    setMailQuota(account, 1);
    bypassCarbonioStorages(true);

    final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
    assertDoesNotThrow(() -> mailbox.delete(null, message.getId(), Message.Type.MESSAGE));
  }

  @Test
  void sendingIsDeniedOverQuotaWhenReceiveButNotSendIsEnabled() throws Exception {
    final Account account = createAccount().create();
    addMessage(account);
    setMailQuota(account, 1);
    account.setMailAllowReceiveButNotSendWhenOverQuota(true);
    bypassCarbonioStorages(true);

    assertQuotaExceeded(() -> AccountUtil.checkQuotaWhenSendMail(mailboxOf(account)));
  }

  @Test
  void sendingIsAllowedOverQuotaWhenReceiveButNotSendIsDisabled() throws Exception {
    final Account account = createAccount().create();
    addMessage(account);
    setMailQuota(account, 1);
    account.setMailAllowReceiveButNotSendWhenOverQuota(false);
    bypassCarbonioStorages(true);

    assertDoesNotThrow(() -> AccountUtil.checkQuotaWhenSendMail(mailboxOf(account)));
  }

  @Test
  void registeredQuotaCheckIsUsedWhenBypassIsDisabled() throws Exception {
    final QuotaCheck registered = new QuotaCheckSingleton.DefaultQuotaCheck();
    QuotaCheckSingleton.setInstance(registered);

    bypassCarbonioStorages(true);
    assertEquals(ZimbraMailQuotaCheck.INSTANCE, QuotaCheckSingleton.getInstance());

    bypassCarbonioStorages(false);
    assertEquals(registered, QuotaCheckSingleton.getInstance());
  }

  private static void bypassCarbonioStorages(boolean value) {
    LC.bypass_carbonio_storages.setDefault(value);
  }

  private static void setMailQuota(Account account, long quota) throws Exception {
    Provisioning.getInstance()
        .modifyAttrs(account, Map.of(AccountUtil.A_ZIMBRA_MAIL_QUOTA, String.valueOf(quota)));
  }

  private static long effectiveQuota(long accountQuota, long domainQuota) throws Exception {
    return AccountUtil.getEffectiveQuota(accountInDomainWithQuota(accountQuota, domainQuota));
  }

  private static Account accountInDomainWithQuota(long accountQuota, long domainQuota)
      throws Exception {
    final String domainName = UUID.randomUUID() + ".com";
    Provisioning.getInstance()
        .createDomain(
            domainName,
            new HashMap<>(
                Map.of(AccountUtil.A_ZIMBRA_MAIL_DOMAIN_QUOTA, String.valueOf(domainQuota))));
    final Account account = createAccount().withDomain(domainName).create();
    setMailQuota(account, accountQuota);
    return account;
  }

  private static Mailbox mailboxOf(Account account) throws Exception {
    return MailboxManager.getInstance().getMailboxByAccount(account);
  }

  private static Message addMessage(Account account) throws Exception {
    return mailboxOf(account)
        .addMessage(
            null,
            MailboxTestUtil.generateMessage("test subject"),
            new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX),
            null);
  }

  private static void assertQuotaExceeded(org.junit.jupiter.api.function.Executable action) {
    final MailServiceException exception = assertThrows(MailServiceException.class, action);
    assertEquals(MailServiceException.QUOTA_EXCEEDED, exception.getCode());
  }
}
