// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.AutoCompleteMatch;
import com.zimbra.soap.type.ZmBoolean;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_AUTO_COMPLETE_RESPONSE)
public class AutoCompleteResponse {

  /**
   * @zm-api-field-tag can-be-cached
   * @zm-api-field-description Flag whether can be cached
   */
  @XmlAttribute(name = MailConstants.A_CANBECACHED /* canBeCached */, required = false)
  private ZmBoolean canBeCached;

  /**
   * @zm-api-field-description Matches
   */
  @XmlElement(name = MailConstants.E_MATCH /* match */, required = false)
  private List<AutoCompleteMatch> matches = Lists.newArrayList();

  public AutoCompleteResponse() {}

  public void setCanBeCached(Boolean canBeCached) {
    this.canBeCached = ZmBoolean.fromBool(canBeCached);
  }

  public void setMatches(Iterable<AutoCompleteMatch> matches) {
    this.matches.clear();
    if (matches != null) {
      Iterables.addAll(this.matches, matches);
    }
  }

  public AutoCompleteResponse addMatche(AutoCompleteMatch matche) {
    this.matches.add(matche);
    return this;
  }

  public Boolean getCanBeCached() {
    return ZmBoolean.toBool(canBeCached);
  }

  public List<AutoCompleteMatch> getMatches() {
    return Collections.unmodifiableList(matches);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("canBeCached", canBeCached).add("matches", matches);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
