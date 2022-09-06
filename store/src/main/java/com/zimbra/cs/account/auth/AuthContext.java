// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth;

public class AuthContext {
  /*
   * Originating client IP address.
   * Present in context for SOAP, IMAP, POP3, and http basic authentication.
   *
   * type: String
   */
  public static final String AC_ORIGINATING_CLIENT_IP = "ocip";

  /*
   * Remote address as seen by ServletRequest.getRemoteAddr()
   * Present in context for SOAP, IMAP, POP3, and http basic authentication.
   *
   * type: String
   */
  public static final String AC_REMOTE_IP = "remoteip";

  /*
   * Account name passed in to the interface.
   * Present in context for SOAP and http basic authentication.
   *
   * type: String
   */
  public static final String AC_ACCOUNT_NAME_PASSEDIN = "anp";

  /*
   * User agent sending in the auth request.
   *
   * type: String
   */
  public static final String AC_USER_AGENT = "ua";

  /*
   * Whether the auth request came to the admin port and attempting
   * to acquire an admin auth token
   *
   * type: Boolean
   */
  public static final String AC_AS_ADMIN = "asAdmin";

  /*
   *
   */
  public static final String AC_AUTHED_BY_MECH = "authedByMech";

  /*
   * Protocol from which the auth request went in.
   *
   * type: AuthContext.Protocol
   */
  public static final String AC_PROTOCOL = "proto";

  /*
   * Unique device ID, used for identifying trusted mobile devices.
   */
  public static final String AC_DEVICE_ID = "did";

  public enum Protocol {
    client_certificate,
    http_basic,
    http_dav,
    im,
    imap,
    pop3,
    soap,
    spnego,
    zsync,

    // for internal use only
    test;
  };
}
