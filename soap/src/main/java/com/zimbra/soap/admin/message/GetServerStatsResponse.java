// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.Stat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_SERVER_STATS_RESPONSE)
public class GetServerStatsResponse {

  /**
   * @zm-api-field-description Details of server monitoring statistics
   */
  @XmlElement(name = AdminConstants.E_STAT)
  private List<Stat> stats = Lists.newArrayList();

  public List<Stat> getStats() {
    return Collections.unmodifiableList(stats);
  }

  public void setStats(Iterable<Stat> stats) {
    this.stats.clear();
    if (stats != null) {
      Iterables.addAll(this.stats, stats);
    }
  }

  public String getValue(String statName) {
    Iterator<Stat> i = Stat.filterByName(stats, statName);
    if (i.hasNext()) {
      return i.next().getValue();
    }
    return null;
  }

  /**
   * @throws NullPointerException if the value does not exist
   * @throws NumberFormatException if the value cannot be parsed
   */
  public int getIntValue(String statName) {
    String val = getValue(statName);
    if (val == null) {
      throw new NullPointerException("No value found for stat " + statName);
    }
    return Integer.parseInt(val);
  }
}
