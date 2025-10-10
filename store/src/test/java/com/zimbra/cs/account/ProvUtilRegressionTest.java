package com.zimbra.cs.account;

import static org.junit.jupiter.api.Assertions.*;

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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("api")
@Disabled
public class ProvUtilRegressionTest {

  private static final Logger log = LogManager.getLogger(ProvUtilRegressionTest.class);

  public static final String DEFAULT_DOMAIN = "test.com";
  public static final String DEFAULT_DOMAIN_ID = "f4806430-b434-4e93-9357-a02d9dd796b8";
  private static final String SERVER_NAME = "localhost";

  public static final String ACCOUNT_UUID = "186c1c23-d2ad-46b4-9efd-ddd890b1a4a2";
  public static final String ACCOUNT_NAME = "test@test.com";

  @RegisterExtension
  static SoapExtension soapExtension = new SoapExtension.Builder()
          .addEngineHandler(TrackCommandRequestService.class.getName())
          .withBasePath("/service/admin/")
          .create();

  ProvUtilRequestsFile requestsFile = new ProvUtilRequestsFile(Paths.get("src/test/resources/provutil/requests"));

  @BeforeAll
  static void setUp() {
    //noinspection HttpUrlsUsage
    LC.zimbra_admin_service_scheme.setDefault("http://");
    LC.zimbra_admin_service_port.setDefault(soapExtension.getPort());
  }

  @AfterAll
  static void shutDown() throws Exception {
    soapExtension.initData();
  }

