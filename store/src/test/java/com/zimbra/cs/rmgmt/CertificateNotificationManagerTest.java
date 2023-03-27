package com.zimbra.cs.rmgmt;

import static org.junit.Assert.assertEquals;
import static com.zimbra.cs.service.admin.CertificateNotificationManager.*;
import static org.mockito.Mockito.mock;

import com.zimbra.cs.account.Domain;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.service.admin.CertificateNotificationManager;
import java.util.Map;

import org.junit.Test;

public class CertificateNotificationManagerTest {
  Mailbox mailbox = mock(Mailbox.class);
  Domain domain = mock(Domain.class);

  @Test
  public void shouldParseSystemFailureMessage() {
    final String inputMessage = "system failure: exception executing command "
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

    final String expectedMessage = HEADER + FAILURE_RESULT + inputMessage;
    final Map<String, Object> notificationMap = CertificateNotificationManager.createIssueCertNotificationMap(inputMessage);
    assertEquals(SYSTEM_FAILURE, notificationMap.get(SUBJECT_RESULT));
    assertEquals(expectedMessage, notificationMap.get(GLOBAL_MESSAGE));
  }

  @Test
  public void shouldParseCertbotFailureMessage() {
    final String included = "Simulating a certificate request for test.zextras.io\n"
        + "\n"
        + "Certbot failed to authenticate some domains (authenticator: webroot). "
        + "The Certificate Authority reported these problems:\n"
        + "Domain: test.zextras.io\n"
        + "Type: dns\n"
        + "Detail: DNS problem: NXDOMAIN looking up A for test.zextras.io "
        + "- check that a DNS record exists for this domain; DNS problem: NXDOMAIN looking up AAAA "
        + "for test.zextras.io - check that a DNS record exists for this domain\n"
        + "\n"
        + "Hint: The Certificate Authority failed to download the temporary challenge files created "
        + "by Certbot. Ensure that the listed domains serve their content from the provided "
        + "--webroot-path/-w and that files created there can be downloaded from the internet.\n"
        + "\n";
    final String inputMessage = "STARTCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot "
        + "certonly --agree-tos --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras "
        + "--cert-name test.zextras.io -d test.zextras.io -d test.zextras.io\n"
        + included
        + "ENDCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot certonly --agree-tos "
        + "--email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras "
        + "--cert-name test.zextras.io -d test.zextras.io -d test.zextras.io";

    final String expectedGlobalMessage = HEADER + FAILURE_RESULT + included;
    final Map<String, Object> notificationMap = CertificateNotificationManager.createIssueCertNotificationMap(inputMessage);
    assertEquals(CERTBOT_FAILURE, notificationMap.get(SUBJECT_RESULT));
    assertEquals(expectedGlobalMessage, notificationMap.get(GLOBAL_MESSAGE));
  }

  @Test
  public void shouldParseCertbotSuccessfullyReceivedMessage() {
    final String inputMessage = "STARTCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot "
        + "certonly --agree-tos --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras "
        + "--cert-name le.zextras.io -d le1.zextras.io -d le2.zextras.io\n"
        + "Simulating a certificate request for le1.zextras.io and le2.zextras.io\n"
        + "The dry run was successful.\n"
        + "Requesting a certificate for le1.zextras.io and le2.zextras.io\n"
        + "\n"
        + "Successfully received certificate.\n"
        + "Certificate is saved at: "
        + "/opt/zextras/common/certbot/etc/letsencrypt/live/le.zextras.io/fullchain.pem\n"
        + "Key is saved at: "
        + "/opt/zextras/common/certbot/etc/letsencrypt/live/le.zextras.io/privkey.pem\n"
        + "This certificate expires on 2023-06-15.\n"
        + "These files will be updated when the certificate renews.\n"
        + "NEXT STEPS:\n"
        + "- The certificate will need to be renewed before it expires. Certbot can automatically "
        + "renew the certificate in the background, but you may need to take steps to enable that "
        + "functionality. See https://certbot.org/renewal-setup for instructions.\n"
        + "\n"
        + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        + "If you like Certbot, please consider supporting our work by:\n"
        + "* Donating to ISRG / Let's Encrypt: https://letsencrypt.org/donate\n"
        + "* Donating to EFF: https://eff.org/donate-le\n"
        + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        + "ENDCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot "
        + "certonly --agree-tos --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras"
        + " --cert-name le.zextras.io -d le1.zextras.io -d le2.zextras.io";
    final String template = "The certificate was successfully received.\n"
        + "*Please NOTE  that the Certificate and Key will be available after the proxy reload. "
        + "You’ll be able to download them from the Certificate section in the admin interface.\n"
        + "\n"
        + "The files will be automatically updated when the certificate renews.\n"
        + "This certificate expires on 2023-06-15.";

    final String expectedMessage = HEADER + SUCCESS_RESULT + template;
    final Map<String, Object> actualMessage = CertificateNotificationManager.createIssueCertNotificationMap(inputMessage);
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  public void shouldParseCertbotNotYetDueForRenewalMessage() {
    final String included = "Simulating renewal of an existing certificate for abc.demo.zextras.io\n"
        + "The dry run was successful.\n"
        + "Certificate not yet due for renewal\n"
        + "\n"
        + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        + "Certificate not yet due for renewal; no action taken.\n"
        + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n";
    final String inputMessage = "STARTCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot "
        + "certonly --agree-tos --email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras "
        + "--cert-name test.zextras.io -d test.zextras.io -d test.zextras.io\n"
        + included
        + "ENDCMD: nbm-m01.demo.zextras.io /opt/zextras/libexec/certbot certonly --agree-tos "
        + "--email zextras@demo.zextras.io -n --keep --webroot -w /opt/zextras "
        + "--cert-name test.zextras.io -d test.zextras.io -d test.zextras.io";

    final String expectedMessage = HEADER + SUCCESS_RESULT + included;
    final Map<String, Object> actualMessage = CertificateNotificationManager.createIssueCertNotificationMap(inputMessage);
    assertEquals(expectedMessage, actualMessage);
  }

}