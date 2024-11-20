package com.zimbra.cs.account;

import com.zextras.mailbox.soap.SoapExtension;
import com.zimbra.common.localconfig.LC;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ProvUtilRegressionTest {

  private static final int SOAP_PORT = 8080;

  @RegisterExtension
  static SoapExtension soapExtension = new SoapExtension.Builder()
          .addEngineHandler("com.zimbra.cs.account.provutil.TrackingAdminService")
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

  String runCommand(String[] commandWithArgs) throws Exception {
    OutputStream outputStream = new ByteArrayOutputStream();
    runCommand(outputStream, new ByteArrayOutputStream(), commandWithArgs);
    return outputStream.toString();
  }

  private void runCommand(OutputStream outputStream, OutputStream errorStream, String[] commandWithArgs)
          throws Exception {
    ProvUtil.main(new ProvUtil.Console(outputStream, errorStream), commandWithArgs);
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

}
