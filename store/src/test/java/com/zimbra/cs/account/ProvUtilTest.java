// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.account;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;

import com.zextras.mailbox.soap.SoapExtension;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.ProvUtil.Category;
import com.zimbra.cs.account.ProvUtil.Console;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("api")
class ProvUtilTest {

  private static final int SOAP_PORT = 8080;

  @RegisterExtension
  static SoapExtension soapExtension = new SoapExtension.Builder()
      .addEngineHandler("com.zimbra.cs.service.admin.AdminService")
      .addEngineHandler("com.zimbra.cs.service.account.AccountService")
      .addEngineHandler("com.zimbra.cs.service.mail.MailService")
      .withBasePath("/service/admin/")
      .withPort(SOAP_PORT)
      .create();

  @BeforeAll
  static void setUp() {
    //noinspection HttpUrlsUsage
    LC.zimbra_admin_service_scheme.setDefault("http://");
    LC.zimbra_admin_service_port.setDefault(SOAP_PORT);
  }

  @SuppressWarnings("UnusedReturnValue")
  String createAccountForDomain(String domain) throws Exception {
    return createAccount(UUID.randomUUID() + "@" + domain);
  }

  String createAccount(String accountMail) throws Exception {
    return runCommand(new String[]{"ca", accountMail, "password"});
  }

  String runCommand(String[] commandWithArgs) throws Exception {
    OutputStream outputStream = new ByteArrayOutputStream();
    runCommand(outputStream, new ByteArrayOutputStream(), commandWithArgs);
    return outputStream.toString();
  }

  private void runCommand(OutputStream outputStream, OutputStream errorStream, String[] commandWithArgs)
      throws Exception {
    ProvUtil.main(new Console(outputStream, errorStream), commandWithArgs);
  }

  private void runCommand(PrintStream outputStream, PrintStream errorStream, String[] commandWithArgs)
      throws Exception {
    ProvUtil.main(new Console(outputStream, errorStream), commandWithArgs);
  }

  void assertIsUUID(String value) {
    //noinspection ResultOfMethodCallIgnored
    UUID.fromString(value);
  }

  //////////////////////////////// HELP

  @Test
  void testHelp() {
    try {
      final String result = runCommand(new String[]{"help"});
      System.out.println(result);
    } catch (Exception e) {
      Assertions.fail("Should not throw exception");
    }
  }

  @ParameterizedTest
  @EnumSource(ProvUtil.Category.class)
  void testHelpSubCommands(Category command) {
    try {
      final String result = runCommand(new String[]{"help", command.name().toLowerCase()});
      System.out.println(result);
    } catch (Exception e) {
      Assertions.fail("should not throw exception");
    }
  }

  //////////////////////////////// ACCOUNT

  @Test
  void addAccountAlias() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    runCommand(new String[]{"aaa", accountName, "alias@test.com"});
    final String result = runCommand(new String[]{"ga", "alias@test.com", "zimbraAliasTargetId"});

