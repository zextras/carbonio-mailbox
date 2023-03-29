package com.zimbra.cs.service.admin;

import static com.zimbra.cs.service.admin.CertificateNotificationManager.CERTBOT_FAILURE;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.CERTBOT_SUCCESS;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.DOMAIN_MESSAGE;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.FAILURE_DOMAIN_NOTIFICATION_TEMPLATE;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.FAILURE_RESULT;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.GLOBAL_MESSAGE;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.HEADER;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.SUBJECT_RESULT;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.SUBJECT_TEMPLATE;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.SUCCESS_DOMAIN_NOTIFICATION_TEMPLATE;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.SUCCESS_RESULT;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.SYSTEM_FAILURE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import java.util.List;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.junit.Before;
import org.junit.Test;

public class CertificateNotificationManagerTest {
  private final String systemFailureMessage =
      "system failure: exception executing command "
          + "certbot certonly --agree-tos --email zextras@demo.zextras.io -n --keep "
          + "--webroot -w /opt/zextras --cert-name test.tld -d test.tld -d test.tld "
          + "with {RemoteManager: nbm-m02.demo.zextras.io->zextras@nbm-m01.demo.zextras.io:22} "
          + "java.io.IOException: FAILURE: exit status=1\n"
          + "STDOUT=STARTCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot "
          + "certonly --agree-tos --email zextras@demo.zextras.io -n --keep "
          + "--webroot -w /opt/zextras --cert-name test.tld -d test.tld -d test.tld\n"
          + "STDERR= An unexpected error occurred:\n"
          + "Error creating new order :: Cannot issue for \"test.tld\": "
          + "Domain name does not end with a valid public suffix (TLD)\n";

  private final String certbotFailureMessage =
      "STARTCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot certonly --agree-tos"
          + " --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras --cert-name"
          + " test.zextras.io -d test.zextras.io -d test.zextras.io\n"
          + "Simulating a certificate request for test.zextras.io\n"
          + "\n"
          + "Certbot failed to authenticate some domains (authenticator: webroot). The"
          + " Certificate Authority reported these problems:\n"
          + "Domain: test.zextras.io\n"
          + "Type: dns\n"
          + "Detail: DNS problem: NXDOMAIN looking up A for test.zextras.io - check that a DNS"
          + " record exists for this domain; DNS problem: NXDOMAIN looking up AAAA for"
          + " test.zextras.io - check that a DNS record exists for this domain\n"
          + "\n"
          + "Hint: The Certificate Authority failed to download the temporary challenge files"
          + " created by Certbot. Ensure that the listed domains serve their content from the"
          + " provided --webroot-path/-w and that files created there can be downloaded from the"
          + " internet.\n"
          + "\n"
          + "ENDCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot certonly --agree-tos"
          + " --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras --cert-name"
          + " test.zextras.io -d test.zextras.io -d test.zextras.io";

  private final String certbotSuccessMessage =
      "STARTCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot certonly --agree-tos"
          + " --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras --cert-name"
          + " le.zextras.io -d le1.zextras.io -d le2.zextras.io\n"
          + "Simulating a certificate request for le1.zextras.io and le2.zextras.io\n"
          + "The dry run was successful.\n"
          + "Requesting a certificate for le1.zextras.io and le2.zextras.io\n"
          + "\n"
          + "Successfully received certificate.\n"
          + "Certificate is saved at:"
          + " /opt/zextras/common/certbot/etc/letsencrypt/live/le.zextras.io/fullchain.pem\n"
          + "Key is saved at:"
          + " /opt/zextras/common/certbot/etc/letsencrypt/live/le.zextras.io/privkey.pem\n"
          + "This certificate expires on 2023-06-15.\n"
          + "These files will be updated when the certificate renews.\n"
          + "NEXT STEPS:\n"
          + "- The certificate will need to be renewed before it expires. Certbot can"
          + " automatically renew the certificate in the background, but you may need to take"
          + " steps to enable that functionality. See https://certbot.org/renewal-setup for"
          + " instructions.\n"
          + "\n"
          + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
          + "If you like Certbot, please consider supporting our work by:\n"
          + "* Donating to ISRG / Let's Encrypt: https://letsencrypt.org/donate\n"
          + "* Donating to EFF: https://eff.org/donate-le\n"
          + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
          + "ENDCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot certonly --agree-tos"
          + " --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras --cert-name"
          + " le.zextras.io -d le1.zextras.io -d le2.zextras.io";

  private final String otherCertbotMessage =
      "STARTCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot certonly --agree-tos"
          + " --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras --cert-name"
          + " test.zextras.io -d test.zextras.io -d test.zextras.io\n"
          + "Simulating renewal of an existing certificate for abc.demo.zextras.io\n"
          + "The dry run was successful.\n"
          + "Certificate not yet due for renewal\n"
          + "\n"
          + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
          + "Certificate not yet due for renewal; no action taken.\n"
          + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
          + "ENDCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot certonly --agree-tos"
          + " --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras --cert-name"
          + " test.zextras.io -d test.zextras.io -d test.zextras.io";


