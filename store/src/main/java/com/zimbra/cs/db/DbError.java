// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import java.sql.SQLException;

public enum DbError {
    DUPLICATE_ROW(1062),
    FOREIGN_KEY_CHILD_EXISTS(1451),
    NO_SUCH_TABLE(1146),
    TABLE_FULL(1114);

    private final int code;

    DbError(int code) {
        this.code = code;
    }

    public boolean matches(SQLException e) {
        return e.getErrorCode() == code;
    }
}
