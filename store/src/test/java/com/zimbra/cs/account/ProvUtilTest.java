// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.account;

import com.zextras.mailbox.soap.SoapExtension;
import com.zimbra.common.localconfig.LC;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

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
    LC.zimbra_admin_service_scheme.setDefault("http://");
    LC.zimbra_admin_service_port.setDefault(SOAP_PORT);
  }

  @Test
  void createAccount() throws Exception {
    final String result = runCommand(new String[]{"ca", "test@test.com", "password"});
    assertIsUUID(result.trim());
  }

  @Test
  void createDomain() throws Exception {
    final String result = runCommand(new String[]{"cd", "testDomain.com"});
    assertIsUUID(result.trim());
  }

  @Test
  void modifyAccount() throws Exception {
    final String accountName = UUID.randomUUID() + "@test.com";
    runCommand(new String[]{"ca", accountName, "password"});

    final String newDisplayName = "My name is Earl";
    final String modifyOperationResult = runCommand(new String[]{"ma", accountName, "displayName", newDisplayName});

    Assertions.assertTrue(modifyOperationResult.isEmpty());

    final String getOperationResult = runCommand(new String[]{"ga", accountName, "displayName"});
    final String expectedOutput = "# name " + accountName + "\n"
                                + "displayName: " + newDisplayName;
    Assertions.assertEquals(expectedOutput, getOperationResult.trim());
  }

  @Test
  void testHelp() throws Exception {
    final String result = runCommand(new String[]{"help"});
    System.out.println(result);
  }

  private String runCommand(String[] commandWithArgs) throws Exception {
    OutputStream outputStream = new ByteArrayOutputStream();
    ProvUtil.mainWithSystemOut(new PrintStream(outputStream),  commandWithArgs);
    return outputStream.toString();
  }

  void assertIsUUID(String value) {
    UUID.fromString(value);
  }

}