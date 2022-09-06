// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ContactActionSelector extends ActionSelector {

  /**
   * @zm-api-field-description New Contact attributes
   */
  @XmlElement(name = MailConstants.E_ATTRIBUTE, required = false)
  private final List<NewContactAttr> attrs = Lists.newArrayList();

  public ContactActionSelector() {}

  public ContactActionSelector(String ids, String operation) {
    super(ids, operation);
  }

  public void setAttrs(Iterable<NewContactAttr> attrs) {
    this.attrs.clear();
    if (attrs != null) {
      Iterables.addAll(this.attrs, attrs);
    }
  }

  public ContactActionSelector addAttr(NewContactAttr attr) {
    this.attrs.add(attr);
    return this;
  }

  public List<NewContactAttr> getAttrs() {
    return Collections.unmodifiableList(attrs);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("attrs", attrs).toString();
  }
}
