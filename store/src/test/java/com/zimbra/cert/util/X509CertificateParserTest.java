package com.zimbra.cert.util;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cert.util.X509CertificateParser;
import com.zimbra.common.service.ServiceException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;


public class X509CertificateParserTest {

  @Test
  public void shouldGenerateCertFromValidData() throws Exception {
    final String certSource =
            "-----BEGIN CERTIFICATE-----\n"
            + "MIIFPjCCBCagAwIBAgISAyeF5ryS59TmpV5xTYquviaNMA0GCSqGSIb3DQEBCwUA\n"
            + "MDIxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQswCQYDVQQD\n"
            + "EwJSMzAeFw0yMjExMjIxNjI2MDFaFw0yMzAyMjAxNjI2MDBaMCcxJTAjBgNVBAMT\n"
            + "HHdlYm1haWwtYWNtZS5kZW1vLnpleHRyYXMuaW8wggEiMA0GCSqGSIb3DQEBAQUA\n"
            + "A4IBDwAwggEKAoIBAQDdo/18KgJWki8yGVKC+cSV1wUJ9RKuOzLS7FdHd0RIt0H6\n"
            + "lT9UriqWd2Yoc49wGJnvs2JY9K8oS3qj1VCAMMUrLBPt6NXPrPAU9uCG7p4e1R8o\n"
            + "jgJq1Wumhxaua+btaPnkEJefGge0zuNDP0aEusK+zH9mCxgMqTDcbkjqaI6FFxFb\n"
            + "++HTMe/c4quAput2FzpmUY5loLsomcd4eq28pWP4zRSli31HxEBFNgk6V6zVKAGl\n"
            + "d92jrYNXNzNVQE3zAa5Ds9VyJq4Z6/8/Irl9lG7GKeF9c4TIBzPUvoHQOcbyC3LU\n"
            + "1mlJtbkHiouGwFkSgJGH0NPmZzj8eA5VNhEjm+GVAgMBAAGjggJXMIICUzAOBgNV\n"
            + "HQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwGA1Ud\n"
            + "EwEB/wQCMAAwHQYDVR0OBBYEFGHHL5z1LwZqLDE1YHc76YWctfCuMB8GA1UdIwQY\n"
            + "MBaAFBQusxe3WFbLrlAJQOYfr52LFMLGMFUGCCsGAQUFBwEBBEkwRzAhBggrBgEF\n"
            + "BQcwAYYVaHR0cDovL3IzLm8ubGVuY3Iub3JnMCIGCCsGAQUFBzAChhZodHRwOi8v\n"
            + "cjMuaS5sZW5jci5vcmcvMCcGA1UdEQQgMB6CHHdlYm1haWwtYWNtZS5kZW1vLnpl\n"
            + "eHRyYXMuaW8wTAYDVR0gBEUwQzAIBgZngQwBAgEwNwYLKwYBBAGC3xMBAQEwKDAm\n"
            + "BggrBgEFBQcCARYaaHR0cDovL2Nwcy5sZXRzZW5jcnlwdC5vcmcwggEEBgorBgEE\n"
            + "AdZ5AgQCBIH1BIHyAPAAdwB6MoxU2LcttiDqOOBSHumEFnAyE4VNO9IrwTpXo1Lr\n"
            + "UgAAAYSgYBDwAAAEAwBIMEYCIQDCZVS/DDy0C2R9IrY1ScTonqeedlI7ZjXo7R5R\n"
            + "ZunbhwIhAMwCpmEsi0NUx1PhhNUwP3x85bRUEq4S+w/0/Nwj1I0tAHUAtz77JN+c\n"
            + "Tbp18jnFulj0bF38Qs96nzXEnh0JgSXttJkAAAGEoGAS9AAABAMARjBEAiAbZ6Ag\n"
            + "fsdche4bRXR6G/CAfNk6sRyA5W3ThLWyn9+vfAIgdYMflSb3bZyQbuhKB4zt3s1O\n"
            + "BXrSDRXXrPYnF5rMWhEwDQYJKoZIhvcNAQELBQADggEBAJXIkzjX3thAZyBSj9lr\n"
            + "It8ipidZNghgI27d7cqntkdsIDWQYyRRbS05GObMoAEiQnFti8EGnqQqEFkgOAj1\n"
            + "9wXjSQdOGJjzm+giMhci2VMBC4N/7fWOSxWQ+jmVsYOLUFFb8VBuhWpwU5VHOAcv\n"
            + "6cHuQKNOw8cYm9BFtdKqeexcTkflpWVsNK/CxIiK3cf9/p43MLO73AdGPT2g6DEk\n"
            + "f+6Ay6pRLopibLZgGWqqBxevG9Pag5VN1CXcR5cu431Ic5l6NLJp58ED9qw8rf/7\n"
            + "2gAhU3H9TC8UfXXnezzd5PSRjFAi05ciYyUQv+jtAN/U/wGDpiZ1QySvq0p6NC0J\n"
            + "f+g=\n"
            + "-----END CERTIFICATE-----\n";
    final X509Certificate x509Cert =
        X509CertificateParser.generateCertificate(certSource.getBytes());
    assertEquals("CN=webmail-acme.demo.zextras.io", x509Cert.getSubjectX500Principal().getName());
    assertEquals("CN=R3,O=Let's Encrypt,C=US", x509Cert.getIssuerX500Principal().getName());
  }

  @Test
  public void shouldThrowExceptionFromInvalidData() throws Exception {
    final String invalidCertSource =
        "-----BEGIN CERTIFICATE-----\n" + "invalid text\n" + "-----END CERTIFICATE-----\n";

    assertThrows(ServiceException.class, () -> {
      X509CertificateParser.generateCertificate(invalidCertSource.getBytes());
    });
  }

  @Test
  public void shouldParseAltNamesFromLists() {
    final Collection<List<?>> listsOfAltNames = List.of(List.of(2, "webmail-acme.demo.zextras.io"));
    assertEquals(
        "webmail-acme.demo.zextras.io",
        X509CertificateParser.parseSubjectAltNames(listsOfAltNames));
  }

  @Test
  public void shouldCreateEmptyStringIfNoAltNames() {
    final Collection<List<?>> listsOfAltNames = List.of(List.of(2));
    assertEquals("", X509CertificateParser.parseSubjectAltNames(listsOfAltNames));
  }
}
