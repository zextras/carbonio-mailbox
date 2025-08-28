package com.zimbra.cert.util;

import com.zimbra.common.service.ServiceException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper util class for X509 certificate operations. It provides a way to access all needed
 * attributes of X.509 certificate using a standard java.security.cert package.
 *
 * @author Yuliya Aheeva
 * @since 23.2.0
 */
public class X509CertificateParser {
  private static final String CERT_TYPE = "X.509";

  private X509CertificateParser() {
    throw new RuntimeException("Utility class cannot be instantiated.");
  }

  /**
   * Generates a certificate object and initializes it with the data read from the input stream
   * inStream.
   *
   * @param certificate byte array certificate representation.
   * @return {@link X509Certificate} X509Certificate object.
   * @throws ServiceException if an error occurs during certificate generation.
   */
  public static X509Certificate generateCertificate(byte[] certificate) throws ServiceException {
    try (InputStream inStream = new ByteArrayInputStream(certificate)) {
      CertificateFactory cf = CertificateFactory.getInstance(CERT_TYPE);
      return (X509Certificate) cf.generateCertificate(inStream);
    } catch (IOException | CertificateException e) {
      throw ServiceException.FAILURE("Failure on generating certificate: " + e.getMessage());
    }
  }

  /**
   * Gets subject alternative names. It is standalone because could throw an exception.
   *
   * @param certificate {@link X509Certificate} X509Certificate object.
   * @return collection of lists with subject alternative names.
   * @throws ServiceException if an error occurs during subject alt names parsing.
   */
  public static Collection<List<?>> getSubjectAltNames(X509Certificate certificate)
      throws ServiceException {
    try {
      return certificate.getSubjectAlternativeNames();
    } catch (CertificateParsingException e) {
      throw ServiceException.FAILURE(
          "Failure on getting subject alternative names: " + e.getMessage());
    }
  }

  /**
   * Util method to create a string representation of subject alt names collection.
   *
   * @param altNamesList collection of lists with subject alternative names.
   * @return String representation of subject alt names.
   */
  public static String parseSubjectAltNames(Collection<List<?>> altNamesList) {
    return altNamesList
        .stream()
        .filter(list -> list.size() >= 2)
        .map(list -> list.get(1).toString())
        .collect(Collectors.joining(", "));
  }
}
