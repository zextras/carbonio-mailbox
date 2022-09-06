// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.InfoForSessionType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_DUMP_SESSIONS_RESPONSE)
@XmlType(
    propOrder = {
      "soapSessions",
      "imapSessions",
      "adminSessions",
      "wikiSessions",
      "synclistenerSessions",
      "waitsetSessions"
    })
public class DumpSessionsResponse {

  /**
   * @zm-api-field-tag total-active-session-count
   * @zm-api-field-description Count of active sessions
   */
  @XmlAttribute(name = AdminConstants.A_ACTIVE_SESSIONS, required = true)
  private final int totalActiveSessions;

  // Mapped from Session.Type.SOAP
  /**
   * @zm-api-field-description Information about SOAP sessions
   */
  @XmlElement(name = "soap", required = false)
  private InfoForSessionType soapSessions;

  // Mapped from Session.Type.IMAP
  /**
   * @zm-api-field-description Information about IMAP sessions
   */
  @XmlElement(name = "imap", required = false)
  private InfoForSessionType imapSessions;

  // Mapped from Session.Type.ADMIN
  /**
   * @zm-api-field-description Information about ADMIn sessions
   */
  @XmlElement(name = "admin", required = false)
  private InfoForSessionType adminSessions;

  // Mapped from Session.Type.WIKI
  /**
   * @zm-api-field-description Information about WIKI sessions
   */
  @XmlElement(name = "wiki", required = false)
  private InfoForSessionType wikiSessions;

  // Mapped from Session.Type.SYNCLISTENER
  /**
   * @zm-api-field-description Information about SYNCLISTENER sessions
   */
  @XmlElement(name = "synclistener", required = false)
  private InfoForSessionType synclistenerSessions;

  // Mapped from Session.Type.WAITSET
  /**
   * @zm-api-field-description Information about WaitSet sessions
   */
  @XmlElement(name = "waitset", required = false)
  private InfoForSessionType waitsetSessions;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DumpSessionsResponse() {
    this(-1);
  }

  public DumpSessionsResponse(int totalActiveSessions) {
    this.totalActiveSessions = totalActiveSessions;
  }

  public void setSoapSessions(InfoForSessionType soapSessions) {
    this.soapSessions = soapSessions;
  }

  public void setImapSessions(InfoForSessionType imapSessions) {
    this.imapSessions = imapSessions;
  }

  public void setAdminSessions(InfoForSessionType adminSessions) {
    this.adminSessions = adminSessions;
  }

  public void setWikiSessions(InfoForSessionType wikiSessions) {
    this.wikiSessions = wikiSessions;
  }

  public void setSynclistenerSessions(InfoForSessionType synclistenerSessions) {
    this.synclistenerSessions = synclistenerSessions;
  }

  public void setWaitsetSessions(InfoForSessionType waitsetSessions) {
    this.waitsetSessions = waitsetSessions;
  }

  public int getTotalActiveSessions() {
    return totalActiveSessions;
  }

  public InfoForSessionType getSoapSessions() {
    return soapSessions;
  }

  public InfoForSessionType getImapSessions() {
    return imapSessions;
  }

  public InfoForSessionType getAdminSessions() {
    return adminSessions;
  }

  public InfoForSessionType getWikiSessions() {
    return wikiSessions;
  }

  public InfoForSessionType getSynclistenerSessions() {
    return synclistenerSessions;
  }

  public InfoForSessionType getWaitsetSessions() {
    return waitsetSessions;
  }
}
