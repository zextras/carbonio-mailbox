// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.Collection;

public class ArrayUtil {

    /** Destructively reverses an array of objects and returns the altered passed-in array. */
    public static <A> A[] reverse(A[] array) {
        if (array != null && array.length > 1) {
            for (int i = 0, length = array.length; i < length / 2; i++) {
                A temp = array[i];
                array[i] = array[length - i - 1];
                array[length - i - 1] = temp;
            }
        }
        return array;
    }

    /** Destructively reverses an array of ints and returns the altered passed-in array. */
    public static int[] reverse(int[] array) {
        if (array != null && array.length > 1) {
            for (int i = 0, length = array.length; i < length / 2; i++) {
                int temp = array[i];
                array[i] = array[length - i - 1];
                array[length - i - 1] = temp;
            }
        }
        return array;
    }

    /** Destructively reverses an array of bytes and returns the altered passed-in array. */
    public static byte[] reverse(byte[] array) {
        if (array != null && array.length > 1) {
            for (int i = 0, length = array.length; i < length / 2; i++) {
                byte temp = array[i];
                array[i] = array[length - i - 1];
                array[length - i - 1] = temp;
            }
        }
        return array;
    }

    /** Destructively reverses an array of chars and returns the altered passed-in array. */
    public static char[] reverse(char[] array) {
        if (array != null && array.length > 1) {
            for (int i = 0, length = array.length; i < length / 2; i++) {
                char temp = array[i];
                array[i] = array[length - i - 1];
                array[length - i - 1] = temp;
            }
        }
        return array;
    }

    /** Converts a {@link Collection} of Bytes into a byte[] array. */
    public static byte[] toByteArray(Collection<Byte> c) {
        int pos = 0;
        byte[] byteArray = new byte[c.size()];
        for (Byte id : c)
            byteArray[pos++] = id;
        return byteArray;
    }

    /** Converts a {@link Collection} of Integers into an int[] array. */
    public static int[] toIntArray(Collection<Integer> c) {
        int[] intArray = new int[c.size()];
      int pos = 0;
      for (Integer id : c)
            intArray[pos++] = id;
        return intArray;
    }

    /** Returns <code>true</code> if the given array is <tt>null</tt> or empty. */
    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }
    
    public boolean byteArrayContains(byte[] array, byte val) {
        for (int i = array.length-1; i >= 0; i--) {
            if (array[i] == val) {
                return true;
            }
        }
        return false;
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
