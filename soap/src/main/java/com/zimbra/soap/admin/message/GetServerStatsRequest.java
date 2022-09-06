// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.Stat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Returns server monitoring stats. These are the same stats that are
 *     logged to mailboxd.csv. If no <b>&lt;stat></b> element is specified, all server stats are
 *     returned. If the stat name is invalid, returns a SOAP fault.
 */
@XmlRootElement(name = AdminConstants.E_GET_SERVER_STATS_REQUEST)
@XmlType(propOrder = {})
public class GetServerStatsRequest {

  /**
   * @zm-api-field-description Stats
   */
  @XmlElement(name = AdminConstants.E_STAT)
  private List<Stat> stats = new ArrayList<Stat>();

  public GetServerStatsRequest() {}

  public GetServerStatsRequest(String... statNames) {
    if (statNames != null) {
      for (String name : statNames) {
        addStat(name);
      }
    }
  }

  public List<Stat> getStats() {
    return Collections.unmodifiableList(stats);
  }

  public void setStats(Iterable<Stat> stats) {
    this.stats.clear();
    if (stats != null) {
      Iterables.addAll(this.stats, stats);
    }
  }

  public void setStatNames(Iterable<String> names) {
    this.stats.clear();
    if (names != null) {
      for (String name : names) {
        addStat(name);
      }
    }
  }

  public void addStat(String statName) {
    Stat stat = new Stat();
    stat.setName(statName);
    this.stats.add(stat);
  }
}
