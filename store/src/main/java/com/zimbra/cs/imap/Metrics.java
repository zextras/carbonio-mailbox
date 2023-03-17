/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zimbra.cs.imap;

/**
 * CLass for IMAP metrics naming.
 *
 * @since 23.4.0
 * @author davidefrison
 */
public class Metrics {

  // Commands
  static final String IMAP_EXEC = "imap_exec";
  static final String IMAP_COMMAND_TAG = "command";

  // Connections
  static final String IMAP_CONN = "imap_conn";
  static final String IMAP_SSL_CONN = "imap_ssl_conn";

  // Threads
  static final String IMAP_THREADS = "imap_threads";
  static final String IMAP_SSL_THREADS = "imap_ssl_threads";
}
