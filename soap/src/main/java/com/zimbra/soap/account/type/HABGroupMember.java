// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;
import com.zimbra.soap.type.NamedValue;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class HABGroupMember extends HABMember {

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  public HABGroupMember() {
    this((String) null);
  }

  public HABGroupMember(String name) {
    super(name);
  }

  /**
   * @zm-api-field-description Member attributes. Currently only these attributes are returned:
   *     <table>
   * <tr><td> <b>zimbraId</b>       </td><td> the unique UUID of the hab member </td></tr>
   * <tr><td> <b>displayName</b>    </td><td> display name for the member </td></tr>
   * </table>
   */
  @ZimbraKeyValuePairs
  @XmlElement(name = AccountConstants.E_ATTR /* attr */, required = true)
  private List<NamedValue> attrs = Lists.newArrayList();

  public List<NamedValue> getAttrs() {
    return attrs;
  }

  public void setAttrs(Iterable<NamedValue> attrs) {
    this.attrs.clear();
    if (attrs != null) {
      Iterables.addAll(this.attrs, attrs);
    }
  }

  public HABGroupMember addAttr(NamedValue attr) {
    this.attrs.add(attr);
    return this;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    super.addToStringInfo(helper);
    return helper.add("attrs", attrs.toString());
  }
}
