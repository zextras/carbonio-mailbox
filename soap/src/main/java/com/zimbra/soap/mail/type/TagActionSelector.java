// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class TagActionSelector extends ActionSelector {

  /**
   * @zm-api-field-description Retention policy
   */
  @XmlElement(name = MailConstants.E_RETENTION_POLICY, required = false)
  protected RetentionPolicy retentionPolicy;

  public TagActionSelector() {
    this((String) null, (String) null);
  }

  public TagActionSelector(String ids, String operation) {
    super(ids, operation);
  }

  public void setRetentionPolicy(RetentionPolicy rp) {
    this.retentionPolicy = rp;
  }

  public RetentionPolicy getRetentionPolicy() {
    return retentionPolicy;
  }
}
