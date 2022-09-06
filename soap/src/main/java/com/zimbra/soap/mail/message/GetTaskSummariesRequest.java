// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Task summaries
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_TASK_SUMMARIES_REQUEST)
public class GetTaskSummariesRequest {

  /**
   * @zm-api-field-tag range-start
   * @zm-api-field-description Range start
   */
  @XmlAttribute(name = MailConstants.A_CAL_START_TIME /* s */, required = true)
  private final long startTime;

  /**
   * @zm-api-field-tag range-end
   * @zm-api-field-description Range end
   */
  @XmlAttribute(name = MailConstants.A_CAL_END_TIME /* e */, required = true)
  private final long endTime;

  /**
   * @zm-api-field-tag folder-id
   * @zm-api-field-description Folder ID
   */
  @XmlAttribute(name = MailConstants.A_FOLDER /* l */, required = false)
  private String folderId;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetTaskSummariesRequest() {
    this(-1L, -1L);
  }

  public GetTaskSummariesRequest(long startTime, long endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public void setFolderId(String folderId) {
    this.folderId = folderId;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public String getFolderId() {
    return folderId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("startTime", startTime).add("endTime", endTime).add("folderId", folderId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
