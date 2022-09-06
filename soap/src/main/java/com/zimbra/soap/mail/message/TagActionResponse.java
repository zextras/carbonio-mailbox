// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import com.zimbra.soap.mail.type.TagActionInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_TAG_ACTION_RESPONSE)
public class TagActionResponse {

  /**
   * @zm-api-field-description The <b>&lt;action></b> element contains information about the tags
   *     affected by the operation if and only if the operation was successful
   */
  @ZimbraUniqueElement
  @XmlElement(name = MailConstants.E_ACTION /* action */, required = true)
  private TagActionInfo action;

  public TagActionResponse() {}

  public void setAction(TagActionInfo action) {
    this.action = action;
  }

  public TagActionInfo getAction() {
    return action;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("action", action);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
