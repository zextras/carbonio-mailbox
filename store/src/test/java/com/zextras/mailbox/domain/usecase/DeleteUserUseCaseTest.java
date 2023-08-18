package com.zextras.mailbox.domain.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.common.io.Files;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LocalConfig;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import io.vavr.control.Try;
import java.io.File;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;

@Execution(ExecutionMode.CONCURRENT)
public class DeleteUserUseCaseTest {

  private DeleteUserUseCase deleteUserUseCase;
  private Provisioning mockProvisioning;
  private File localconfig;
  private File tempDir;
  private final String localconfigContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "\n"
          + "<localconfig>\n"
          + "  <key name=\"ssl_default_digest\">\n"
          + "    <value>sha256</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_tls_high_cipherlist\">\n"
          + "    <value>:EDH+AESGCM:AES256+EECDH:AES256+EDH:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-SHA384:ECDHE-ECDSA-AES128-SHA256:ECDHE-ECDSA-AES128-SHA:AES256+EECDH:AES256+EDH:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-SHA256:ECDHE-RSA-AES128-SHA:ECDHE-RSA-AES256-SHA384:ECDHE-RSA-AES256-SHA:AES256-GCM-SHA384:AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-SHA256:DHE-RSA-AES256-SHA:DHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES128-SHA256:DHE-RSA-AES128-SHA:DHE-DSS-AES256-GCM-SHA384:DHE-DSS-AES256-SHA256:DHE-DSS-AES256-SHA:DHE-DSS-AES128-GCM-SHA256:DHE-DSS-AES128-SHA256:kEDH+AESGCM:AES256:AES128:HIGH:!DHE-RSA-AES128-SHA:!DHE-RSA-CAMELLIA256-SHA:!DHE-RSA-CAMELLIA128-SHA:!AES256-SHA:!AES128-SHA:!CAMELLIA256-SHA:!CAMELLIA128-SHA:!ECDHE-RSA-DES-CBC3-SHA:!EDH-RSA-DES-CBC3-SHA:!DES-CBC3-SHA:!aNULL:!eNULL:!EXPORT:!DES:!MD5:!PSK:!RC4</value>\n"
          + "  </key>\n"
          + "  <key name=\"mailboxd_java_heap_size\">\n"
          + "    <value>1996</value>\n"
          + "  </key>\n"
          + "  <key name=\"ssl_allow_mismatched_certs\">\n"
          + "    <value>true</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_tls_random_source\">\n"
          + "    <value>dev:/dev/urandom</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_tls_eecdh_strong_curve\">\n"
          + "    <value>prime256v1</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_java_home\">\n"
          + "    <value>/opt/zextras/common/lib/jvm/java</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_port\">\n"
          + "    <value>389</value>\n"
          + "  </key>\n"
          + "  <key name=\"mailboxd_keystore\">\n"
          + "    <value>/opt/zextras/mailboxd/etc/keystore</value>\n"
          + "  </key>\n"
          + "  <key name=\"mailboxd_keystore_password\">\n"
          + "    <value>251aMaJzAE</value>\n"
          + "  </key>\n"
          + "  <key name=\"mailboxd_truststore\">\n"
          + "    <value>/opt/zextras/common/lib/jvm/java/lib/security/cacerts</value>\n"
          + "  </key>\n"
          + "  <key name=\"av_notify_user\">\n"
          + "    <value>zextras@demo.zextras.io</value>\n"
          + "  </key>\n"
          + "  <key name=\"mailboxd_directory\">\n"
          + "    <value>/opt/zextras/mailboxd</value>\n"
          + "  </key>\n"
          + "  <key name=\"av_notify_domain\">\n"
          + "    <value>demo.zextras.io</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_require_interprocess_security\">\n"
          + "    <value>0</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_tls_preempt_cipherlist\">\n"
          + "    <value>yes</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_gid\">\n"
          + "    <value>997</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_amavis_password\">\n"
          + "    <value>5xLLjwKEPh</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_url\">\n"
          + "    <value>ldap://nbm-s02.demo.zextras.io:389</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_smtpd_tls_dh512_param_file\">\n"
          + "    <value>/opt/zextras/conf/postfix_2048_dhparams.pem</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_starttls_supported\">\n"
          + "    <value>0</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_smtp_starttls_timeout\">\n"
          + "    <value>300s</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_smtpd_tls_session_cache_timeout\">\n"
          + "    <value>1800s</value>\n"
          + "  </key>\n"
          + "  <key name=\"ssl_allow_untrusted_certs\">\n"
          + "    <value>true</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_user\">\n"
          + "    <value>zextras</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_replication_password\">\n"
          + "    <value>5xLLjwKEPh</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_setgid_group\">\n"
          + "    <value>postdrop</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_mysql_password\">\n"
          + "    <value>4p8YOWQ_EEtdGjS4nvifvnalFrss</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_postfix_password\">\n"
          + "    <value>5xLLjwKEPh</value>\n"
          + "  </key>\n"
          + "  <key name=\"mysql_root_password\">\n"
          + "    <value>MHEuyrINhcGiHz57pKKPEd5TM8GK</value>\n"
          + "  </key>\n"
          + "  <key name=\"mailboxd_server\">\n"
          + "    <value>jetty</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_mysql_connector_maxActive\">\n"
          + "    <value>100</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_nginx_password\">\n"
          + "    <value>5xLLjwKEPh</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_master_url\">\n"
          + "    <value>ldap://nbm-s02.demo.zextras.io:389</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_ldap_password\">\n"
          + "    <value>5xLLjwKEPh</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_smtp_tls_policy_maps\">\n"
          + "    <value>lmdb:/opt/zextras/conf/postfix_tls_policy_file</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_zmjava_options\">\n"
          + "    <value>-Xmx256m -Dhttps.protocols=TLSv1.2,TLSv1.3 -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Djava.net.preferIPv4Stack=true</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_mail_service_port\">\n"
          + "    <value>8080</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_tls_eecdh_ultra_curve\">\n"
          + "    <value>secp384r1</value>\n"
          + "  </key>\n"
          + "  <key name=\"mysql_bind_address\">\n"
          + "    <value>127.0.0.1</value>\n"
          + "  </key>\n"
          + "  <key name=\"mailboxd_truststore_password\">\n"
          + "    <value>changeit</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_host\">\n"
          + "    <value>nbm-s02.demo.zextras.io</value>\n"
          + "  </key>\n"
          + "  <key name=\"zmtrainsa_cleanup_host\">\n"
          + "    <value>true</value>\n"
          + "  </key>\n"
          + "  <key name=\"antispam_mysql_host\">\n"
          + "    <value>127.0.0.1</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_smtpd_tls_mandatory_exclude_ciphers\">\n"
          + "    <value>aNULL, eNULL, EXPORT, DES, MD5, PSK</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_zmprov_default_to_ldap\">\n"
          + "    <value>false</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_uid\">\n"
          + "    <value>997</value>\n"
          + "  </key>\n"
          + "  <key name=\"mailboxd_java_options\">\n"
          + "    <value>-server -Dhttps.protocols=TLSv1.2,TLSv1.3 -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dsun.net.inetaddr.ttl=${networkaddress_cache_ttl} -Dorg.apache.jasper.compiler.disablejsr199=true -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=15 -XX:G1MaxNewSizePercent=45 -XX:-OmitStackTraceInFastThrow -verbose:gc -Xlog:gc*=info,safepoint=info:file=/opt/zextras/log/gc.log:time:filecount=20,filesize=10m</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_smtpd_tls_eecdh_grade\">\n"
          + "    <value>ultra</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_is_master\">\n"
          + "    <value>true</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_smtpd_tls_dh1024_param_file\">\n"
          + "    <value>/opt/zextras/conf/postfix_4096_dhparams.pem</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_smtp_tls_exclude_ciphers\">\n"
          + "    <value>aNULL, eNULL, EXPORT, DES, MD5, PSK</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_server_hostname\">\n"
          + "    <value>nbm-s02.demo.zextras.io</value>\n"
          + "  </key>\n"
          + "  <key name=\"ldap_root_password\">\n"
          + "    <value>5xLLjwKEPh</value>\n"
          + "  </key>\n"
          + "  <key name=\"postfix_mail_owner\">\n"
          + "    <value>postfix</value>\n"
          + "  </key>\n"
          + "  <key name=\"zimbra_ldap_userdn\">\n"
          + "    <value>uid=zimbra,cn=admins,cn=zimbra</value>\n"
          + "  </key>\n"
          + "</localconfig>";

