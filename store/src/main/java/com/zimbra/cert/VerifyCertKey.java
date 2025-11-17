// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cert;

import com.zimbra.cert.util.ProcessStarter;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.CertMgrConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;

/**
 * Admin Handler class to verify provided private key and certificate using zmcertmgr verifycrt. It
 * verifies the crt and the key content as well the ca. At the moment the ca is the same as the crt,
 * so the certificate is expected to include the chain.
 *
 * <p>NOTE: The provided content is formatted by replacing spaces with newlines, for cases such as
 * copy-paste from a terminal. This should be taken in consideration when calling the API, as
 * zmcertmgr may fail if the content is not as expected. It has been tested though that spaces
 * before or after headers do not affect the result. Additional spaces in the base64 content instead
 * cause the verification to fail.
 */
public class VerifyCertKey extends AdminDocumentHandler {

  static final String VERIFY_CERT_COMMAND = "verifycrt";
  static final String CERT_MGR = "/opt/zextras/bin/zmcertmgr";
  static final String CERT_TYPE_COMM = "comm";

  private final ProcessStarter processStarter;
  private final Supplier<String> basePathSupplier;

  public VerifyCertKey(ProcessStarter baseProcess, Supplier<String> basePathSupplier) {
    this.processStarter = baseProcess;
    this.basePathSupplier = basePathSupplier;
  }

  /**
   * Handles the request.
   *
   * @param request {@link Element} representation of {@link
   *     com.zimbra.soap.admin.message.VerifyCertKeyRequest}
   * @param context request context
   * @return {@link Element} representation of {@link
   *     com.zimbra.soap.admin.message.VerifyCertKeyResponse}
   * @throws ServiceException
   */
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element response = zsc.createElement(CertMgrConstants.VERIFY_CERTKEY_RESPONSE);
    String certBuffer = request.getAttribute(CertMgrConstants.E_cert);
    String pvtKeyBuffer = request.getAttribute(CertMgrConstants.A_privkey);
    String certificateChain = request.getAttribute(CertMgrConstants.A_ca, "");
    if (Objects.isNull(certificateChain) || Objects.equals("", certificateChain)) {
      certificateChain = certBuffer;
    }

    boolean verifyResult = false;
    final String tmpPath = basePathSupplier.get();
    final String keyFile = tmpPath + ZimbraCertMgrExt.COMM_CRT_KEY_FILE_NAME;
    final String certFile = tmpPath + ZimbraCertMgrExt.COMM_CRT_FILE_NAME;
    final String caFile = tmpPath + ZimbraCertMgrExt.COMM_CRT_CA_FILE_NAME;

    try {
      // replace the space character with '\n'
      String sanitizedCrt = formatValidContent(certBuffer);
      String sanitizedPvtKey = formatValidContent(pvtKeyBuffer);
      String sanitizedCertChain = formatValidContent(certificateChain);

      if (sanitizedCrt.length() == 0 || sanitizedPvtKey.length() == 0) {
        response.addAttribute(CertMgrConstants.A_verifyResult, "invalid");
        return response;
      }

      // store pvt key, crt and ca in a temporary file
      byte[] crtBytes = sanitizedCrt.getBytes();
      byte[] crtChainBytes = sanitizedCertChain.getBytes();
      byte[] pvtKeyBytes = sanitizedPvtKey.getBytes();

      File comm_path = new File(tmpPath);
      if (!comm_path.exists()) {
        if (!comm_path.mkdirs()) {
          throw ServiceException.FAILURE("Cannot create dir " + comm_path.getAbsolutePath(), null);
        }
      } else if (!comm_path.isDirectory()) {
        throw ServiceException.FAILURE(
            "Path is not a directory: " + comm_path.getAbsolutePath(), null);
      }

      ByteUtil.putContent(certFile, crtBytes);
      ByteUtil.putContent(caFile, crtChainBytes);
      ByteUtil.putContent(keyFile, pvtKeyBytes);

      final Process zmCertMgrProcess =
          processStarter.start(
              CERT_MGR, VERIFY_CERT_COMMAND, CERT_TYPE_COMM, keyFile, certFile, caFile);
      final String verifyCrtKeyCmdResult =
          new String(zmCertMgrProcess.getInputStream().readAllBytes());
      verifyResult = this.verifyCrtCommandResult(verifyCrtKeyCmdResult);
      ZimbraLog.security.info(
          " GetVerifyCertResponse:"
              + verifyResult
              + ". Command result was: "
              + verifyCrtKeyCmdResult);

      File comm_priv = new File(keyFile);
      if (!comm_priv.delete()) {
        throw new SecurityException("Deleting commercial private key file failed.");
      }
      File comm_cert = new File(certFile);
      if (!comm_cert.delete()) {
        throw new SecurityException("Deleting commercial certificate file failed.");
      }
      File comm_ca = new File(caFile);
      if (!comm_ca.delete()) {
        throw new SecurityException("Deleting commercial CA certificate file failed.");
      }

      if (!comm_path.delete()) {
        throw new SecurityException("Deleting directory of certificate/key failed.");
      }

    } catch (SecurityException se) {
      ZimbraLog.security.error("File(s) of commercial certificates/prvkey was not deleted", se);
    } catch (IOException ioe) {
      throw ServiceException.FAILURE(
          "IOException occurred while running cert verification command", ioe);
    }

    response.addAttribute(CertMgrConstants.A_verifyResult, verifyResult);
    return response;
  }

  /**
   * Formats web client input to valid private key and crt content. The method replaces spaces with
   * new lines but preserves header structure.
   *
   * @param input input string
   * @return formatted string with headers and content separated by new lines
   */
  public String formatValidContent(String input) {
    // splits on every "-----" followed or preceded by spaces and removes spaces
    final String splitRegex = "(\\s)(?=(-----))|(?<=(-----))(\\s)";
    final StringBuilder result = new StringBuilder();
    for (String line : input.split(splitRegex)) {
      if (line.contains("BEGIN")) {
        result.append(line).append(System.lineSeparator());
        continue;
      }
      if (line.contains("END")) {
        result.append(System.lineSeparator()).append(line).append(System.lineSeparator());
        continue;
      }
      result.append(line.replaceAll("(\\s)", System.lineSeparator()));
    }
    return result.toString();
  }

  /**
   * Parses the command output and checks if it was successful based on displayed information.
   *
   * @param commandResult the received command result
   * @return if command was successful
   */
  private boolean verifyCrtCommandResult(String commandResult) {
    return !StringUtils.containsIgnoreCase(commandResult, "error");
  }
}
