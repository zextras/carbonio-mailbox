package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * CertificateNotificationManager is intermediate between remote execution of {@link
 * com.zimbra.cs.rmgmt.RemoteCertbot} and {@link com.zimbra.cs.mailbox.MailSender}. Helps to create
 * mimeMessages based on domain and remote execution result in order to notify domain and global
 * recipients about {@link com.zimbra.soap.admin.message.IssueCertRequest}.
 *
 * @author Yuliya Aheeva
 * @since 23.5.0
 */
public class CertificateNotificationManager {
  public static final String GLOBAL_MESSAGE = "globalMessage";
  public static final String DOMAIN_MESSAGE = "domainMessage";

  public static final String SUBJECT = "subject";
  public static final String SUBJECT_TEMPLATE = " SSL certification request - ";

  public static final String SYSTEM_FAILURE = "system failure";

  public static final String CERTBOT_SUCCESS = "Certificate Authority success";
  public static final String CERTBOT_FAILURE = "Certificate Authority failure";

  public static final String RESULT = "result";
  public static final String HEADER = "The certification result is: ";
  public static final String SUCCESS_RESULT = "SUCCESS\n";
  public static final String FAILURE_RESULT = "FAILURE\n";

  public static final String FAIL = "fail";
  public static final String FAILURE_DOMAIN_NOTIFICATION_TEMPLATE =
      "\n"
          + "The SSL certificate request for <DOMAIN_NAME> was unsuccessful and the system "
          + "wasn't able to verify the validity of the domain.\n"
          + "\n"
          + "Most common reasons that could cause this kind of failure are:\n"
          + "- Misspelled or missing Public Service Hostname and/or Virtual Hostname."
          + " Make sure both are filled in with a valid Fully Qualified Domain Name.\n"
          + "- Wrong or missing A/AAAA entry for Public Service Hostname and/or Virtual Hostname."
          + " Make sure there is a valid, public resolution for the Fully Qualified Domain Name"
          + " used for any of the Public Service Hostname or Virtual Hostname.\n"
          + "- Private or unreachable IP address. In order to validate the domain name,"
          + "the Certificate Authority (CA) must be able to resolve and browse the provided Fully "
          + "Qualified Domain Name (FQDN).\n"
          + "\n"
          + "Check your environment for these common issues and try submitting the request again.\n"
          + "\n"
          + "If the error persists, please contact your system administrator(s) for assistance.";

  public static final String RECEIVED = "received";
  public static final String SUCCESS_DOMAIN_NOTIFICATION_TEMPLATE =
      "\n"
          + "The certificate for <DOMAIN_NAME> was successfully received.\n"
          + "Please NOTE  that the Certificate and Key will be available after the proxy reload.\n"
          + "You’ll be able to download them from the Certificate section in the admin interface.\n"
          + "\n"
          + "The files will be automatically updated when the certificate renews.\n";

  private final Mailbox mbox;
  private final Domain domain;

  private CertificateNotificationManager(Mailbox mbox, Domain domain) {
    this.mbox = mbox;
    this.domain = domain;
  }

  /**
   * Instantiates a CertificateNotificationManager object.
   *
   * @param mbox object of {@link com.zimbra.cs.mailbox.Mailbox} needed to get the proper {@link
   *     com.zimbra.cs.mailbox.MailSender} in order to send message
   * @param domain object of {@link com.zimbra.cs.account.Domain} needed to get {@link
   *     com.zimbra.common.account.ZAttrProvisioning} attributes A_carbonioNotificationRecipients
   *     and A_carbonioNotificationFrom
   * @return an instantiated object
   *
   * @author Yuliya Aheeva
   * @since 23.5.0
   */
  public static CertificateNotificationManager getCertificateNotificationManager(
      Mailbox mbox, Domain domain) {
    return new CertificateNotificationManager(mbox, domain);
  }

