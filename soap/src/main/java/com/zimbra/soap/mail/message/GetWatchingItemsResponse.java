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
import com.zimbra.soap.mail.type.WatchingTarget;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusXmlConstants.E_GET_WATCHING_ITEMS_RESPONSE)
public class GetWatchingItemsResponse {

  /**
   * @zm-api-field-description Watching targets
   */
  @XmlElement(name = MailConstants.E_TARGET /* target */, required = false)
  private List<WatchingTarget> targets = Lists.newArrayList();

  public GetWatchingItemsResponse() {}

  public void setTargets(Iterable<WatchingTarget> targets) {
    this.targets.clear();
    if (targets != null) {
      Iterables.addAll(this.targets, targets);
    }
  }

  public void addTarget(WatchingTarget target) {
    this.targets.add(target);
  }

  public List<WatchingTarget> getTargets() {
    return Collections.unmodifiableList(targets);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("targets", targets);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
