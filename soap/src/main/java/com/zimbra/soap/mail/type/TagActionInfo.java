// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class TagActionInfo {

  /**
   * @zm-api-field-tag tag-ids
   * @zm-api-field-description Tag IDs for successfully applied operation
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private String successes;

  /**
   * @zm-api-field-tag tag-names
   * @zm-api-field-description Names of tags affected by successfully applied operation <br>
   *     Only present if <b>"tn"</b> was specified in the request
   */
  @XmlAttribute(name = MailConstants.A_TAG_NAMES /* tn */, required = false)
  private String successNames;

  /**
   * @zm-api-field-tag operation
   * @zm-api-field-description Operation - "read|!read|color|delete|rename|update|retentionpolicy"
   */
  @XmlAttribute(name = MailConstants.A_OPERATION /* op */, required = true)
  private String operation;

  public TagActionInfo() {}

  public void setSuccesses(String successes) {
    this.successes = successes;
  }

  public void setSuccessNames(String successNames) {
    this.successNames = successNames;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public String getSuccesses() {
    return successes;
  }

  public String getSuccessNames() {
    return successNames;
  }

  public String getOperation() {
    return operation;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("successes", successes)
        .add("successNames", successNames)
        .add("operation", operation);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
