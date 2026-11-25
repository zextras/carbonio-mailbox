// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.db;

import com.zimbra.common.localconfig.LC;

public record MariaDBConfig(String host, int port, String database) implements DbConfig {

  public static MariaDBConfig fromLocalConfig() {
    return new MariaDBConfig(LC.mysql_bind_address.value(), LC.mysql_port.intValue(), "zimbra");
  }
}
