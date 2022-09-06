// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.VoiceAdminConstants;
import com.zimbra.soap.type.KeyValuePair;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = VoiceAdminConstants.E_UPDATE_PRESENCE_SESSION_ID_RESPONSE)
@XmlType(propOrder = {})
public class UpdatePresenceSessionIdResponse {

  /**
   * @zm-api-field-description Newly generated Cisco presence session ID.
   */
  @XmlElement(name = AdminConstants.E_A)
  private KeyValuePair sessionId;

  public UpdatePresenceSessionIdResponse() {}

  public void setSessionId(KeyValuePair sessionId) {
    this.sessionId = sessionId;
  }

  public String getSessionId() {
    if (sessionId == null) {
      return null;
    }
    return sessionId.getValue();
  }
}
