// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import java.sql.SQLException;
import java.util.Map;

public class MariaDB extends Db {

    private static final Map<Db.Error, Integer> ERROR_CODES = Map.of(
        Db.Error.DEADLOCK_DETECTED, 1213,
        Db.Error.DUPLICATE_ROW, 1062,
        Db.Error.FOREIGN_KEY_NO_PARENT, 1216,
        Db.Error.FOREIGN_KEY_CHILD_EXISTS, 1451,
        Db.Error.NO_SUCH_DATABASE, 1146,
        Db.Error.NO_SUCH_TABLE, 1146,
        Db.Error.TABLE_FULL, 1114);

    @Override
    boolean compareError(SQLException e, Db.Error error) {
        Integer code = ERROR_CODES.get(error);
        return code != null && e.getErrorCode() == code;
    }
}
