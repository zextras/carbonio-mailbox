// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.clam;

import com.google.common.net.HostAndPort;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.clam.client.ClamAVClient;
import com.zimbra.cs.service.mail.UploadScanner;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

/**
 * The ClamScanner class represents a scanner for detecting malware using the ClamAV antivirus
 * engine. It uses {@link ClamAVClient} to send scan requests and retrieve scan results from a
 * remotely running ClamAV server.
 * <p>
 *
 * @author Keshav Bhatt
 * @since 23.7.0
 */
public class ClamScanner extends UploadScanner {

  public static final String PROTOCOL_PREFIX = "clam://";
  private static final Log LOGGER = ZimbraLog.extensions;

  private ClamAVClient mClamAVClient = null;

  /**
   * @param urlArg URL string to be verified and sanitized.
   *               <p>
   * @return sanitized URL {@link String}
   * @throws MalformedURLException if url is malformed
   *
   *                               <p>
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  static String sanitizedUrl(final String urlArg)
      throws MalformedURLException {
    String sanitizedUrl = urlArg;
    if (!urlArg.toLowerCase().startsWith(PROTOCOL_PREFIX)) {
      throw new MalformedURLException("Invalid clamd URL: " + sanitizedUrl);
    }

    if (sanitizedUrl.lastIndexOf('/') > PROTOCOL_PREFIX.length()) {
      sanitizedUrl = sanitizedUrl.substring(0, sanitizedUrl.lastIndexOf('/'));
    }

    if (sanitizedUrl.startsWith(PROTOCOL_PREFIX)) {
      int lastIndex = sanitizedUrl.lastIndexOf(":");
      if (lastIndex > PROTOCOL_PREFIX.length()) {
        String portStr = sanitizedUrl.substring(lastIndex + 1);
        try {
          int port = Integer.parseInt(portStr);
          if (port <= 0 || port > 65535) {
            throw new MalformedURLException(
                "Invalid or out of bound port specified in URL: " + sanitizedUrl);
          }
        } catch (NumberFormatException e) {
          throw new MalformedURLException(
              "Invalid port specified in URL: " + sanitizedUrl + ": " + e.getMessage());
        }
      }
    }
    return sanitizedUrl;
  }

  @Override
  protected Result accept(InputStream inputStream, StringBuffer scanOutput) {
    return performScan(inputStream, scanOutput);
  }

  @Override
  protected Result accept(byte[] bytes, StringBuffer scanOutput) {
    final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    return performScan(bis, scanOutput);
  }

  @Override
  public boolean isEnabled() {
    return mClamAVClient != null;
  }

  /**
   * Wrapper to scan a supplied InputStream with the {@link ClamAVClient}
   *
   * <p>
   *
   * @param data InputStream object
   * @return the result object {@link Result}
   *
   * <p>
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  private Result performScan(InputStream data, StringBuffer scanOutput) {
    if (mClamAVClient == null) {
      return ERROR;
    }
    // SCAN
    try {
      byte[] result;
      result = mClamAVClient.scan(data);
      scanOutput.append(new String(result, StandardCharsets.UTF_8));
      if (ClamAVClient.replyOk(result)) {
        return ACCEPT;
      } else {
        return REJECT;
      }
    } catch (IOException e) {
      LOGGER.error("Could not scan the input", e);
      return ERROR;
    }
  }

  /**
   * Wrapper around createClamAVClient to create a new {@link ClamAVClient} with default
   * configuration.
   * <p>
   *
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  private void createDefaultClamAVClient() {
    createClamAVClient(ClamScannerConfig.FALLBACK_HOSTNAME, ClamScannerConfig.FALLBACK_PORT);
  }

  /**
   * Creates a new ClamAV client with supplied hostname and port. Uses fallback config if supplied
   * argument are not valid.
   * <p>
   *
   * @param hostname hostname in form of String which {@link ClamAVClient} will use to connect to
   *                 clamAV daemon
   * @param port     port number in form of int which {@link ClamAVClient} will use to connect to *
   *                 clamAV daemon
   *                 <p>
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  private void createClamAVClient(String hostname, int port) {

    if (hostname == null) {
      hostname = ClamScannerConfig.FALLBACK_HOSTNAME;
    }
    if (port <= 0) {
      port = ClamScannerConfig.FALLBACK_PORT;
    }

    mClamAVClient = new ClamAVClient(hostname, port, ClamScannerConfig.FALLBACK_TIMEOUT,
        ClamScannerConfig.FALLBACK_CHUNK_SIZE);

    LOGGER.info(String.format(
        "Initialized ClamAVClient with Hostname: %s, Port: %s, Timeout: %s, ChunkSize: %s",
        mClamAVClient.getHostName(),
        mClamAVClient.getPort(),
        mClamAVClient.getTimeout(),
        mClamAVClient.getChunkSize()));
  }

  @Override
  public void setURL(String urlArg) throws MalformedURLException {
    if (urlArg == null) {
      createDefaultClamAVClient();
    } else {

      final String sanitizedUrl;
      sanitizedUrl = sanitizedUrl(urlArg);
      final HostAndPort hostPort = HostAndPort.fromString(
          sanitizedUrl.substring(PROTOCOL_PREFIX.length()));

      createClamAVClient(hostPort.getHost(), hostPort.getPort());
    }
  }

  String getClamAVClientHostname() {
    return mClamAVClient.getHostName();
  }

  int getClamAVClientPort() {
    return mClamAVClient.getPort();
  }
}
