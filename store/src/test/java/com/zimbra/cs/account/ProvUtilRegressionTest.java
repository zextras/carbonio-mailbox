package com.zimbra.cs.account;

import com.zextras.mailbox.soap.SoapExtension;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.provutil.SoapCallTrackingService;
import com.zimbra.cs.account.provutil.TrackCommandRequestHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

@Tag("api")
@Execution(ExecutionMode.SAME_THREAD)
public class ProvUtilRegressionTest {

  private static final int SOAP_PORT = 8080;

  @RegisterExtension
  static SoapExtension soapExtension = new SoapExtension.Builder()
          .addEngineHandler(SoapCallTrackingService.class.getName())
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

  String runCommand(String... commandWithArgs) throws Exception {
    OutputStream outputStream = new ByteArrayOutputStream();
    runCommand(outputStream, new ByteArrayOutputStream(), commandWithArgs);
    return outputStream.toString();
  }

  private void runCommand(OutputStream outputStream, OutputStream errorStream, String... commandWithArgs)
          throws Exception {
    TrackCommandRequestHandler.setCommand(commandWithArgs);
    ProvUtil.main(new ProvUtil.Console(outputStream, errorStream), commandWithArgs);
    TrackCommandRequestHandler.reset();
  }

  @Test
  void createAccount() throws Exception {
    final String result = runCommand(new String[]{"ca", "test@test.com", "password"});

  }

  @Test
  void test1() throws Exception {
    runCommand("cxc", "blah", "0d853865-4491-4a6d-8627-d99b9673d7de", "server", "classname", "ok", "type");
  }

}
