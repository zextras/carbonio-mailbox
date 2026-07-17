// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.testcontainers;

public interface RedisResource {

  String getHost();

  int getPort();

  void close();
}
