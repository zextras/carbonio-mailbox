package com.zimbra.cs.account;

import com.zextras.mailbox.soap.SoapExtension;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.provutil.ProvUtilRequestsFile;
import com.zimbra.cs.account.provutil.TrackCommandRequestHandler;
import com.zimbra.cs.account.provutil.TrackCommandRequestService;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.GetAccountResponse;
import com.zimbra.soap.admin.type.AccountInfo;
import com.zimbra.soap.util.Jaxb2Xsds;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("api")
@Execution(ExecutionMode.SAME_THREAD)
public class ProvUtilRegressionTest {

  private static final Logger log = LogManager.getLogger(Jaxb2Xsds.class);
  
  private static final int SOAP_PORT = 8080;
  public static final String ACCOUNT_UUID = "186c1c23-d2ad-46b4-9efd-ddd890b1a4a2";
  public static final String ACCOUNT_NAME = "test@test.com";

  @RegisterExtension
  static SoapExtension soapExtension = new SoapExtension.Builder()
          .addEngineHandler(TrackCommandRequestService.class.getName())
          .withBasePath("/service/admin/")
          .withPort(SOAP_PORT)
          .create();

  ProvUtilCommandRunner commandRunner = new ProvUtilCommandRunner();
  ProvUtilRequestsFile requestsFile = new ProvUtilRequestsFile(Paths.get("src/test/resources/provutil/requests"));

