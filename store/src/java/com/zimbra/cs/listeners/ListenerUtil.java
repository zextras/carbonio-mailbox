// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.listeners;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class ListenerUtil {

    // listeners priority
    public enum Priority {
        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN;
    }

    // Method for sorting the listener map based on priorities
    public static <K, V extends Comparable<V>> Map<K, V> sortByPriority(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {

            public int compare(K k1, K k2) {
                int compare = map.get(k1).compareTo(map.get(k2));
                if (compare == 0)
                    return 1;
                else
                    return compare;
            }
        };

        Map<K, V> sortedByPriorities = new TreeMap<K, V>(valueComparator);
        sortedByPriorities.putAll(map);
        return sortedByPriorities;
    }
}
