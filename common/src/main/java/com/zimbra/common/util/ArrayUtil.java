// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;

public class ArrayUtil {

  /** Converts a {@link Collection} of Integers into an int[] array. */
    public static int[] toIntArray(Collection<Integer> c) {
        return Ints.toArray(c);
    }

    /** Returns <code>true</code> if the given array is <tt>null</tt> or empty. */
    public static boolean isEmpty(Object[] array) {
        return ArrayUtils.isEmpty(array);
    }

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
