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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

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

  @AfterAll
  static void shutDown() throws Exception {
    soapExtension.initData();
  }

  @BeforeEach
  void setUpBefore() throws Exception {
    soapExtension.initData();
  }

  @AfterEach
  void clear() throws Exception {
    soapExtension.clearData();
  }

  static Map<String, String> parseZmprovKeyValue(String content) {
    var lines = content.split("\n");
    Map<String, String> res = new HashMap<>();
    for (var line: lines) {
      if (!line.isEmpty() && !line.startsWith("\\s#")) {
          var kv = line.split(": ");
          var v = kv.length == 1 ? "" : kv[1];
          res.put(kv[0], v);
      }
    }
    return res;
  }

  @SuppressWarnings("UnusedReturnValue")
  String createAccountForDomain(String domain) throws Exception {
    return createAccount(UUID.randomUUID() + "@" + domain);
  }

  String createAccount(String accountMail) throws Exception {
    return runCommand(new String[]{"ca", accountMail, "password"});
  }

  String runCommand(String... commandWithArgs) throws Exception {
    OutputStream outputStream = new ByteArrayOutputStream();
    runCommand(outputStream, new ByteArrayOutputStream(), commandWithArgs);
    return outputStream.toString();
  }

  private void runCommand(OutputStream outputStream, OutputStream errorStream, String... commandWithArgs)
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
      runCommand(new String[]{"help"});
    } catch (Exception e) {
      Assertions.fail("Should not throw exception");
    }
  }

  @ParameterizedTest
  @EnumSource(ProvUtil.Category.class)
  void testHelpSubCommands(Category command) {
    try {
      runCommand(new String[]{"help", command.name().toLowerCase()});
    } catch (Exception e) {
      Assertions.fail("should not throw exception");
    }
  }

  //////////////////////////////// ACCOUNT

  @Test
  void createAccount() throws Exception {
    final String result = runCommand(new String[]{"ca", "test@test.com", "password"});
    assertIsUUID(result.trim());
  }

  @Test
  void createAccount_and_getAccount_via_LDAP() throws Exception {
    final String domain = "test.com";
    final UUID id = UUID.randomUUID();
    final String accountName = id + "@" + domain;
    runCommand(new String[]{"-l", "ca", accountName, "password", "zimbraId", id.toString()});

    final String output = runCommand(new String[]{"-l", "ga", accountName});

    Assertions.assertTrue(output.contains("zimbraId: " + id));
    Assertions.assertTrue(output.contains("zimbraMailDeliveryAddress: " + accountName));
  }

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

  //////////////////////////////// MAILBOX
  @Test
  void unlockMailbox_exists_when_there_is_no_lock() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final OutputStream stdOutputStream = new ByteArrayOutputStream();
    runCommand(new PrintStream(stdOutputStream), new ByteArrayOutputStream(),
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
    catchSystemExit(() -> runCommand(new ByteArrayOutputStream(), stdErrOutputStream,
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
    catchSystemExit(() -> runCommand(new ByteArrayOutputStream(), stdErrOutputStream,
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

    final String result = runCommand(new String[]{"gqu", "localhost"});
    final String expected = String.format("%s 0 0%n", accountName);

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

  //////////////////////////////// CALENDAR

  @Test
  void createCalendarResources() throws Exception {
    final String calResourceId = "calRes@test.com";
    final String output = runCommand(
        new String[]{"-l", "ccr", calResourceId, "password", "name", "testCalResource", "zimbraCalResContactName",
            "TestResName"});
    assertIsUUID(output.trim());
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
    runCommand(outputStream, new ByteArrayOutputStream(),
        new String[]{"searchAccounts", ldapQuery});
    Assertions.assertEquals(accountMail, outputStream.toString().trim());
  }

  @Test
  void searchCalendarResources() throws Exception {
    final String calResourceId = "calRes@test.com";
    runCommand(
        new String[]{"-l", "ccr", calResourceId, "password", "name", "testCalResource", "zimbraCalResContactName",
            "TestResName"});

    final String searchCalResOutput = runCommand(
        new String[]{"-l", "scr", "test.com", "zimbraCalResContactName", "has", "TestResName"});

    Assertions.assertEquals(calResourceId.toLowerCase() + "\n", searchCalResOutput);
  }


  @Test
  void searchGal_fail_when_domain_not_found() {
    try {
      catchSystemExit(() -> runCommand(new String[]{"sg", "unknown.domain", "search_term"}));
    } catch (Exception e) {
      Assertions.fail("Should not throw exception, the exit is already caught.");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "--ldap"})
  void searchGal_returns_no_results_when_gal_is_not_configured(String ldapOption) throws Exception {
    final String domain = "testDomain.com";
    runCommand(new String[]{"cd", domain});
    String[] command = (ldapOption.isEmpty()) ?
        new String[]{"sg", domain, "search_term"} :
        new String[]{ldapOption, "sg", domain, "search_term"};
    final String output = runCommand(command);

    Assertions.assertEquals("", output,
        "searchGal need more setup in order to work in test environments so don't expect any results");
  }

  @Test
  void searchGal_fails_when_using_ldap_backend_and_unsupported_arguments_passed() throws Exception {
    final String domain = "testDomain.com";
    runCommand(new String[]{"cd", domain});

    OutputStream stdErr = new ByteArrayOutputStream();
    catchSystemExit(
        () -> runCommand(new ByteArrayOutputStream(), stdErr,
            new String[]{"--ldap", "sg", domain, "search_term", "offset", "10"}));

    String expected = "ERROR: service.INVALID_REQUEST (invalid request: offset is not supported with -l)\n";
    Assertions.assertEquals(expected, stdErr.toString());
  }

  @Test
  void searchGal_fails_when_using_ldap_backend_and_unsupported_arguments_passed2() throws Exception {
    final String domain = "testDomain.com";
    runCommand(new String[]{"cd", domain});

    OutputStream stdErr = new ByteArrayOutputStream();
    catchSystemExit(
        () -> runCommand(new ByteArrayOutputStream(), stdErr,
            new String[]{"--ldap", "sg", domain, "search_term", "sortBy", "fullName"}));

    String expected = "ERROR: service.INVALID_REQUEST (invalid request: sortBy is not supported with -l)\n";
    Assertions.assertEquals(expected, stdErr.toString());
  }

  @Test void createBulkAccounts() throws Exception {
    var stdErr = new ByteArrayOutputStream();
    var outputStream = new ByteArrayOutputStream();
    runCommand("cd", "demo.zextras.io");
    String password = "passwd";
    runCommand(outputStream, stdErr, "createBulkAccounts", "demo.zextras.io", "ntestuser", "4", password);
    var ids = new String(outputStream.toByteArray()).split("\n");
    for (var id : ids) {
      var out = runCommand("-l", "ga", id);
      Assertions.assertEquals(password, parseZmprovKeyValue(out).get("userPassword"));
    }
  }

  @Test void whenNumberOfArgumentsAreExcessiveAnErrorIsDisplayed() throws Exception {
    OutputStream stdErr = new ByteArrayOutputStream();
    catchSystemExit(
            () -> runCommand(new ByteArrayOutputStream(), stdErr,
                    "createBulkAccounts", "demo.zextras.io", "ntestuser", "4", "passwd", "other"));
    String expected = "createBulkAccounts is expecting 4 arguments but 5 arguments have been provided\n";
    Assertions.assertEquals(expected, stdErr.toString());
  }

  @Test void whenNumberOfArgumentsAreLackingAnErrorIsDisplayed() throws Exception {
    OutputStream stdErr = new ByteArrayOutputStream();
    catchSystemExit(
            () -> runCommand(new ByteArrayOutputStream(), stdErr, "createBulkAccounts", "demo.zextras.io"));
    String expected = "createBulkAccounts is expecting 4 arguments but 1 argument has been provided\n";
    Assertions.assertEquals(expected, stdErr.toString());
  }

  @Test void variadicCommandArgumentsNumberMismatchMaxInt() throws Exception {
    OutputStream stdErr = new ByteArrayOutputStream();
    catchSystemExit(
            () -> runCommand(new ByteArrayOutputStream(), stdErr, "createDynamicDistributionList"));
    String expected = "createDynamicDistributionList is expecting at least 1 arguments but 0 arguments have been provided\n";
    Assertions.assertEquals(expected, stdErr.toString());
  }

  @Test void variadicCommandArgumentsNumberMismatchZero() throws Exception {
    OutputStream stdErr = new ByteArrayOutputStream();
    catchSystemExit(
            () -> runCommand(new ByteArrayOutputStream(), stdErr, "getAllCos", "p1", "p2", "p3"));
    String expected = "getAllCos is expecting at most 1 arguments but 3 arguments have been provided\n";
    Assertions.assertEquals(expected, stdErr.toString());
  }

  @Test void getAllAccounts() throws Exception {
    runCommand("ca", "user1@test.com", "password");
    runCommand("ca", "user2@test.com", "password");
    runCommand("ca", "user3@test.com", "password");

    var getAllAccountOutput = runCommand("-l", "getAllAccounts");
    String expectedOutput = """
            user1@test.com
            user2@test.com
            user3@test.com
            """;
    Assertions.assertEquals(expectedOutput, getAllAccountOutput);
  }

  @Test void getAllAccountsVerbose() throws Exception {
    var usr1 = runCommand("ca", "user1@test.com", "password", "zimbraMailHost", "localhost");
    var usr2 = runCommand("ca", "user2@test.com", "password", "zimbraMailHost", "localhost");
    var usr3 = runCommand("ca", "user3@test.com", "password", "zimbraMailHost", "localhost");

    var getAllAccountOutput = runCommand("--ldap", "getAllAccounts", "-v", "test.com", "-s", "localhost");

    Assertions.assertTrue(getAllAccountOutput.contains(String.format("zimbraId: %s", usr1)));
    Assertions.assertTrue(getAllAccountOutput.contains(String.format("zimbraId: %s", usr2)));
    Assertions.assertTrue(getAllAccountOutput.contains(String.format("zimbraId: %s", usr3)));
  }

  @Test void getAllAdminAccounts() throws Exception {
    runCommand("ca", "user1@test.com", "password", "zimbraIsAdminAccount", "TRUE");
    runCommand("ca", "user2@test.com", "password", "zimbraIsDelegatedAdminAccount", "TRUE");
    runCommand("ca", "user3@test.com", "password");

    var output = runCommand("-l", "getAllAdminAccounts");
    String expectedOutput = """
            user1@test.com
            user2@test.com
            """;
    Assertions.assertEquals(expectedOutput, output);
  }

  @ParameterizedTest
  @ValueSource(strings = {"test1.com", "dev.io"})
  void renameDomain(String targetDomain) throws Exception {
    var output = runCommand("-l", "renameDomain", "test.com", targetDomain);
    Assertions.assertTrue(output.contains(String.format("domain test.com renamed to %s", targetDomain)));
  }

  @Test void renameInvalidDomain() throws Exception {
    catchSystemExit( () ->
            runCommand("-l", "renameDomain", "test1.com", "test2.com") );
  }

}