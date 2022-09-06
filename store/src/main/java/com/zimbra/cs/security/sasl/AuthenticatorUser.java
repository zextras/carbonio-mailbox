// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.security.sasl;

import com.zimbra.common.util.Log;
import java.io.IOException;

/** Implemented by user of the Authenticator class. */
public interface AuthenticatorUser {
  /**
   * Returns the protocol for the authenticator (i.e. pop3, imap)
   *
   * @return the authenticator protocol
   */
  String getProtocol();

  /**
   * Sends an error response to the client indicating that a bad request has been received.
   *
   * @param s the error message to be sent
   * @throws IOException if an I/O error occurred
   */
  void sendBadRequest(String s) throws IOException;

  /**
   * Sends a generic error response to the client indicating that authentication has failed.
   *
   * @throws IOException if an I/O error occurred
   */
  void sendFailed() throws IOException;

  /**
   * Sends an error response to the client indicating that authentication has failed.
   *
   * @param msg the error message to be sent
   * @throws IOException if an I/O error occurred
   */
  void sendFailed(String msg) throws IOException;

  /**
   * Sends a generic response to the client indicating that authentication was successful.
   *
   * @throws IOException if an I/O error has occurred
   */
  void sendSuccessful() throws IOException;

  /**
   * Sends a continuation response to the client.
   *
   * @param s the continuation message
   * @throws IOException if an I/O error has occurred
   */
  void sendContinuation(String s) throws IOException;

  /**
   * Authenticates the user with the server.
   *
   * @param authorizationId the authorization id for the user, or null if none
   * @param authenticationId the authentication id for the user (required)
   * @param password the user password, or null if none
   * @param auth the Authenticator performing the authentication
   * @return true if the user was authenticated, false otherwise
   * @throws IOException if an I/O error occurred
   */
  boolean authenticate(
      String authorizationId, String authenticationId, String password, Authenticator auth)
      throws IOException;

  /**
   * Returns the logger to be used by the authenticator.
   *
   * @return the Log to be used
   */
  Log getLog();

  /**
   * Returns true if SSL encryption is enabled for the connection.
   *
   * @return true if SSL encryption has been enabled, false otherwise
   */
  boolean isSSLEnabled();

  /**
   * Returns true if plain username/password login is permitted without SSL encryption active.
   *
   * @return true if login is permitted without SSL encryption, false otherwise
   */
  boolean allowCleartextLogin();

  /**
   * Returns true if plain username/password login is permitted without SSL encryption active.
   *
   * @return true if login is permitted without SSL encryption, false otherwise
   */
  boolean isGssapiAvailable();
}
