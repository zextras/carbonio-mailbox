// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHECK_DOMAIN_MX_RECORD_RESPONSE)
@XmlType(propOrder = {"entries", "code", "message"})
public class CheckDomainMXRecordResponse {

  /**
   * @zm-api-field-description MX Record entries
   */
  @XmlElement(name = AdminConstants.E_ENTRY, required = false)
  private List<String> entries = Lists.newArrayList();

  /**
   * @zm-api-field-description Code - <b>Ok</b> or <b>Failed</b>
   */
  @XmlElement(name = AdminConstants.E_CODE, required = true)
  private String code;

  /**
   * @zm-api-field-description Message associated with <b>code="Failed"</b>
   */
  @XmlElement(name = AdminConstants.E_MESSAGE, required = false)
  private String message;

  public CheckDomainMXRecordResponse() {}

  public CheckDomainMXRecordResponse setEntries(Collection<String> entries) {
    this.entries.clear();
    if (entries != null) {
      this.entries.addAll(entries);
    }
    return this;
  }

  public CheckDomainMXRecordResponse addEntry(String entry) {
    entries.add(entry);
    return this;
  }

  public List<String> getEntries() {
    return Collections.unmodifiableList(entries);
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
