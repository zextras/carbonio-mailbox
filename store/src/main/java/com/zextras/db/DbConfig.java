// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.db;

public interface DbConfig {

  String host();

  int port();

  String database();

  default String rootUrl() {
    String address = host().indexOf(':') > -1 ? "[" + host() + "]" : host();
    return "jdbc:mysql://" + address + ":" + port() + "/";
  }

  default String jdbcUrl() {
    return rootUrl() + database();
  }
}
