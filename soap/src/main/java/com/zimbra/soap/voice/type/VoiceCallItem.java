// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.VoiceConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceCallItem {

  /**
   * @zm-api-field-tag phone
   * @zm-api-field-description Phone to which the message is for
   */
  @XmlAttribute(name = VoiceConstants.A_PHONE /* phone */, required = true)
  private String phone;

  /**
   * @zm-api-field-tag folder-id
   * @zm-api-field-description Folder id of the folder in which the message resides
   */
  @XmlAttribute(name = MailConstants.A_FOLDER /* l */, required = true)
  private String folderId;

  /**
   * @zm-api-field-tag sort-field-value
   * @zm-api-field-description Value of the field this search is based on
   */
  @XmlAttribute(name = MailConstants.A_SORT_FIELD /* sf */, required = true)
  private String sortFieldValue;

  /**
   * @zm-api-field-tag msg-duration-secs
   * @zm-api-field-description Message duration in seconds
   */
  @XmlAttribute(name = VoiceConstants.A_VMSG_DURATION /* du */, required = true)
  private int durationInSecs;

  /**
   * @zm-api-field-tag date
   * @zm-api-field-description Timestamp when the message was deposited
   */
  @XmlAttribute(name = MailConstants.A_DATE /* d */, required = true)
  private long date;

  public VoiceCallItem() {}

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public void setFolderId(String folderId) {
    this.folderId = folderId;
  }

  public void setSortFieldValue(String sortFieldValue) {
    this.sortFieldValue = sortFieldValue;
  }

  public void setDurationInSecs(int durationInSecs) {
    this.durationInSecs = durationInSecs;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public String getPhone() {
    return phone;
  }

  public String getFolderId() {
    return folderId;
  }

  public String getSortFieldValue() {
    return sortFieldValue;
  }

  public int getDurationInSecs() {
    return durationInSecs;
  }

  public long getDate() {
    return date;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("phone", phone)
        .add("folderId", folderId)
        .add("sortFieldValue", sortFieldValue)
        .add("durationInSecs", durationInSecs)
        .add("date", date);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
