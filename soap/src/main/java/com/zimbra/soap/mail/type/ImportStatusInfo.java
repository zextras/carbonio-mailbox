// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class ImportStatusInfo {

  /**
   * @zm-api-field-tag datasource-id
   * @zm-api-field-description Data source ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = false)
  private String id;

  /**
   * @zm-api-field-tag is-running
   * @zm-api-field-description Whether data is currently being imported from this data source
   */
  @XmlAttribute(name = MailConstants.A_DS_IS_RUNNING /* isRunning */, required = false)
  private ZmBoolean running;

  /**
   * @zm-api-field-tag success
   * @zm-api-field-description Whether the last import completed successfully. (not returned if the
   *     import has not run yet)
   */
  @XmlAttribute(name = MailConstants.A_DS_SUCCESS /* success */, required = false)
  private ZmBoolean success;

  /**
   * @zm-api-field-tag error-message
   * @zm-api-field-description If the last import failed, this is the error message that was
   *     returned. (not returned if the import has not run yet)
   */
  @XmlAttribute(name = MailConstants.A_DS_ERROR /* error */, required = false)
  private String error;

  public ImportStatusInfo() {}

  public void setId(String id) {
    this.id = id;
  }

  public void setRunning(Boolean running) {
    this.running = ZmBoolean.fromBool(running);
  }

  public void setSuccess(Boolean success) {
    this.success = ZmBoolean.fromBool(success);
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getId() {
    return id;
  }

  public Boolean getRunning() {
    return ZmBoolean.toBool(running);
  }

  public Boolean getSuccess() {
    return ZmBoolean.toBool(success);
  }

  public String getError() {
    return error;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("running", running).add("success", success).add("error", error);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
