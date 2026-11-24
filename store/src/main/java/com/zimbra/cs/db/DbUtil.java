// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

public final class DbUtil {

    private DbUtil() {
    }

    public static String suitableNumberOfVariables(int size) {
        StringBuilder sb = new StringBuilder(" (");
        for (int i = 0; i < size; i++)
            sb.append(i == 0 ? "?" : ", ?");
        return sb.append(")").toString();
    }

    public static String whereIn(String column, boolean in, int size) {
        return column + (in ? " IN" : " NOT IN") + suitableNumberOfVariables(size);
    }

    public static String whereIn(String column, int size) {
        return whereIn(column, true, size);
    }

    public static String whereNotIn(String column, int size) {
        return whereIn(column, false, size);
    }
}
