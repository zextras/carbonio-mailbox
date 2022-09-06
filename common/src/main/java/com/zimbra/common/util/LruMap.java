// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * {@code Map} implementation that retains a maximum number of entries. Entries are aged out by
 * access order.
 *
 * @param <K> key
 * @param <V> value
 */
@SuppressWarnings("serial")
public class LruMap<K, V> extends LinkedHashMap<K, V> {

  private int maxSize;

  public LruMap(int maxSize) {
    super(16, 0.75f, true);
    this.maxSize = maxSize;
  }

  public int getMaxSize() {
    return maxSize;
  }

  @Override
  protected boolean removeEldestEntry(Entry<K, V> eldest) {
    boolean willRemove = size() > maxSize;
    if (willRemove) {
      willRemove(eldest.getKey(), eldest.getValue());
    }
    return willRemove;
  }

  /** Override to handle an entry that will be removed from the map. */
  protected void willRemove(K key, V value) {}
}
