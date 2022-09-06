// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.mail.type.WatcherInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusXmlConstants.E_GET_WATCHERS_RESPONSE)
public class GetWatchersResponse {

  /**
   * @zm-api-field-description Information on items being watched by users
   */
  @XmlElement(name = MailConstants.E_WATCHER /* watcher */, required = false)
  private List<WatcherInfo> watchers = Lists.newArrayList();

  public GetWatchersResponse() {}

  public void setWatchers(Iterable<WatcherInfo> watchers) {
    this.watchers.clear();
    if (watchers != null) {
      Iterables.addAll(this.watchers, watchers);
    }
  }

  public void addWatcher(WatcherInfo watcher) {
    this.watchers.add(watcher);
  }

  public List<WatcherInfo> getWatchers() {
    return Collections.unmodifiableList(watchers);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("watchers", watchers);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
