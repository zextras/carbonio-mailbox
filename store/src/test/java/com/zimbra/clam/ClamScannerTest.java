// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.clam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.MalformedURLException;
import org.junit.jupiter.api.Test;

public class ClamScannerTest {

  @Test
  public void testSetURLValidURL() {
    String validUrl = "clam://example.com:1234/";
    ClamScanner scanner = new ClamScanner();

    try {
      scanner.setURL(validUrl);
    } catch (MalformedURLException e) {
      fail("Unexpected MalformedURLException occurred");
    }
  }

  public void parseGoodUrl(String url, String expectedHost, int expectedPort) {
    ClamScanner scanner = new ClamScanner();
    try {
      scanner.setURL(url);
    } catch (MalformedURLException e) {
      fail("failed to parse [" + url + "]");
    }
    assertTrue(scanner.isEnabled());
    assertEquals(expectedHost, scanner.getClamAVClientHostname());
    assertEquals(expectedPort, scanner.getClamAVClientPort());
  }

  public void parseBadUrl(String url) {
    ClamScanner scanner = new ClamScanner();
    try {
      scanner.setURL(url);
      fail("expected parse error from url [" + url + "]");
    } catch (MalformedURLException e) {
      // expected - bad url
    } catch (IllegalStateException e) {
      // expected - acceptable OK url, but no port or not clam
    }
  }

  public void createAndParseGoodUrl(String host, int port) {
    parseGoodUrl("clam://" + host + ":" + port, host, port);
  }

  @Test
  public void localhostPort() {
    createAndParseGoodUrl("localhost", 12345);
  }

  @Test
  public void hostnamePort() {
    createAndParseGoodUrl("clam.example.com", 3452);
  }

  @Test
  public void ipv4() {
    createAndParseGoodUrl("129.151.2.13", 9213);
  }

  @Test
  public void ipv6() {
    parseGoodUrl(
        "clam://[2001:db8:85a3:8d3:1319:8a2e:370:7348]:443",
        "2001:db8:85a3:8d3:1319:8a2e:370:7348",
        443);
  }

  @Test
  public void trailingSlashes() {
    parseGoodUrl("clam://localhost:12345/", "localhost", 12345);
    parseGoodUrl("clam://192.151.121.1:999/", "192.151.121.1", 999);
    parseGoodUrl("clam://clam.example.com:4212/", "clam.example.com", 4212);
    parseGoodUrl(
        "clam://[2001:db8:85a3:8d3:1319:8a2e:370:7348]:876",
        "2001:db8:85a3:8d3:1319:8a2e:370:7348",
        876);
    parseGoodUrl("clam://10.137.245.108:3310/", "10.137.245.108", 3310);
    parseGoodUrl("clam://zqa-363.eng.zimbra.com:3310/", "zqa-363.eng.zimbra.com", 3310);
    parseGoodUrl("clam://[fe80::250:56ff:fea5:151e]:3310/", "fe80::250:56ff:fea5:151e", 3310);
    parseGoodUrl("clam://localhost:3310/", "localhost", 3310);
  }

  @Test
  public void badUrls() {
    parseBadUrl("foo");
    parseBadUrl("foo://localhost:1231");
    parseBadUrl("clam://localhost:99999"); // out of port range
    parseBadUrl("clam:///"); // host/ip required
    parseBadUrl("clam://localhost/"); // port required
    parseBadUrl("clam://2001:db8:85a3:8d3:1319:8a2e:370:7348:1234"); // ipv6 brackets required
  }

  @Test
  public void testSanitizedUrl_validUrl() {
    String validUrl = "clam://example.com:1234";
    try {
      String sanitizedUrl = ClamScanner.sanitizedUrl(validUrl);
      assertEquals(validUrl, sanitizedUrl);
    } catch (MalformedURLException e) {
      fail("Should not throw an exception for a valid URL");
    }
  }

  @Test
  public void testSanitizedUrl_invalidProtocol() {
    String invalidUrl = "http://example.com";
    try {
      ClamScanner.sanitizedUrl(invalidUrl);
      fail("Should throw MalformedURLException for an invalid protocol");
    } catch (MalformedURLException e) {
      assertEquals("Invalid clamd URL: " + invalidUrl, e.getMessage());
    }
  }

  @Test
  public void testSanitizedUrl_invalidPort() {
    String invalidUrl = "clam://example.com:99999";
    try {
      ClamScanner.sanitizedUrl(invalidUrl);
      fail("Should throw MalformedURLException for an invalid port");
    } catch (MalformedURLException e) {
      assertEquals("Invalid or out of bound port specified in URL: " + invalidUrl, e.getMessage());
    }
  }

  @Test
  public void testSanitizedUrl_invalidPortFormat() {
    String invalidUrl = "clam://example.com:abc";
    try {
      ClamScanner.sanitizedUrl(invalidUrl);
      fail("Should throw MalformedURLException for an invalid port format");
    } catch (MalformedURLException e) {
      assertEquals(
          "Invalid port specified in URL: " + invalidUrl + ": For input string: \"abc\"",
          e.getMessage());
    }
  }
}
