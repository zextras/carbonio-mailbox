// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

public class ArrayUtil {

    /**
     * Returns the first element, or <tt>null</tt> if the array
     * is <tt>null</tt> or empty.
     */
    public static <E> E getFirstElement(E[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[0];
    }
}
