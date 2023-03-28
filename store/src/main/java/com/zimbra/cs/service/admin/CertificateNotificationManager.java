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
 * CertificateNotificationManager is intermediate between remote execution and mailSender.
 * Helps to create mimeMessages based on domain and remote execution result in order to notify
 * domain and global recipients about issue certificate request.
 *
 * @author Yuliya Aheeva
 * @since 23.5.0
 */
public class CertificateNotificationManager {
  public static final String GLOBAL_FROM = "globalFrom";
  public static final String GLOBAL_TO = "globalTo";
  public static final String GLOBAL_MESSAGE = "globalMessage";

  public static final String DOMAIN_FROM = "domainFrom";
  public static final String DOMAIN_TO = "domainTo";
  public static final String DOMAIN_MESSAGE = "domainMessage";

  public static final String DOMAIN_NAME = "domainName";
  public static final String SUBJECT_RESULT = "subjectResult";
  public static final String SUBJECT_TEMPLATE = " certification request - ";

  public static final String SYSTEM_FAILURE = "system failure";

  public static final String CERTBOT_SUCCESS = "Certificate Authority success";
  public static final String CERTBOT_FAILURE = "Certificate Authority failure";

  public static final String RESULT = "result";
  public static final String HEADER = "The certification result is: ";
  public static final String SUCCESS_RESULT = "SUCCESS\n";
  public static final String FAILURE_RESULT = "FAILURE\n";

  public static final String FAIL = "fail";
  public static final String FAILURE_DOMAIN_NOTIFICATION_TEMPLATE =
      "Hint: Common use cases that cause this behavior are:"
          + "<ul>"
          + "<li>Your domain public service hostname or virtual hostname is wrong. Make sure that "
          + "your domain public service hostname and virtual hostname is entered and saved "
          + "correctly.\n</li>"
          + "<li>Your DNS A/AAAA record is wrong. Make sure the DNS A/AAAA record for the "
          + "domain is entered and saved correctly.\n</li>"
          + "<li>Your IP address is wrong or private. Make sure the DNS A/AAAA record contains "
          + "the right public IP address.\n</li>"
          + "<li>Your DNS record isn’t propagated yet. Go to https://dnsmap.io to check if it’s "
          + "propagated.\n</li>"
          + "</ul>";

  public static final String RECEIVED = "received certificate";
  public static final String SUCCESS_DOMAIN_NOTIFICATION_TEMPLATE =
      "The certificate was successfully received.\n"
          + "Please NOTE  that the Certificate and Key will be available after the proxy reload.\n"
          + "You’ll be able to download them from the Certificate section in the admin interface.\n"
          + "\n"
          + "The files will be automatically updated when the certificate renews.\n";

