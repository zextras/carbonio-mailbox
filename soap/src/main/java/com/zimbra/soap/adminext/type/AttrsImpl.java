// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.adminext.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.util.StringUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AttrsImpl implements Attrs {

  /**
   * @zm-api-field-description Attrs
   */
  @XmlElement(name = AdminConstants.E_A /* a */, required = false)
  private List<Attr> attrs = Lists.newArrayList();

  public AttrsImpl() {
    this.setAttrs((Iterable<Attr>) null);
  }

  public AttrsImpl(Iterable<Attr> attrs) {
    this.setAttrs(attrs);
  }

  public AttrsImpl(Map<String, ? extends Object> attrs) throws ServiceException {
    this.setAttrs(attrs);
  }

  public Attrs setAttrs(Iterable<Attr> attrs) {
    this.attrs.clear();
    if (attrs != null) {
      Iterables.addAll(this.attrs, attrs);
    }
    return this;
  }

  public Attrs setAttrs(Map<String, ? extends Object> attrs) throws ServiceException {
    this.setAttrs(Attr.fromMap(attrs));
    return this;
  }

  public Attrs addAttr(Attr attr) {
    attrs.add(attr);
    return this;
  }

  public List<Attr> getAttrs() {
    return Collections.unmodifiableList(attrs);
  }

  public Multimap<String, String> getAttrsMultimap() {
    return Attr.toMultimap(attrs);
  }

  public Map<String, Object> getAttrsAsOldMultimap() {
    return StringUtil.toOldMultimap(getAttrsMultimap());
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("attrs", attrs);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
