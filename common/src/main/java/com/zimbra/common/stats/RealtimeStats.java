// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.stats;

import com.zimbra.common.util.ArrayUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This implementation of <code>Accumulator</code> is used to retrieve the current value of a
 * statistic. When a system component initializes, it calls {@link
 * #addCallback(RealtimeStatsCallback)}
 *
 * @author bburtin
 */
public class RealtimeStats implements Accumulator<Integer> {

  private List<String> mNames;
  private List<RealtimeStatsCallback> mCallbacks = new ArrayList<RealtimeStatsCallback>();

  public RealtimeStats(String[] names) {
    if (ArrayUtil.isEmpty(names)) {
      throw new IllegalArgumentException("names cannot be null or empty");
    }
    mNames = new ArrayList<>();
    mNames.addAll(Arrays.asList(names));
  }

  public void addName(String name) {
    mNames.add(name);
  }

  public void addCallback(RealtimeStatsCallback callback) {
    mCallbacks.add(callback);
  }

  public List<String> getNames() {
    return mNames;
  }

  public Map<String, Integer> getData() {
    final Map<String, Integer> result = new HashMap<>();
    for (RealtimeStatsCallback callback : mCallbacks) {
      final Map<String, Integer> statData = callback.getStatData();
      if (!(Objects.isNull(statData))) {
        result.putAll(statData);
      }
    }
    return result;
  }

  public void reset() {}
}