    Assertions.assertEquals("# name " + accountName + "\n\n", result);
  }

  @Test
  void checkPasswordStrengthForStrongPassword() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final String result = runCommand(new String[]{"cps", accountName, "password"});

    Assertions.assertEquals("Password passed strength check.\n", result);
  }

  @Test
  void checkPasswordStrengthForWeakPassword() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    OutputStream outputStream = new ByteArrayOutputStream();
    OutputStream errorStream = new ByteArrayOutputStream();
    catchSystemExit(
        () -> runCommand(outputStream, errorStream, new String[]{"cps", accountName, "new"}));

    final String expectedError = "ERROR: account.INVALID_PASSWORD (invalid password: too short)\n";
    Assertions.assertEquals(expectedError, errorStream.toString());
  }

  @Test
  void createAccount() throws Exception {
    final String result = runCommand(new String[]{"ca", "test@test.com", "password"});
    assertIsUUID(result.trim());
  }

  @Test
  void createAndGetDataSource() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final String createdDataSourceId = runCommand(
        new String[]{"cds", accountName, "imap", "dsname", "zimbraDataSourceEnabled", "TRUE",
            "zimbraDataSourceFolderId", "2"});
    final String result = runCommand(new String[]{"gds", accountName, "zimbraDataSourceId"});
    Assertions.assertTrue(result.contains(createdDataSourceId));
  }

  @Test
  void modifyAccount() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final String newDisplayName = "My name is Earl";
    final String modifyOperationResult = runCommand(
        new String[]{"ma", accountName, "displayName", newDisplayName});

    Assertions.assertTrue(modifyOperationResult.isEmpty());

    final String getOperationResult = runCommand(new String[]{"ga", accountName, "displayName"});
    final String expectedOutput = "# name " + accountName + "\n"
        + "displayName: " + newDisplayName;
    Assertions.assertEquals(expectedOutput, getOperationResult.trim());
  }

  @Test
  void deleteAccount() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final String deleteOperationResult = runCommand(new String[]{"da", accountName});
    Assertions.assertTrue(deleteOperationResult.isEmpty());

    OutputStream outputStream = new ByteArrayOutputStream();
    OutputStream errorStream = new ByteArrayOutputStream();
    catchSystemExit(() -> runCommand(outputStream, errorStream, new String[]{"ga", accountName}));
    final String expectedError =
        "ERROR: account.NO_SUCH_ACCOUNT (no such account: " + accountName + ")\n";
    Assertions.assertEquals(expectedError, errorStream.toString());
  }

  //////////////////////////////// DOMAIN

  @Test
  void createDomain() throws Exception {
    final String result = runCommand(new String[]{"cd", "testDomain.com"});
    assertIsUUID(result.trim());
  }

  @Test
  void deleteDomain() throws Exception {
    final String domain = "ohhere.com";
    runCommand(new String[]{"cd", domain});

    final String accountName1 = "putcardealer@" + domain;
    runCommand(new String[]{"ca", accountName1, "password"});

    OutputStream outputStream = new ByteArrayOutputStream();
    OutputStream errorStream = new ByteArrayOutputStream();
    catchSystemExit(() -> runCommand(outputStream, errorStream, new String[]{"dd", domain}));
    final String expectedError = "ERROR: account.DOMAIN_NOT_EMPTY (domain not empty: ohhere.com"
        + " (remaining entries: [uid=putcardealer,ou=people,dc=ohhere,dc=com] ...))\n";
    Assertions.assertEquals(expectedError, errorStream.toString());
  }

  @Test
  void countAccount() throws Exception {
    final String domain = UUID.randomUUID() + ".com";
    runCommand(new String[]{"cd", domain});

    for (int i = 0; i < 5; i++) {
      final String accountName1 = UUID.randomUUID().toString() + i + "@" + domain;
      runCommand(new String[]{"ca", accountName1, "password"});
    }

    final String ctaOutput = runCommand(new String[]{"cta", domain});
    final String expectedOutput =
        "cos name             cos id                                   # of accounts\n"
            + "-------------------- ---------------------------------------- --------------------\n"
            + "default              e00428a1-0c00-11d9-836a-000d93afea2a     5\n\n";

    Assertions.assertEquals(expectedOutput, ctaOutput);
  }

  //////////////////////////////// RIGHTS

  @Test
  void testCheckRightUsage() {
    try {
      catchSystemExit(() -> runCommand(new String[]{"ckr", "blah"}));
    } catch (Exception e) {
      Assertions.fail("Should have thrown exception");
    }
  }

  //////////////////////////////// SEARCH

  @Test
  void searchAccounts() throws Exception {
    final String domain = UUID.randomUUID() + ".com";
    runCommand(new String[]{"cd", domain});

    final String accountMail = UUID.randomUUID() + "@" + domain;
    final String accountUUID = createAccount(accountMail).trim();
    createAccountForDomain(domain);
    createAccountForDomain(domain);

    final OutputStream outputStream = new ByteArrayOutputStream();
    final String ldapQuery = "(zimbraId=" + accountUUID + ")";
    final PrintStream errorStream = System.err;
    runCommand(new PrintStream(outputStream), errorStream,
        new String[]{"searchAccounts", ldapQuery});
    Assertions.assertEquals(accountMail, outputStream.toString().trim());
  }

  //////////////////////////////// MAILBOX
  @Test
  void unlockMailbox_exists_when_there_is_no_lock() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final OutputStream stdOutputStream = new ByteArrayOutputStream();
    runCommand(new PrintStream(stdOutputStream), System.err,
        new String[]{"ulm", accountName});

    final String accountIdForAccountName = runCommand(new String[]{"ga", accountName, "zimbraId"}).split(
        "zimbraId: ")[1].trim();
    final String expected = "Warning: No lock known for account " + accountIdForAccountName + "\n";
    Assertions.assertEquals(expected, stdOutputStream.toString());
  }

  @Test
  void unlockMailbox_fails_when_target_hostname_is_passed_and_it_does_not_support_op() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final OutputStream stdErrOutputStream = new ByteArrayOutputStream();
    catchSystemExit(() -> runCommand(new PrintStream(System.out), stdErrOutputStream,
        new String[]{"ulm", accountName, "localhost"}));
    final String expected = "ERROR: service.FAILURE "
        + "(system failure: target server version does not support UnregisterMailboxMoveOutRequest.)"
        + " (cause: com.zimbra.common.soap.SoapFaultException unknown document: UnregisterMailboxMoveOutRequest)\n";

    Assertions.assertEquals(expected, stdErrOutputStream.toString());
  }

  @Test
  void unlockMailbox_fails_when_target_account_status_is_not_active() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});
    runCommand(new String[]{"ma", accountName, "zimbraAccountStatus", "locked"});

    final OutputStream stdErrOutputStream = new ByteArrayOutputStream();
    catchSystemExit(() -> runCommand(new PrintStream(System.out), stdErrOutputStream,
        new String[]{"ulm", accountName}));
    final String expected = "Cannot unlock mailbox for account " + accountName
        + ". Account status must be active. Current account status is locked."
        + " You must change the value of zimbraAccountStatus to 'active' first\n";

    Assertions.assertEquals(expected, stdErrOutputStream.toString());
  }

  @Test
  void getMailboxInfo() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final String result = runCommand(new String[]{"gmi", accountName});
    final String expected = "mailboxId: 1\nquotaUsed: 0\n";

    Assertions.assertEquals(expected, result);
  }

  @Test
  void getQuotaUsage() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final String accountName2 = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName2, "password"});

    final String result = runCommand(new String[]{"gqu", "localhost"});
    final String expected = String.format("%s 0 0%n%s 0 0%n", accountName, accountName2);

    Assertions.assertEquals(expected, result);
  }

  @Test
  void recalculateMailboxCounts_exists_when_mailbox_is_not_found() throws Exception {
    final String domain = "test.com";
    final UUID id = UUID.randomUUID();
    final String accountName = id + "@" + domain;
    runCommand(new String[]{"ca", accountName, "password", "zimbraId", id.toString()});

    final OutputStream stdError = new ByteArrayOutputStream();
    final OutputStream stdOut = new ByteArrayOutputStream();
    catchSystemExit(() -> runCommand(stdOut, stdError, new String[]{"rmc", accountName}));
    final String expected = "ERROR: mail.NO_SUCH_MBOX (no mailbox for account: " + id + ")\n";

    Assertions.assertEquals(expected, stdError.toString());
  }
}