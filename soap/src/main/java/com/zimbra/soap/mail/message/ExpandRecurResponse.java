// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ExpandedRecurrenceInstance;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "ExpandRecurResponse")
public class ExpandRecurResponse {

  /**
   * @zm-api-field-description Expanded recurrence instances
   */
  @XmlElement(name = MailConstants.E_INSTANCE /* inst */, required = false)
  private List<ExpandedRecurrenceInstance> instances = Lists.newArrayList();

  public ExpandRecurResponse() {}

  public void setInstances(Iterable<ExpandedRecurrenceInstance> instances) {
    this.instances.clear();
    if (instances != null) {
      Iterables.addAll(this.instances, instances);
    }
  }

  public ExpandRecurResponse addInstance(ExpandedRecurrenceInstance instance) {
    this.instances.add(instance);
    return this;
  }

  public List<ExpandedRecurrenceInstance> getInstances() {
    return Collections.unmodifiableList(instances);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("instances", instances);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