  @BeforeEach
  void setUpBefore() throws Exception {
    soapExtension.initData();
    Provisioning provisioning = Provisioning.getInstance();
    provisioning.createAccount("adminAccount@test.com", "password", new HashMap<>(Map.of(
        Provisioning.A_zimbraMailHost, SERVER_NAME,
        Provisioning.A_zimbraIsAdminAccount, "TRUE"
    )));
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

  private void run(String commandLine) throws IOException {
    String[] argsArray = commandLine.split(" +");
    log.info("Executing '{}'", commandLine);
    ProvUtilCommandRunner.CommandOutput out = null;
    try {
      out = ProvUtilCommandRunner.runCommand(argsArray);
    } catch (Exception e) {
      fail(String.format("""
                      Error executing command':
                      command       : %s
                      error         : %s""",
              commandLine, e.getMessage()));
    }
    var args = Arrays.asList(argsArray);
    var diffOutput = requestsFile.diffOrStore(args, out.requests());
    if (diffOutput.equals(ProvUtilRequestsFile.DiffResult.notOk)) {
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
          "checkRight account 22c3163a-ea39-4b65-a1c2-88447b30000f 75aca60e-8616-4165-a3ba-a6b96d529c97 viewFreeBusy",
          "checkRight account target@example.com 75aca60e-8616-4165-a3ba-a6b96d529c97 invite",
          "checkRight cos 22c3163a-ea39-4b65-a1c2-88447b30000f grantee.name@example.com getAccount",
          "checkRight cos target@example.com grantee.name@example.com viewFreeBusy",
          "checkRight domain 22c3163a-ea39-4b65-a1c2-88447b30000f grantee.name@example.com invite",
          "getAllEffectiveRights all expandGetAttrs",
          "getAllEffectiveRights egp 75aca60e-8616-4165-a3ba-a6b96d529c97 expandSetAttrs",
          "getAllEffectiveRights gst 75aca60e-8616-4165-a3ba-a6b96d529c97",
          "getAllEffectiveRights key 75aca60e-8616-4165-a3ba-a6b96d529c97 expandSetAttrs expandGetAttrs",
          "getAllEffectiveRights usr expandSetAttrs expandGetAttrs",
          "getAllRights -t account -c USER",
          "getAllRights -t cos",
          "getAllRights -v -c ADMIN",
          "getAllRights -v -c USER",
          "getAllRights -v -t domain -c ALL",
          "getEffectiveRights account grantee.name@example.com expandSetAttrs expandGetAttrs",
          "getEffectiveRights account target@example.com 75aca60e-8616-4165-a3ba-a6b96d529c97 expandSetAttrs",
          "getEffectiveRights account target@example.com grantee.name@example.com expandGetAttrs",
          "getEffectiveRights cos 22c3163a-ea39-4b65-a1c2-88447b30000f 75aca60e-8616-4165-a3ba-a6b96d529c97",
          "getEffectiveRights cos target@example.com 75aca60e-8616-4165-a3ba-a6b96d529c97 expandSetAttrs expandGetAttrs",
          "getGrants -g dom grantee.name@example.com 0",
          "getGrants -g egp grantee.name@example.com 1",
          "getGrants -g email 75aca60e-8616-4165-a3ba-a6b96d529c97 1",
          "getGrants -g grp 75aca60e-8616-4165-a3ba-a6b96d529c97",
          "getGrants -g key grantee.name@example.com",
          "getRight getAccount",
          "getRight getAccount -e",
          "getRight invite",
          "getRight invite -e",
          "getRight viewFreeBusy -e",
          "getRightsDoc",
          "getRightsDoc org.example.mypackage",
          "grantRight account 75aca60e-8616-4165-a3ba-a6b96d529c97 usr test@test.com secret",
          "grantRight config usr test@test.com grp modifyAccount",
          "grantRight group 75aca60e-8616-4165-a3ba-a6b96d529c97 grp 8a64a712-cceb-4e03-b5ce-c131481bb455 secret",
          "revokeRight account user@example.com usr grantee.name@example.com listAccount",
          "revokeRight config usr 75aca60e-8616-4165-a3ba-a6b96d529c97 viewFreeBusy",
          "revokeRight cos cosName grp 75aca60e-8616-4165-a3ba-a6b96d529c97 viewFreeBusy",
          "revokeRight domain test.com usr grantee.name@example.com invite",
          "revokeRight global usr 75aca60e-8616-4165-a3ba-a6b96d529c97 viewFreeBusy"
  })
  void provUtilRight(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "createServer someName",
          "createServer someName zimbraId 1 zimbraImapBindPort 1",
          "createServer someName zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "deleteServer 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteServer someName",
          "getAllMemcachedServers",
          "getAllMtaAuthURLs",
          "getAllServers -v",
          "getAllServers -v -e mta",
          "getAllServers -v -e spell",
          "getAllServers antispam",
          "getAllServers spell",
          "getServer -e 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getServer -e someName",
          "getServer 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getServer someName attr attr",
          "getServer someName attr attr attr",
          "modifyServer 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyServer someName zimbraId 1 zimbraImapBindPort 1",
          "modifyServer someName zimbraId 1 zimbraImapBindPort 1 zimbraId 1"
  })
  void provUtilServer(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "addAccountLogger --server localhost 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbra.soap debug",
          "addAccountLogger --server localhost user@example.com zimbra.soap trace",
          "addAccountLogger -s localhost user@example.com zimbra.lmtp error",
          "addAccountLogger 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbra.lmtp warn",
          "addAccountLogger user@example.com zimbra.lmtp info",
          "getAccountLoggers --server localhost 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccountLoggers --server localhost user@example.com",
          "getAccountLoggers -s localhost 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccountLoggers 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccountLoggers user@example.com",
          "getAllAccountLoggers",
          "getAllAccountLoggers --server localhost",
          "getAllAccountLoggers -s localhost",
          "removeAccountLogger --server localhost",
          "removeAccountLogger --server localhost 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "removeAccountLogger -s localhost user@example.com zimbra.soap",
          "removeAccountLogger 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbra.soap",
          "removeAccountLogger zimbra.lmtp",
          "resetAllLoggers",
          "resetAllLoggers --server localhost",
          "resetAllLoggers -s localhost"
  })
  void provUtilLog(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "-l countObjects account -d test.com",
          "-l countObjects alias -d test.com",
          "-l countObjects calresource -d f4806430-b434-4e93-9357-a02d9dd796b8",
          "-l countObjects cos",
          "-l countObjects dl",
          "createBulkAccounts example.com namemask 1 password",
          "createBulkAccounts example.com namemask 4 password",
          "createDistributionListsBulk test.com domain 3",
          "describe -a zimbraId",
          "describe -ni group",
          "describe -v cos",
          "describe -v server",
          "flushCache all someName 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "flushCache domain",
          "flushCache globalgrant someName 8a64a712-cceb-4e03-b5ce-c131481bb455 someName",
          "flushCache group",
          "flushCache locale someName 8a64a712-cceb-4e03-b5ce-c131481bb455 someName",
          "generateDomainPreAuth 8a64a712-cceb-4e03-b5ce-c131481bb455 8a64a712-cceb-4e03-b5ce-c131481bb455 by 0 1729944652",
          "generateDomainPreAuth 8a64a712-cceb-4e03-b5ce-c131481bb455 8a64a712-cceb-4e03-b5ce-c131481bb455 by 1732623357 1729944652",
          "generateDomainPreAuth example.com 8a64a712-cceb-4e03-b5ce-c131481bb455 by 0 0",
          "generateDomainPreAuth example.com 8a64a712-cceb-4e03-b5ce-c131481bb455 by 1732623357 0",
          "generateDomainPreAuth example.com someName by 0 0",
          "-l generateDomainPreAuthKey test.com",
          "-l generateDomainPreAuthKey f4806430-b434-4e93-9357-a02d9dd796b8",
          "-l generateDomainPreAuthKey -f test.com",
          "-l generateDomainPreAuthKey -f f4806430-b434-4e93-9357-a02d9dd796b8",
          "getAuthTokenInfo 0_2b6c930a7ca1a02daad5f27528d6c9986317204e_69643d33363a62333134613231652d666137392d346533352d613765352d6437666637303834333866363b6578703d31333a313733323535383437303239303b76763d323a31363b747970653d363a7a696d6272613b753d313a613b7469643d31303a313131353331313832383b",
          "getMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105",
          "getMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105",
          "getMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105",
          "getMemcachedClientConfig localhost",
          "help commands",
          "reloadMemcachedClientConfig all",
          "reloadMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105",
          "reloadMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105",
          "reloadMemcachedClientConfig f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105 f129be06-86bd-4123-8232-be39a96c2105",
          "syncGal example.com",
          "syncGal example.com 1732621757"
  })
  void provUtilMisc(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "-l getAllReverseProxyDomains",
          "getAllReverseProxyBackends",
          "getAllReverseProxyURLs"
  })
  void provUtilReverseproxy(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
//          "-l renameDomain f4806430-b434-4e93-9357-a02d9dd796b8 new.example.com",
//          "-l renameDomain test.com new.example.com",
          "countAccount 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "countAccount example.com",
          "createAliasDomain example-alias.com 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "createAliasDomain example-alias.com local-example.com",
          "createAliasDomain example-alias.com local-example.com zimbraId 1 zimbraImapBindPort 1",
          "createAliasDomain example-alias.com local-example.com zimbraId 1 zimbraImapBindPort 1 zimbraMailHost \"value\"",
          "createAliasDomain test.com local-example.com zimbraImapBindPort 1 zimbraMailHost \"value\"",
          "createDomain example.com",
          "createDomain example.com zimbraId 1 zimbraImapBindPort 1",
          "createDomain example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "deleteDomain 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteDomain example.com",
          "getAllDomains -v",
          "getAllDomains -v -e",
          "getAllDomains -v -e attr attr attr",
          "getAllDomains attr attr",
          "getDomain -e 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getDomain -e example.com",
          "getDomain 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getDomain example.com attr attr",
          "getDomain example.com attr attr attr",
          "getDomainInfo id 42",
          "getDomainInfo id 8a64a712-cceb-4e03-b5ce-c131481bb455 value attr attr attr",
          "getDomainInfo name 42 attr attr attr",
          "getDomainInfo virtualHostname 42 attr attr",
          "getDomainInfo virtualHostname host.example.org 42",
          "modifyDomain 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDomain example.com zimbraId 1 zimbraImapBindPort 1",
          "modifyDomain example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1"
  })
  void provUtilDomain(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "-l searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) 0 0 sortAscending false example.com example.com",
          "-l searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) 0 1",
          "-l searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) 0 1 sortBy attr example.com example.com",
          "-l searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) sortAscending true example.com example.com",
          "-l searchAccounts (&(zimbraVirtualIPAddress=192.168.0.58)(objectClass=zimbraDomain)) sortBy attr sortAscending false",
          "-l searchCalendarResources -v test.com attr eq 42",
          "-l searchCalendarResources -v test.com attr eq 42 attr2 has 345334",
          "-l searchCalendarResources test.com attr eq 42",
          "-l searchCalendarResources test.com attr eq 42 attr2 endswith 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "-l searchCalendarResources test.com attr eq 42 attr2 has 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "-l searchCalendarResources test.com attr eq value attr2 has 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "autoCompleteGal example.com someName",
          "searchGal example.com someName 0 42 sortBy attr",
          "searchGal example.com someName 1 1 1 0 sortBy attr",
          "searchGal example.com someName 1 10",
          "searchGal example.com someName 1 10 1 1 sortBy attr",
          "searchGal example.com someName 42 42"
  })
  void provUtilSearch(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "autoProvControl start",
          "autoProvControl status",
          "autoProvControl stop"
  })
  void provUtilCommands(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "addDistributionListAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com",
          "addDistributionListAlias list@example.com alias@example.com",
          "addDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com",
          "addDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com member@example.com member@example.com",
          "addDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com member@example.com member@example.com member@example.com",
          "addDistributionListMember list@example.com member@example.com",
          "addDistributionListMember list@example.com member@example.com member@example.com member@example.com",
          "createDistributionList list@example.com",
          "createDynamicDistributionList list@example.com",
          "deleteDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 false",
          "deleteDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 true",
          "deleteDistributionList list@example.com",
          "deleteDistributionList list@example.com true",
          "getAllDistributionLists",
          "getAllDistributionLists -v",
          "getAllDistributionLists -v test.com",
          "getAllDistributionLists test.com",
          "getDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getDistributionList list@example.com",
          "getDistributionList list@example.com attr attr",
          "getDistributionList list@example.com attr attr attr",
          "getDistributionListMembership 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getDistributionListMembership user@example.com",
          "modifyDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDistributionList list@example.com zimbraId 1 zimbraImapBindPort 1",
          "modifyDistributionList list@example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "removeDistributionListAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com",
          "removeDistributionListAlias list@example.com alias@example.com",
          "removeDistributionListMember 8a64a712-cceb-4e03-b5ce-c131481bb455 member@example.com",
          "removeDistributionListMember list@example.com member@example.com",
          "renameDistributionList 8a64a712-cceb-4e03-b5ce-c131481bb455 newName@domain",
          "renameDistributionList list@example.com newName@domain"
  })
  void provUtilList(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "compactIndexMailbox user@example.com start",
          "compactIndexMailbox user@example.com status",
          "compactIndexMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 start",
          "compactIndexMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 status",
          "getIndexStats user@example.com",
          "getIndexStats user@example.com",
          "getIndexStats 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getMailboxInfo 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "getQuotaUsage localhost",
          "recalculateMailboxCounts 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "recalculateMailboxCounts user@example.com",
          "unlockMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "unlockMailbox user@example.com",
          "verifyIndex 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "verifyIndex user@example.com"
  })
  void provUtilMailbox(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "getAllConfig",
          "getAllConfig attr attr",
          "getAllConfig attr attr attr",
          "getConfig someName",
          "modifyConfig zimbraId 1 zimbraImapBindPort 1",
          "modifyConfig zimbraId 1 zimbraImapBindPort 1 zimbraId 1"
  })
  void provUtilConfig(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "copyCos 8a64a712-cceb-4e03-b5ce-c131481bb455 cos",
          "copyCos srcCosName cos",
          "createCos someName",
          "createCos someName zimbraId 1",
          "createCos someName zimbraId 1 zimbraImapBindPort 1",
          "deleteCos 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteCos someName",
          "getAllCos",
          "getAllCos -v",
          "getCos 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getCos 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getCos someName",
          "getCos someName attr attr",
          "getCos someName attr attr attr",
          "modifyCos 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyCos someName zimbraId 1 zimbraImapBindPort 1",
          "modifyCos someName zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "renameCos 8a64a712-cceb-4e03-b5ce-c131481bb455 newName",
          "renameCos someName newName"
  })
  void provUtilCos(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "getShareInfo ed56a2de-1418-4ff6-a790-988c19c6004d",
          "getShareInfo ownerName"
  })
  void provUtilShare(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "createCalendarResource user@example.com password",
          "createCalendarResource user@example.com password zimbraId 1 zimbraImapBindPort 1",
          "deleteCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "deleteCalendarResource user@example.com",
          "getAllCalendarResources -e -v -s localhost test.com",
          "getAllCalendarResources -s localhost",
          "getAllCalendarResources -s localhost test.com",
          "getAllCalendarResources -v",
          "getAllCalendarResources -v -e test.com",
          "getCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getCalendarResource user@example.com",
          "getCalendarResource user@example.com attr attr",
          "getCalendarResource user@example.com attr attr attr",
          "modifyCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyCalendarResource user@example.com zimbraId 1 zimbraImapBindPort 1",
          "modifyCalendarResource user@example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "purgeAccountCalendarCache 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "purgeAccountCalendarCache 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593 user@example.com",
          "purgeAccountCalendarCache user@example.com",
          "purgeAccountCalendarCache user@example.com 301c1dab-c07d-478c-b5db-eaffcc64b593 user@example.com",
          "purgeAccountCalendarCache user@example.com 301c1dab-c07d-478c-b5db-eaffcc64b593 user@example.com 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "renameCalendarResource 8a64a712-cceb-4e03-b5ce-c131481bb455 newName@domain",
          "renameCalendarResource user@example.com newName@domain"
  })
  void provUtilCalendar(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "getAllFbp",
          "getAllFbp -v",
          "getFreebusyQueueInfo",
          "getFreebusyQueueInfo provider-name",
          "purgeFreebusyQueue",
          "purgeFreebusyQueue provider-name",
          "pushFreebusy 186c1c23-d2ad-46b4-9efd-ddd890b1a4a2d",
          "pushFreebusy 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "pushFreebusy 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593 301c1dab-c07d-478c-b5db-eaffcc64b593",
          "pushFreebusy test@test.com",
          "pushFreebusyDomain example.com"
  })
  void provUtilFreebusy(String cmd) throws IOException {
    run(cmd);
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "-l getAllAccounts -e -v -s localhost test.com",
          "-l getAllAccounts -s localhost",
          "-l getAllAccounts -s localhost test.com",
          "-l getAllAccounts -v",
          "-l getAllAccounts -v -e test.com",
          "-l getAllAdminAccounts -v",
          "-l getAllAdminAccounts -v -e",
          "-l getAllAdminAccounts -v -e attr attr attr",
          "-l getAllAdminAccounts attr attr",
          "-l getAllAdminAccounts attr attr attr",
          "addAccountAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com",
          "addAccountAlias user@example.com alias@example.com",
          "changePrimaryEmail 8a64a712-cceb-4e03-b5ce-c131481bb455 newName@domain",
          "changePrimaryEmail user@example.com newName@domain",
          "checkPasswordStrength 8a64a712-cceb-4e03-b5ce-c131481bb455 password",
          "checkPasswordStrength user@example.com password",
          "createAccount user@example.com password",
          "createAccount user@example.com password zimbraId 1 zimbraImapBindPort 1",
          "createAccount user@example.com password zimbraId 1 zimbraImapBindPort 1 zimbraMailHost 1",
          "createDataSource user@example.com contacts databaseName zimbraDataSourceEnabled TRUE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237",
          "createDataSource user@example.com contacts databaseName zimbraDataSourceEnabled TRUE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237 zimbraId 1 zimbraImapBindPort 1",
          "createDataSource user@example.com pop3 databaseName zimbraDataSourceEnabled FALSE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237",
          "createDataSource user@example.com pop3 databaseName zimbraDataSourceEnabled FALSE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237 zimbraId 1 zimbraImapBindPort 1",
          "createDataSource user@example.com pop3 databaseName zimbraDataSourceEnabled TRUE zimbraDataSourceFolderId eb15a846-21ed-4f1f-bf21-2759f282c237 zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "deleteAccount 186c1c23-d2ad-46b4-9efd-ddd890b1a4a2",
          "deleteAccount test@test.com",
          "deleteDataSource 8a64a712-cceb-4e03-b5ce-c131481bb455 5bfd9bc4-d359-4a2c-8424-1101dffba0ee",
          "deleteDataSource user@example.com 5bfd9bc4-d359-4a2c-8424-1101dffba0ee",
          "getAccount -e 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccount -e user@example.com",
          "getAccount 8a64a712-cceb-4e03-b5ce-c131481bb455 attr attr attr",
          "getAccount user@example.com attr attr",
          "getAccount user@example.com attr attr attr",
          "getAccountMembership 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getAccountMembership user@example.com",
          "getDataSources 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "getDataSources 8a64a712-cceb-4e03-b5ce-c131481bb455 argument1 argument2 argument1",
          "getDataSources user@example.com",
          "getDataSources user@example.com argument1 argument2",
          "getDataSources user@example.com argument1 argument2 argument1",
          "modifyAccount test@test.com zimbraImapBindPort 1",
          "modifyAccount user@example.com zimbraId 1 zimbraImapBindPort 1",
          "modifyAccount user@example.com zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDataSource 8a64a712-cceb-4e03-b5ce-c131481bb455 5bfd9bc4-d359-4a2c-8424-1101dffba0ee zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "modifyDataSource user@example.com 5bfd9bc4-d359-4a2c-8424-1101dffba0ee zimbraId 1 zimbraImapBindPort 1 zimbraId 1",
          "removeAccountAlias 8a64a712-cceb-4e03-b5ce-c131481bb455 alias@example.com",
          "removeAccountAlias user@example.com alias@example.com",
          "renameAccount 8a64a712-cceb-4e03-b5ce-c131481bb455 newName@domain",
          "renameAccount user@example.com newName@domain",
          "setAccountCos 8a64a712-cceb-4e03-b5ce-c131481bb455 1829acc8-2fd3-45cf-aac5-f3b3078daaa8",
          "setAccountCos 8a64a712-cceb-4e03-b5ce-c131481bb455 cos-name",
          "setAccountCos user@example.com 1829acc8-2fd3-45cf-aac5-f3b3078daaa8",
          "setAccountCos user@example.com cos-name",
          "setPassword 8a64a712-cceb-4e03-b5ce-c131481bb455 password",
          "setPassword user@example.com password"
  })
  void provUtilAccount(String cmd) throws IOException {
    run(cmd);
  }

  @Disabled
  @ParameterizedTest
  @ValueSource(strings = {
          "deleteSignature user@example.com signature-name",
          "deleteSignature 8a64a712-cceb-4e03-b5ce-c131481bb455 signature-name",
          "exit",
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
          "reIndexMailbox user@example.com status 8a64a712-cceb-4e03-b5ce-c131481bb455 type 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "reIndexMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 status 8a64a712-cceb-4e03-b5ce-c131481bb455 type",
          "reIndexMailbox user@example.com status",
          "reIndexMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 status",
          "reIndexMailbox user@example.com start 8a64a712-cceb-4e03-b5ce-c131481bb455 type 8a64a712-cceb-4e03-b5ce-c131481bb455",
          "unlockMailbox user@example.com localhost",
          "unlockMailbox 8a64a712-cceb-4e03-b5ce-c131481bb455 localhost",
  })
  void provUtilFailingTests(String cmd) throws IOException {
    run(cmd);
  }
}