  /**
   * Notifies global and domain recipients about certificate generation result.
   *
   * @param outputMessage a message returned by RemoteManager/Certbot acme client
   */
  public void notify(Map<String, String> notificationMap) {
    String domainName = domain.getName();
    
    try {
      Provisioning provisioning = Provisioning.getInstance();
      Config config = provisioning.getConfig();

      String globalFrom =
          Optional.ofNullable(config.getCarbonioNotificationFrom())
              .orElseThrow(
                  () ->
                      ServiceException.FAILURE(
                          "Global CarbonioNotificationFrom attribute is not present.", null));
      String[] globalTo =
          Optional.ofNullable(config.getCarbonioNotificationRecipients())
              .orElseThrow(
                  () ->
                      ServiceException.FAILURE(
                          "Global CarbonioNotificationRecipients attribute is not present.", null));

      String domainFrom =
          Optional.ofNullable(domain.getCarbonioNotificationFrom()).orElse(globalFrom);
      String[] domainTo =
          Optional.ofNullable(domain.getCarbonioNotificationRecipients()).orElse(globalTo);

      MailSender sender = mbox.getMailSender(domain);
      Session session = sender.getCurrentSession();

      String subject = notificationMap.get(SUBJECT);

      List<MimeMessage> mimeMessageList = new ArrayList<>();

      MimeMessage globalNotification =
          createMimeMessage(
              session,
              subject,
              convert(globalFrom),
              convert(globalTo),
              notificationMap.get(GLOBAL_MESSAGE));
      mimeMessageList.add(globalNotification);

      if (notificationMap.containsKey(DOMAIN_MESSAGE)) {
        MimeMessage domainNotification = createMimeMessage(
            session,
            subject,
            convert(domainFrom),
            convert(domainTo),
            notificationMap.get(DOMAIN_MESSAGE));
        mimeMessageList.add(domainNotification);
      }

      sender.sendMimeMessageList(mbox, mimeMessageList);

      ZimbraLog.rmgmt.info(
          "Notifications about LetsEncrypt certificate generation were sent "
              + "for the global and domain "
              + domainName
              + " recipients.");

    } catch (Exception e) {
      ZimbraLog.rmgmt.info(
          "Notifications about LetsEncrypt certificate generation weren't sent "
              + "for the global and domain "
              + domainName
              + " recipients.\n"
              + "Sending failure: "
              + e.getMessage());
    }
  }

  /**
   * Parses and creates a map based on Remote Manager/Certbot output which would be used to
   * create {@link javax.mail.internet.MimeMessage}.
   *
   * @param outputMessage output from RemoteManager/Certbot
   * @return map with needed values of notification SUBJECT and MESSAGE TEXT
   */
  public Map<String, String> createIssueCertNotificationMap(String outputMessage) {
    ZimbraLog.rmgmt.info(
        "Issuing LetsEncrypt cert command for domain "
            + domain.getName()
            + " was finished with the following result: "
            + outputMessage);

    Map<String, String> notificationMap = new HashMap<>();
    notificationMap.put(GLOBAL_MESSAGE, outputMessage);

    // check if a system failure
    boolean isSystemFailure = outputMessage.contains(SYSTEM_FAILURE);
    if (isSystemFailure) {
      String subject = domain.getName() + SUBJECT_TEMPLATE + SYSTEM_FAILURE;
      notificationMap.put(SUBJECT, subject);
      return notificationMap;
    }

    // check if a certbot failure
    boolean isFailure = outputMessage.contains(FAIL);
    if (isFailure) {
      String subject = domain.getName() + SUBJECT_TEMPLATE + CERTBOT_FAILURE;
      notificationMap.put(SUBJECT, subject);

      String domainMessage =
          String.join(
              "",
              HEADER,
              FAILURE_RESULT,
              FAILURE_DOMAIN_NOTIFICATION_TEMPLATE.replace("<DOMAIN_NAME>", domain.getName()));

      notificationMap.put(DOMAIN_MESSAGE, domainMessage);
      return notificationMap;
    }

    // check if a certificate received
    boolean isReceived = outputMessage.contains(RECEIVED);
    if (isReceived) {
      String expire = "\n";

      String regex = "\\d{4}-\\d{2}-\\d{2}";
      Matcher matcher = Pattern.compile(regex).matcher(outputMessage);
      if (matcher.find()) {
        String expiresTemplate = "This certificate expires on ";
        expire = String.join("",expire, expiresTemplate, matcher.group(), ".");
      }

      String domainMessage =
          String.join(
              "",
              HEADER,
              SUCCESS_RESULT,
              SUCCESS_DOMAIN_NOTIFICATION_TEMPLATE.replace("<DOMAIN_NAME>", domain.getName()),
              expire);

      notificationMap.put(DOMAIN_MESSAGE, domainMessage);
    }

    String subject = domain.getName() + SUBJECT_TEMPLATE + CERTBOT_SUCCESS;
    notificationMap.put(SUBJECT, subject);
    return notificationMap;
  }

  private MimeMessage createMimeMessage(
      Session session, String subject, Address from, Address[] to, String message)
      throws ServiceException {

    try {
      MimeMessage mm = new MimeMessage(session);
      mm.setFrom(from);
      mm.setSender(from);
      mm.setRecipients(RecipientType.TO, to);
      mm.setSubject(subject);
      mm.setText(message);
      mm.saveChanges();

      return mm;

    } catch (MessagingException e) {
      throw ServiceException.FAILURE("Unable to create MimeMessage: ", e);
    }
  }

  private Address[] convert(String[] addresses) throws ServiceException {
    try {
      String addressList = String.join(", ", addresses);
      return InternetAddress.parse(addressList);
    } catch (AddressException e) {
      throw ServiceException.FAILURE("Unable to parse address list", e);
    }
  }

  private Address convert(String address) throws ServiceException {
    try {
      return new InternetAddress(address);
    } catch (AddressException e) {
      throw ServiceException.FAILURE("Unable to parse address", e);
    }
  }
}