  @BeforeEach
  void setUp() throws Exception {
    final Method localConfigLoader = LocalConfig.class.getDeclaredMethod("load", String.class);
    localConfigLoader.setAccessible(true);

    tempDir = Files.createTempDir();
    tempDir.deleteOnExit();

    localconfig = new File(tempDir + File.pathSeparator + "localconfig.xml");

    // Insert a virtual FS

    //    localConfigLoader.invoke();

    mockProvisioning = mock(Provisioning.class);
    deleteUserUseCase = new DeleteUserUseCase(mockProvisioning);
  }

  @Test
  void shouldReturnSuccessWhenUserDeleted() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);
    assertTrue(deleteResult.isSuccess());
    assertNull(deleteResult.get());
  }

  @Test
  void shouldReturnFailureWhenUserDoesntExist() throws Exception {
    final String userId = "nonExistingUser";
    when(mockProvisioning.getAccountById(userId)).thenReturn(null);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);
    assertTrue(deleteResult.isFailure());
    final RuntimeException runtimeException =
        assertThrows(RuntimeException.class, deleteResult::get);

    assertEquals("User " + userId + " doesn't exist", runtimeException.getMessage());
  }

  @Test
  void shouldPutAccountInMaintenanceModeBeforeDeletion() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);

    assertTrue(deleteResult.isSuccess());

    final String expectedStatusKind = ZAttrProvisioning.AccountStatus.maintenance.name();
    final ArgumentCaptor<String> gotStatusKind = ArgumentCaptor.forClass(String.class);
    verify(mockProvisioning).modifyAccountStatus(eq(mockAccount), gotStatusKind.capture());
    assertEquals(expectedStatusKind, gotStatusKind.getValue());
  }

  @Test
  void shouldDeleteMailboxIfAccountOnLocalServer() throws Exception {
    final String userId = "id123";
    final Account mockAccount = mock(Account.class);
    when(mockAccount.getAttr(Provisioning.A_zimbraMailHost)).thenReturn("mail.example.com");
    when(mockProvisioning.getAccountById(userId)).thenReturn(mockAccount);
    when(mockProvisioning.onLocalServer(mockAccount)).thenReturn(true);

    final Try<Void> deleteResult = deleteUserUseCase.delete(userId);

    assertTrue(deleteResult.isSuccess());
  }
}
