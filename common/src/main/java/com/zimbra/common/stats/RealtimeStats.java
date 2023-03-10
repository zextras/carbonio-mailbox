// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.stats;

import com.zimbra.common.util.ArrayUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    mNames = new ArrayList<String>();
    for (String name : names) {
      mNames.add(name);
    }
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

  public List<Integer> getData() {
    List<Integer> data = new ArrayList<>();

    // Collect stats from all callbacks
    Map<String, Integer> callbackResults = new HashMap<>();
    for (RealtimeStatsCallback callback : mCallbacks) {
      Map<String, Integer> callbackData = callback.getStatData();
      if (callbackData != null) {
        callbackResults.putAll(callbackData);
      }
    }

    // Populate data based on callback results
    for (String name : mNames) {
      data.add(callbackResults.remove(name));
    }
    if (callbackResults.size() > 0) {
      ZimbraLog.perf.warn(
          "Detected unexpected realtime stats: " + StringUtil.join(", ", callbackResults.keySet()));
    }
    return data;
  }

  public void reset() {}
}
