/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zimbra.cs.lmtpserver;

/**
 * CLass for LMTP metrics naming.
 *
 * @since 23.4.0
 * @author davidefrison
 */
public class Metrics {

  // Rcvd
  static final String LMTP_RCVD_MSGS = "lmtp_rcvd_msgs";
  static final String LMTP_RCVD_BYTES = "lmtp_rcvd_bytes";
  static final String LMTP_RCVD_RCPT = "lmtp_rcvd_rcpt";

  // Dlvd
  static final String LMTP_DLVD_BYTES = "lmtp_dlvd_bytes";
  static final String LMTP_DLVD_MSGS = "lmtp_dlvd_msgs";

  // Commands
  static final String IMAP_EXEC = "imap_exec";
  static final String IMAP_COMMAND_TAG = "command";

  // Connections
  static final String LMTP_CONN = "lmtp_conn";

  // Threads
  static final String LMTP_THREADS = "lmtp_threads";
}
