// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple counter that maintains the count of unique values passed into the
 * {@link #increment} and {@link #decrement} methods.
 *
 * @author bburtin
 */
public class ValueCounter<E> {

    private Map<E, Integer> mValues = new HashMap<>();

    public void increment(E value) {
        increment(value, 1);
    }

    public void decrement(E value) {
        increment(value, -1);
    }

    public void increment(E value, int delta) {
        Integer count = mValues.get(value);
        if (count == null) {
            count = delta;
        } else {
            count = count.intValue() + delta;
        }
        mValues.put(value, count);
    }

    public int getCount(Object value) {
        Integer count = mValues.get(value);
        if (count == null) {
            return 0;
        }
        return count;
    }

    public Iterator<E> iterator() {
        return mValues.keySet().iterator();
    }

    public int size() {
        return mValues.size();
    }

    public int getTotal() {
        int total = 0;
        Iterator<E> i = iterator();
        while (i.hasNext()) {
            total = total + getCount(i.next());
        }
        return total;
    }

    public void clear() {
        mValues.clear();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator<E> i = iterator();
        while (i.hasNext()) {
            if (buf.length() != 0) {
                buf.append(", ");
            }
            Object value = i.next();
            buf.append(value).append(": ").append(getCount(value));
        }
        return buf.toString();
    }
}