  @BeforeAll
  static void setUp() throws ServiceException {
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

  private static Element jaxbToElement(Object resp) {
    try {
      return JaxbUtil.jaxbToElement(resp);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
  }

  private void run(String cmd) throws IOException {
    var args = Arrays.asList(cmd.split("\s+"));
    log.info(String.format("Executing '%s'", String.join(" ", args)));
    ProvUtilCommandRunner.CommandOutput out = null;
    try {
      out = commandRunner.runCommand(args);
    } catch (Exception e) {
      String commandLine = String.join(" ", args);
      fail(String.format("""
                      Error executing command '%s':
                      command       : %s
                      error         : %s""",
              cmd, commandLine, e.getMessage()));
    }
    var diffOutput = requestsFile.diffOrStore(args, out.requests());
    if (diffOutput.equals(ProvUtilRequestsFile.DiffResult.notOk)) {
      String commandLine = String.join(" ", args);
      fail(String.format("""
                      Command '%s' produced different requests sequence please diff expected and actual files:
                      command       : %s
                      expected file : %s
                      actual file   : %s""",
              args.get(0), commandLine,
              requestsFile.getFilePath(args),
              requestsFile.getActualFilePath(args)
      ));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "createIdentity test@test.com identityName",
          "createIdentity test@test.com identityName zimbraId 1 zimbraImapBindPort 1",
          "createIdentity test@test.com identityName zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "createSignature user@example.com signature-name",
          "createSignature user@example.com signature-name",
          "createSignature user@example.com signature-name zimbraSignatureId 1",
          "createSignature user@example.com signature-name zimbraSignatureId 1 zimbraPrefMailSignatureContactId 1",
          "deleteIdentity test@test.com identityName",
          "deleteIdentity test@test.com identityName",
          "deleteIdentity 8a64a712-cceb-4e03-b5ce-c131481bb455 identityName",
  })
  void provUtilTest(String cmd) throws IOException {
    TrackCommandRequestHandler.setCustomResponseMapping(Map.of(
            "GetAccountRequest", () -> {
              GetAccountResponse resp = new GetAccountResponse();
              resp.setAccount(new AccountInfo(ACCOUNT_UUID, ACCOUNT_NAME));
              return jaxbToElement(resp);
            }
    ));
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "addAccountAlias user@example.com alias@example.com",
          "addAccountAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com",
          "addAccountLogger user@example.com zimbra.lmtp info",
          "addAccountLogger 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbra.lmtp warn",
          "addAccountLogger -s localhost user@example.com zimbra.lmtp error",
          "addAccountLogger --server localhost user@example.com zimbra.soap trace",
          "addAccountLogger --server localhost 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbra.soap debug",
          "addDistributionListAlias list@example.com alias@example.com",
          "addDistributionListAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com",
          "addDistributionListMember list@example.com member@example.com member@example.com member@example.com",
          "addDistributionListMember list@example.com member@example.com",
          "addDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com member@example.com member@example.com member@example.com",
          "addDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com member@example.com member@example.com",
          "addDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com",
          "autoCompleteGal example.com someName",
          "autoProvControl start",
          "autoProvControl status",
          "autoProvControl stop",
          "changePrimaryEmail user@example.com newName@domain",
          "changePrimaryEmail 8a64a712-cceb-4e03-b5ce-c131481bb455 newName@domain",
          "checkPasswordStrength user@example.com password",
          "checkPasswordStrength 8a64a712-cceb-4e03-b5ce-c131481bb455 password",
          "checkRight account 22c3163a-ea39-4b65-a1c2-88447b30000f 75aca60e-8616-4165-a3ba-a6b96d529c97 viewFreeBusy",
          "checkRight account target@example.com 75aca60e-8616-4165-a3ba-a6b96d529c97 invite",
          "checkRight cos 22c3163a-ea39-4b65-a1c2-88447b30000f grantee.name@example.com getAccount",
          "checkRight cos target@example.com grantee.name@example.com viewFreeBusy",
          "checkRight domain 22c3163a-ea39-4b65-a1c2-88447b30000f grantee.name@example.com invite",
          "copyCos srcCosName cos",
          "copyCos 8a64a712-cceb-4e03-b5ce-c131481bb455 cos",
          "countAccount example.com",
          "countAccount 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "createAccount user@example.com password",
          "createAccount user@example.com password",
          "createAccount user@example.com password zimbraId 1 zimbraImapBindPort 1",
          "createAccount user@example.com password zimbraId 1 zimbraImapBindPort 1 zimbraMailHost 1",
          "createAliasDomain test.com local-example.com zimbraImapBindPort 1 zimbraMailHost \"value\"",
          "createAliasDomain example-alias.com local-example.com zimbraId 1 zimbraImapBindPort 1",
          "createAliasDomain example-alias.com 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "createAliasDomain example-alias.com local-example.com",
          "createAliasDomain example-alias.com local-example.com zimbraId 1 zimbraImapBindPort 1 zimbraMailHost \"value\"",
          "createBulkAccounts example.com namemask 1 password",
          "createBulkAccounts example.com namemask 4 password",
          "createCalendarResource user@example.com password",
          "createCalendarResource user@example.com password",
          "createCalendarResource user@example.com password zimbraId 1 zimbraImapBindPort 1",
          "createCos someName",
          "createCos someName zimbraId 1",
          "createCos someName zimbraId 1 zimbraImapBindPort 1",
          "createDataSource user@example.com pop3 databaseName zimbraDataSourceEnabled FALSE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237 zimbraId 1 zimbraImapBindPort 1",
          "createDataSource user@example.com contacts databaseName zimbraDataSourceEnabled TRUE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237 zimbraId 1 zimbraImapBindPort 1",
          "createDataSource user@example.com pop3 databaseName zimbraDataSourceEnabled FALSE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237",
          "createDataSource user@example.com contacts databaseName zimbraDataSourceEnabled TRUE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237",
          "createDataSource user@example.com pop3 databaseName zimbraDataSourceEnabled TRUE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "createDistributionList list@example.com",
          "createDistributionListsBulk test.com domain 3",
          "createDomain example.com",
          "createDomain example.com",
          "createDomain example.com zimbraId 1 zimbraImapBindPort 1",
          "createDomain example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "createDynamicDistributionList list@example.com",
          "createServer someName",
          "createServer someName",
          "createServer someName zimbraId 1 zimbraImapBindPort 1",
          "createServer someName zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "createXMPPComponent short example.com server.example.com org.example.MyClass category type",
          "createXMPPComponent short example.com server.example.com org.example.MyClass category type",
          "createXMPPComponent short example.com server.example.com org.example.MyClass category type zimbraId 1 zimbraImapBindPort 1",
          "createXMPPComponent short example.com server.example.com org.example.MyClass category type zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "deleteAccount test@test.com",
          "deleteAccount 186c1c23-d2ad-46b4-9efd-ddd890b1a4a2",
          "deleteCalendarResource user@example.com",
          "deleteCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteCos someName",
          "deleteCos 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteDataSource user@example.com 5bfd9bc4-d359-4a2c-8424-1101dffba0ee",
          "deleteDataSource 8a64a712-cceb-4e03-b5ce-c131481bb455 5bfd9bc4-d359-4a2c-8424-1101dffba0ee",
          "deleteDistributionList list@example.com true",
          "deleteDistributionList list@example.com",
          "deleteDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 false",
          "deleteDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 true",
          "deleteDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteDomain example.com",
          "deleteDomain 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteServer someName",
          "deleteServer 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteXMPPComponent xmppComponentName",
          "describe -ni group",
          "describe -v cos",
          "describe -a zimbraId",
          "describe -v server",
          "flushCache locale someName 8a64a712-cceb-4e03-b5ce-c131481bb455 someName",
          "flushCache all someName 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "flushCache group",
          "flushCache domain",
          "flushCache globalgrant someName 8a64a712-cceb-4e03-b5ce-c131481bb455 someName",
          "generateDomainPreAuth example.com 8a64a712-cceb-4e03-b5ce-c131481bb455 by 0 0",
          "generateDomainPreAuth 8a64a712-cceb-4e03-b5ce-c131481bb455 8a64a712-cceb-4e03-b5ce-c131481bb455 by 0 1729944652",
          "generateDomainPreAuth example.com 8a64a712-cceb-4e03-b5ce-c131481bb455 by 1732623357 0",
          "generateDomainPreAuth 8a64a712-cceb-4e03-b5ce-c131481bb455 8a64a712-cceb-4e03-b5ce-c131481bb455 by 1732623357 1729944652",
          "generateDomainPreAuth example.com someName by 0 0",
          "getAccount 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getAccount user@example.com attr attr",
          "getAccount -e 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccount -e user@example.com",
          "getAccount user@example.com attr attr attr",
          "getAccountLoggers 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccountLoggers user@example.com",
          "getAccountLoggers --server localhost 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccountLoggers --server localhost user@example.com",
          "getAccountLoggers -s localhost 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccountMembership user@example.com",
          "getAccountMembership 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAllAccountLoggers",
          "getAllAccountLoggers -s localhost",
          "getAllAccountLoggers --server localhost",
          "getAllCalendarResources -e -v -s localhost test.com",
          "getAllCalendarResources -s localhost",
          "getAllCalendarResources -v -e test.com",
          "getAllCalendarResources -v",
          "getAllCalendarResources -s localhost test.com",
          "getAllConfig",
          "getAllConfig attr attr",
          "getAllConfig attr attr attr",
          "getAllCos",
          "getAllCos -v",
          "getAllDistributionLists",
          "getAllDistributionLists test.com",
          "getAllDistributionLists -v",
          "getAllDistributionLists -v test.com",
          "getAllDomains -v -e attr attr attr",
          "getAllDomains attr attr",
          "getAllDomains -v -e",
          "getAllDomains -v",
          "getAllEffectiveRights usr expandSetAttrs expandGetAttrs",
          "getAllEffectiveRights egp 75aca60e-8616-4165-a3ba-a6b96d529c97 expandSetAttrs",
          "getAllEffectiveRights all expandGetAttrs",
          "getAllEffectiveRights gst 75aca60e-8616-4165-a3ba-a6b96d529c97",
          "getAllEffectiveRights key 75aca60e-8616-4165-a3ba-a6b96d529c97 expandSetAttrs expandGetAttrs",
          "getAllFbp",
          "getAllFbp -v",
          "getAllMemcachedServers",
          "getAllMtaAuthURLs",
          "getAllReverseProxyBackends",
          "getAllReverseProxyURLs",
          "getAllRights -t account -c USER",
          "getAllRights -v -c ADMIN",
          "getAllRights -v -t domain -c ALL",
          "getAllRights -t cos",
          "getAllRights -v -c USER",
          "getAllServers -v -e spell",
          "getAllServers antispam",
          "getAllServers -v -e mta",
          "getAllServers -v",
          "getAllServers spell",
          "getAllXMPPComponents",
          "getAuthTokenInfo 0_2b6c930a7ca1a02daad5f27528d6c9986317204e_69643d33363a62333134613231652d666137392d346533352d613765352d6437666637303834333866363b6578703d31333a313733323535383437303239303b76763d323a31363b747970653d363a7a696d6272613b753d313a613b7469643d31303a313131353331313832383b",
          "getCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getCalendarResource user@example.com attr attr",
          "getCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getCalendarResource user@example.com",
          "getCalendarResource user@example.com attr attr attr",
          "getConfig someName",
          "getCos 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getCos someName attr attr",
          "getCos 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getCos someName",
          "getCos someName attr attr attr",
          "getDataSources 8a64a712-cceb-4e03-b5ce-c131481bb455 argument1 argument2 argument1",
          "getDataSources user@example.com argument1 argument2",
          "getDataSources 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getDataSources user@example.com",
          "getDataSources user@example.com argument1 argument2 argument1",
          "getDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getDistributionList list@example.com attr attr",
          "getDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getDistributionList list@example.com",
          "getDistributionList list@example.com attr attr attr",
          "getDistributionListMembership user@example.com",
          "getDistributionListMembership 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getDomain 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getDomain example.com attr attr",
          "getDomain -e 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getDomain -e example.com",
          "getDomain example.com attr attr attr",
          "getDomainInfo name 42 attr attr attr",
          "getDomainInfo virtualHostname 42 attr attr",
          "getDomainInfo virtualHostname host.example.org 42",
          "getDomainInfo id 42",
          "getDomainInfo id 8a64a712-cceb-4e03-b5ce-c131481bb455 value attr attr attr",
          "getEffectiveRights account grantee.name@example.com expandSetAttrs expandGetAttrs",
          "getEffectiveRights account target@example.com 75aca60e-8616-4165-a3ba-a6b96d529c97 expandSetAttrs",
          "getEffectiveRights account target@example.com grantee.name@example.com expandGetAttrs",
          "getEffectiveRights cos 22c3163a-ea39-4b65-a1c2-88447b30000f 75aca60e-8616-4165-a3ba-a6b96d529c97",
          "getEffectiveRights cos target@example.com 75aca60e-8616-4165-a3ba-a6b96d529c97 expandSetAttrs expandGetAttrs",
          "getFreebusyQueueInfo",
          "getFreebusyQueueInfo provider-name",
          "getGrants -g grp 75aca60e-8616-4165-a3ba-a6b96d529c97",
          "getGrants -g egp grantee.name@example.com 1",
          "getGrants -g dom grantee.name@example.com 0",
          "getGrants -g key grantee.name@example.com",
          "getGrants -g email 75aca60e-8616-4165-a3ba-a6b96d529c97 1",
          "getMemcachedClientConfig localhost",
          "getMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105",
          "getMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105",
          "getMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105",
          "getQuotaUsage localhost",
          "getRight invite -e",
          "getRight invite",
          "getRight getAccount -e",
          "getRight getAccount",
          "getRight viewFreeBusy -e",
          "getRightsDoc",
          "getRightsDoc org.example.mypackage",
          "getServer 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getServer someName attr attr",
          "getServer -e 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getServer -e someName",
          "getServer someName attr attr attr",
          "getShareInfo ownerName",
          "getShareInfo ed56a2de-1418-4ff6-a790-988c19c6004d",
          "getSpnegoDomain",
          "getXMPPComponent 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getXMPPComponent someName attr attr",
          "getXMPPComponent 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getXMPPComponent someName",
          "getXMPPComponent someName attr attr attr",
          "grantRight account 75aca60e-8616-4165-a3ba-a6b96d529c97 usr test@test.com secret",
          "grantRight group 75aca60e-8616-4165-a3ba-a6b96d529c97 grp 8a64a712-cceb-4e03-b5ce-c131481bb455 secret",
          "grantRight config usr test@test.com grp modifyAccount",
          "help commands",
          "modifyAccount test@test.com zimbraImapBindPort 1",
          "modifyAccount user@example.com zimbraId 1 zimbraImapBindPort 1",
          "modifyAccount user@example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyCalendarResource user@example.com zimbraId 1 zimbraImapBindPort 1",
          "modifyCalendarResource user@example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyConfig zimbraId 1 zimbraImapBindPort 1",
          "modifyConfig zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyCos 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyCos someName zimbraId 1 zimbraImapBindPort 1",
          "modifyCos someName zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDataSource user@example.com 5bfd9bc4-d359-4a2c-8424-1101dffba0ee zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDataSource 8a64a712-cceb-4e03-b5ce-c131481bb455 5bfd9bc4-d359-4a2c-8424-1101dffba0ee zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDistributionList list@example.com zimbraId 1 zimbraImapBindPort 1",
          "modifyDistributionList list@example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDomain 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDomain example.com zimbraId 1 zimbraImapBindPort 1",
          "modifyDomain example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyServer 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyServer someName zimbraId 1 zimbraImapBindPort 1",
          "modifyServer someName zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "purgeAccountCalendarCache 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593 user@example.com",
          "purgeAccountCalendarCache 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "purgeAccountCalendarCache user@example.com 301c1dab-c07d-478c-b5db-eaffcc64b593 user@example.com 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "purgeAccountCalendarCache user@example.com 301c1dab-c07d-478c-b5db-eaffcc64b593 user@example.com",
          "purgeAccountCalendarCache user@example.com",
          "purgeFreebusyQueue",
          "purgeFreebusyQueue provider-name",
          "pushFreebusy test@test.com",
          "pushFreebusy 186c1c23-d2ad-46b4-9efd-ddd890b1a4a2d",
          "pushFreebusy 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "pushFreebusy 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "recalculateMailboxCounts user@example.com",
          "recalculateMailboxCounts 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "reloadMemcachedClientConfig all",
          "reloadMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105",
          "reloadMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105",
          "reloadMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105",
          "removeAccountAlias user@example.com alias@example.com",
          "removeAccountAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com",
          "removeAccountLogger 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbra.soap",
          "removeAccountLogger --server localhost",
          "removeAccountLogger zimbra.lmtp",
          "removeAccountLogger -s localhost user@example.com zimbra.soap",
          "removeAccountLogger --server localhost 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "removeDistributionListAlias list@example.com alias@example.com",
          "removeDistributionListAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com",
          "removeDistributionListMember list@example.com member@example.com",
          "removeDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com",
          "renameAccount user@example.com newName@domain",
          "renameAccount 8a64a712-cceb-4e03-b5ce-c131481bb455 newName@domain",
          "renameCalendarResource user@example.com newName@domain",
          "renameCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455 newName@domain",
          "renameCos someName newName",
          "renameCos 8a64a712-cceb-4e03-b5ce-c131481bb455 newName",
          "renameDistributionList list@example.com newName@domain",
          "renameDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 newName@domain",
          "resetAllLoggers",
          "resetAllLoggers -s localhost",
          "resetAllLoggers --server localhost",
          "revokeRight account user@example.com usr grantee.name@example.com listAccount",
          "revokeRight domain test.com usr grantee.name@example.com invite",
          "revokeRight cos cosName grp 75aca60e-8616-4165-a3ba-a6b96d529c97 viewFreeBusy",
          "revokeRight config usr 75aca60e-8616-4165-a3ba-a6b96d529c97 viewFreeBusy",
          "revokeRight global usr 75aca60e-8616-4165-a3ba-a6b96d529c97 viewFreeBusy",
          "searchGal example.com someName 0 42 sortBy attr",
          "searchGal example.com someName 42 42",
          "searchGal example.com someName 1 1 1 0 sortBy attr",
          "searchGal example.com someName 1 10",
          "searchGal example.com someName 1 10 1 1 sortBy attr",
          "setAccountCos user@example.com cos-name",
          "setAccountCos user@example.com 1829acc8-2fd3-45cf-aac5-f3b3078daaa8",
          "setAccountCos 8a64a712-cceb-4e03-b5ce-c131481bb455 cos-name",
          "setAccountCos 8a64a712-cceb-4e03-b5ce-c131481bb455 1829acc8-2fd3-45cf-aac5-f3b3078daaa8",
          "setPassword user@example.com password",
          "setPassword 8a64a712-cceb-4e03-b5ce-c131481bb455 password",
          "syncGal example.com",
          "syncGal example.com 1732621757",
          "unlockMailbox user@example.com",
          "unlockMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "verifyIndex user@example.com",
          "verifyIndex 8a64a712-cceb-4e03-b5ce-c131481bb455"
  })
  void provUtilTestAll(String cmd) throws IOException {
    run(cmd);
  }


  @Disabled
  @ParameterizedTest
  @ValueSource(strings = {
          "compactIndexMailbox user@example.com start",
          "compactIndexMailbox user@example.com status",
          "compactIndexMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 start",
          "compactIndexMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 status",
          "countObjects alias@example.com -d example.com",
          "countObjects 1829acc8-2fd3-45cf-aac5-f3b3078daaa8",
          "countObjects internal.user@example.com -d 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "countObjects 301c1dab-c07d-478c-b5db-eaffcc64b593 -d example.com",
          "countObjects cos",
          "deleteSignature user@example.com signature-name",
          "deleteSignature 8a64a712-cceb-4e03-b5ce-c131481bb455 signature-name",
          "exit",
          "generateDomainPreAuthKey example.com",
          "generateDomainPreAuthKey 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "generateDomainPreAuthKey -f example.com",
          "generateDomainPreAuthKey -f 8a64a712-cceb-4e03-b5ce-c131481bb455",
//        requires ldap: begin
          "getAllAccounts -e -s localhost example.com",
          "getAllAccounts -s localhost",
          "getAllAccounts -v -e example.com",
          "getAllAccounts -v",
          "getAllAccounts -s localhost example.com",
          "getAllAdminAccounts -e attr attr attr",
          "getAllAdminAccounts attr attr",
          "getAllAdminAccounts -v -e",
          "getAllAdminAccounts -v",
          "getAllAdminAccounts attr attr attr",
//        requires ldap: end
          "getAllReverseProxyDomains",
          "getCreateObjectAttrs account example.com cos-name test@test.com",
          "getCreateObjectAttrs domain 36f01e27-88de-4495-bb4b-9b05443aa8f7 cos-name 75aca60e-8616-4165-a3ba-a6b96d529c97",
          "getCreateObjectAttrs account example.com 1829acc8-2fd3-45cf-aac5-f3b3078daaa8 grantee.name@example.com",
          "getCreateObjectAttrs domain 36f01e27-88de-4495-bb4b-9b05443aa8f7 1829acc8-2fd3-45cf-aac5-f3b3078daaa8 75aca60e-8616-4165-a3ba-a6b96d529c97",
          "getCreateObjectAttrs account 36f01e27-88de-4495-bb4b-9b05443aa8f7 cos-name grantee.name@example.com",
          "getIdentities 8a64a712-cceb-4e03-b5ce-c131481bb455 argument1 argument2 argument1",
          "getIdentities test@test.com argument1 argument2",
          "getIdentities 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getIdentities user@example.com",
          "getIdentities user@example.com argument1 argument2 argument1",
          "getIndexStats user@example.com",
          "getIndexStats 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getMailboxInfo 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "getSignatures 8a64a712-cceb-4e03-b5ce-c131481bb455 argument1 argument2 argument1",
          "getSignatures user@example.com argument1 argument2",
          "getSignatures 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getSignatures user@example.com",
          "getSignatures user@example.com argument1 argument2 argument1",
          "modifyIdentity 8a64a712-cceb-4e03-b5ce-c131481bb455 identityName zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyIdentity user@example.com identityName zimbraId 1 zimbraImapBindPort 1",
          "modifyIdentity 8a64a712-cceb-4e03-b5ce-c131481bb455 identityName",
          "modifyIdentity user@example.com identityName",
          "modifyIdentity user@example.com identityName zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifySignature user@example.com 55da11a3-154f-4271-880d-642423563dde zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifySignature user@example.com signature-name zimbraId 1 zimbraImapBindPort 1",
          "modifySignature 8a64a712-cceb-4e03-b5ce-c131481bb455 55da11a3-154f-4271-880d-642423563dde",
          "modifySignature 8a64a712-cceb-4e03-b5ce-c131481bb455 signature-name",
          "modifySignature user@example.com signature-name zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyXMPPComponent test.com zimbraId 1 zimbraImapBindPort 1",
          "modifyXMPPComponent test.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "pushFreebusyDomain example.com",
          "reIndexMailbox user@example.com status 8a64a712-cceb-4e03-b5ce-c131481bb455 type 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "reIndexMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 status 8a64a712-cceb-4e03-b5ce-c131481bb455 type",
          "reIndexMailbox user@example.com status",
          "reIndexMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 status",
          "reIndexMailbox user@example.com start 8a64a712-cceb-4e03-b5ce-c131481bb455 type 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "renameDomain -l test.com new.example.com",
          "renameDomain 8a64a712-cceb-4e03-b5ce-c131481bb455 new.example.com",
          "searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) sortAscending true example.com example.com",
          "searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) sortBy attr sortAscending false",
          "searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) 0 0 sortAscending false example.com example.com",
          "searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) 0 1",
          "searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) 0 1 sortBy attr example.com example.com",
//        requires ldap begin
          "searchCalendarResources example.com attr op 42 8a64a712-cceb-4e03-b5ce-c131481bb455 type 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "searchCalendarResources -v example.com attr op 42 8a64a712-cceb-4e03-b5ce-c131481bb455 type",
          "searchCalendarResources example.com attr op 42",
          "searchCalendarResources -v example.com attr op 42",
          "searchCalendarResources example.com attr op value 8a64a712-cceb-4e03-b5ce-c131481bb455 type 8a64a712-cceb-4e03-b5ce-c131481bb455",
//        requires ldap end
          "unlockMailbox user@example.com localhost",
          "unlockMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 localhost",
  })
  void provUtilFailingTests(String cmd) throws IOException {
    run(cmd);
  }

}