  /**
   * Notifies domain recipients about certificate generation result.
   *
   * @param mbox object of {@link com.zimbra.cs.mailbox.Mailbox} needed for {@link
   *     com.zimbra.cs.mailbox.MailSender} in order to send message
   * @param domain object of {@link com.zimbra.cs.account.Domain} needed to get {@link
   *     com.zimbra.common.account.ZAttrProvisioning} attributes A_carbonioNotificationRecipients
   *     and A_carbonioNotificationFrom
   * @param message a message returned by Certbot acme client
   */
  public static void notify(Mailbox mbox, Domain domain, String outputMessage) {
    String domainName = domain.getName();

    ZimbraLog.rmgmt.info(
        "Issuing LetsEncrypt cert command for domain "
            + domainName
            + " was finished with the following result: "
            + outputMessage);

    try {
      Map<String, Object> notificationMap = createIssueCertNotificationMap(domain, outputMessage);

      MailSender sender = mbox.getMailSender(domain);
      List<MimeMessage> mimeMessageList = createMimeMessageList(sender.getCurrentSession(), notificationMap);

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
   * Creates a map based on domain values and Remote Manager/Certbot output which would be used to
   * create a MimeMessage.
   *
   * @param outputMessage output from RemoteManager/Certbot
   * @return map
   */
  protected static Map<String, Object> createIssueCertNotificationMap(Domain domain, String outputMessage)
      throws ServiceException {

    Provisioning provisioning = Provisioning.getInstance();
    Config config = provisioning.getConfig();

    String globalFrom = Optional.ofNullable(config.getCarbonioNotificationFrom())
        .orElseThrow(() -> ServiceException.FAILURE(
            "Global CarbonioNotificationFrom attribute is not present.", null));
    String[] globalTo = Optional.ofNullable(config.getCarbonioNotificationRecipients())
        .orElseThrow(() -> ServiceException.FAILURE(
            "Global CarbonioNotificationRecipients attribute is not present.", null));

    String domainFrom = Optional.ofNullable(domain.getCarbonioNotificationFrom())
        .orElse(globalFrom);
    String[] domainTo = Optional.ofNullable(domain.getCarbonioNotificationRecipients())
        .orElse(globalTo);

    Map<String, Object> notificationMap = new HashMap<>();

    notificationMap.put(DOMAIN_NAME, domain.getName());

    notificationMap.put(GLOBAL_FROM, globalFrom);
    notificationMap.put(GLOBAL_TO, globalTo);

    // message for global admin contains all output
    notificationMap.put(GLOBAL_MESSAGE, outputMessage);

    // check if a system failure
    boolean isSystemFailure = outputMessage.contains(SYSTEM_FAILURE);
    if (isSystemFailure) {
      notificationMap.put(SUBJECT_RESULT, SYSTEM_FAILURE);
      return notificationMap;
    }

    // check if a certbot failure
    String startParsing = "Simulating";
    String endParsing = "ENDCMD";
    int startIndex = outputMessage.indexOf(startParsing);
    int endIndex = outputMessage.lastIndexOf(endParsing);
    String substringResult = outputMessage.substring(startIndex, endIndex);

    boolean isFailure = substringResult.contains(FAIL);

    if (isFailure) {
      notificationMap.put(SUBJECT_RESULT, CERTBOT_FAILURE);

      String domainMessage =
          String.join("", HEADER, FAILURE_RESULT, FAILURE_DOMAIN_NOTIFICATION_TEMPLATE);
      notificationMap.put(DOMAIN_MESSAGE, domainMessage);
      notificationMap.put(DOMAIN_FROM, domainFrom);
      notificationMap.put(DOMAIN_TO, domainTo);
      return notificationMap;
    }

    // check if a certificate received
    boolean isReceived = substringResult.contains(RECEIVED);
    if (isReceived) {
      String expire = "\n";

      String regex = "\\d{4}-\\d{2}-\\d{2}";
      Matcher matcher = Pattern.compile(regex).matcher(substringResult);
      if (matcher.find()) {
        String expiresTemplate = "This certificate expires on ";
        expire = expire + expiresTemplate + matcher.group() + ".";
      }

      String domainMessage =
          String.join("", HEADER, SUCCESS_RESULT, SUCCESS_DOMAIN_NOTIFICATION_TEMPLATE, expire);

      notificationMap.put(DOMAIN_MESSAGE, domainMessage);
      notificationMap.put(DOMAIN_FROM, domainFrom);
      notificationMap.put(DOMAIN_TO, domainTo);
    }

    // in any other cases (like certificate is not yet due for renewal ... etc)
    // only global admin would be notified
    notificationMap.put(SUBJECT_RESULT, CERTBOT_SUCCESS);
    return notificationMap;
  }

  protected static List<MimeMessage> createMimeMessageList(
      Session session, Map<String, Object> notificationMap) throws ServiceException {

    List<MimeMessage> list = new ArrayList<>();

    String subject =
        notificationMap.get(DOMAIN_NAME) + SUBJECT_TEMPLATE + notificationMap.get(SUBJECT_RESULT);

    Address globalFrom = convert((String) notificationMap.get(GLOBAL_FROM));
    Address[] globalTo = convert((String[]) notificationMap.get(GLOBAL_TO));
    String globalMessage = (String) notificationMap.get(GLOBAL_MESSAGE);

    list.add(createMimeMessage(session, subject, globalFrom, globalTo, globalMessage));

    if (notificationMap.containsKey(DOMAIN_MESSAGE)) {
      Address domainFrom = convert((String) notificationMap.get(DOMAIN_FROM));
      Address[] domainTo = convert((String[]) notificationMap.get(DOMAIN_TO));
      String domainMessage = (String) notificationMap.get(DOMAIN_MESSAGE);

      list.add(createMimeMessage(session, subject, domainFrom, domainTo, domainMessage));
    }

    return list;
  }

  private static MimeMessage createMimeMessage(
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

  private static Address[] convert(String[] addresses) throws ServiceException {
    try {
      String addressList = String.join(", ", addresses);
      return InternetAddress.parse(addressList);
    } catch (AddressException e) {
      throw ServiceException.FAILURE("Unable to parse address list", e);
    }
  }

  private static Address convert(String address) throws ServiceException {
    try {
      return new InternetAddress(address);
    } catch (AddressException e) {
      throw ServiceException.FAILURE("Unable to parse address", e);
    }
  }
}
