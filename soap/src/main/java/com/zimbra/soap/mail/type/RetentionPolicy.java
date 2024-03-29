// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = MailConstants.E_RETENTION_POLICY, namespace = MailConstants.NAMESPACE_STR)
@XmlAccessorType(XmlAccessType.NONE)
public class RetentionPolicy {

  /**
   * @zm-api-field-description "Purge" retention policies
   */
  @ZimbraJsonArrayForWrapper
  @XmlElementWrapper(name = MailConstants.E_PURGE, required = false)
  @XmlElement(name = MailConstants.E_POLICY, required = false)
  private final List<Policy> purge = Lists.newArrayList();

  public RetentionPolicy() {}

  public RetentionPolicy(Element e) throws ServiceException {
    final Element purgeEl = e.getOptionalElement(MailConstants.E_PURGE);
    if (purgeEl != null) {
      for (final Element p : purgeEl.listElements(MailConstants.E_POLICY)) {
        purge.add(new Policy(p));
      }
    }
  }

  public RetentionPolicy(Iterable<Policy> purge) {
    this.purge.clear();
    if (purge != null) {
      Iterables.addAll(this.purge, purge);
    }
  }

  public List<Policy> getPurgePolicy() {
    return Collections.unmodifiableList(purge);
  }

  public Policy getPolicyById(String id) {
    for (final Policy p : purge) {
      if (Objects.equal(p.getId(), id)) {
        return p;
      }
    }
    return null;
  }

  public boolean isSet() {
    return !purge.isEmpty();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("purge", purge).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RetentionPolicy)) {
      return false;
    }
    final RetentionPolicy other = (RetentionPolicy) o;
    return purge.equals(other.purge);
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }
}