  private Mailbox mailbox = mock(Mailbox.class);
  private Domain domain = mock(Domain.class);
  private Provisioning provisioning = mock(Provisioning.class);
  private Config config = mock(Config.class);
  private MailSender mailSender = mock(MailSender.class);
  private String from = "admin@test.com";
  private String[] recipients = new String[] {from, "admin2@test.com"};
  private String domainName = "test.com";

  @Before
  public void setUp() throws ServiceException {
    when(domain.getName()).thenReturn(domainName);
    Provisioning.setInstance(provisioning);
    when(provisioning.getConfig()).thenReturn(config);
    when(config.getCarbonioNotificationFrom()).thenReturn(from);
    when(config.getCarbonioNotificationRecipients()).thenReturn(recipients);
    when(mailbox.getMailSender(domain)).thenReturn(mailSender);
    when(mailSender.getCurrentSession()).thenReturn(null);
  }

  @Test
  public void shouldNotify() throws ServiceException {
    CertificateNotificationManager.notify(mailbox, domain, systemFailureMessage);
    verify(mailSender).sendMimeMessageList(eq(mailbox), any());
  }

  @Test
  public void shouldCreateMapFromSystemFailureMessage() throws ServiceException {
    final Map<String, Object> notificationMap =
        CertificateNotificationManager.createIssueCertNotificationMap(domain, systemFailureMessage);

    assertEquals(SYSTEM_FAILURE, notificationMap.get(SUBJECT_RESULT));
    assertEquals(systemFailureMessage, notificationMap.get(GLOBAL_MESSAGE));
    assertFalse(notificationMap.containsKey(DOMAIN_MESSAGE));
  }

  @Test
  public void shouldCreateMapFromCertbotFailureMessage() throws ServiceException {
    final String expectedDomainMessage =
        HEADER + FAILURE_RESULT + FAILURE_DOMAIN_NOTIFICATION_TEMPLATE
            .replace("<DOMAIN_NAME>", domainName);
    final Map<String, Object> notificationMap =
        CertificateNotificationManager.createIssueCertNotificationMap(domain, certbotFailureMessage);

    assertEquals(CERTBOT_FAILURE, notificationMap.get(SUBJECT_RESULT));
    assertEquals(certbotFailureMessage, notificationMap.get(GLOBAL_MESSAGE));
    assertEquals(expectedDomainMessage, notificationMap.get(DOMAIN_MESSAGE));
  }

  @Test
  public void shouldCreateMapFromCertbotSuccessfullyReceivedMessage() throws ServiceException {
    final String expiration = "\n" + "This certificate expires on 2023-06-15.";
    final String expectedDomainMessage =
        HEADER + SUCCESS_RESULT + SUCCESS_DOMAIN_NOTIFICATION_TEMPLATE + expiration;
    final Map<String, Object> notificationMap =
        CertificateNotificationManager.createIssueCertNotificationMap(domain, certbotSuccessMessage);

    assertEquals(CERTBOT_SUCCESS, notificationMap.get(SUBJECT_RESULT));
    assertEquals(certbotSuccessMessage, notificationMap.get(GLOBAL_MESSAGE));
    assertEquals(expectedDomainMessage, notificationMap.get(DOMAIN_MESSAGE));
  }

  @Test
  public void shouldCreateMapFromOtherCertbotMessage() throws ServiceException {
    final Map<String, Object> notificationMap =
        CertificateNotificationManager.createIssueCertNotificationMap(domain, otherCertbotMessage);

    assertEquals(CERTBOT_SUCCESS, notificationMap.get(SUBJECT_RESULT));
    assertEquals(otherCertbotMessage, notificationMap.get(GLOBAL_MESSAGE));
    assertFalse(notificationMap.containsKey(DOMAIN_MESSAGE));
  }


  @Test
  public void shouldCreateMimeMessageList() throws Exception {
    String subject = domainName + SUBJECT_TEMPLATE + SYSTEM_FAILURE;

    final Map<String, Object> notificationMap =
        CertificateNotificationManager.createIssueCertNotificationMap(domain, systemFailureMessage);

    List<MimeMessage> actualList =
        CertificateNotificationManager.createMimeMessageList(null, notificationMap);

    MimeMessage actualMimeMessage = actualList.get(0);

    assertEquals(from, actualMimeMessage.getSender().toString());
    assertEquals(from, actualMimeMessage.getFrom()[0].toString());
    assertEquals(recipients[0], actualMimeMessage.getAllRecipients()[0].toString());
    assertEquals(recipients[1], actualMimeMessage.getAllRecipients()[1].toString());
    assertEquals(subject, actualMimeMessage.getSubject());
    assertEquals(systemFailureMessage, actualMimeMessage.getContent());
  }
}
