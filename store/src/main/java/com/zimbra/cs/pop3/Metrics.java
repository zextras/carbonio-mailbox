/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zimbra.cs.pop3;

/**
 * CLass for POP3 metrics naming.
 *
 * @since 23.4.0
 * @author davidefrison
 */
public class Metrics {
  // Commands
  static final String POP_EXEC = "pop_exec";
  static final String POP_COMMAND_TAG = "command";

  // Connections
  static final String POP_CONN = "pop_conn";
  static final String POP_SSL_CONN = "pop_ssl_conn";

  // Threads
  static final String POP_THREADS = "pop_threads";
  static final String POP_SSL_THREADS = "pop_ssl_threads";
}
