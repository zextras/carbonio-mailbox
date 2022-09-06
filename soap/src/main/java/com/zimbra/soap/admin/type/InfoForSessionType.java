// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"accounts", "sessions"})
public class InfoForSessionType {

  /**
   * @zm-api-field-tag active-account-count
   * @zm-api-field-description Count of number of active accounts
   */
  @XmlAttribute(name = AdminConstants.A_ACTIVE_ACCOUNTS, required = false)
  private final Integer activeAccounts;

  /**
   * @zm-api-field-tag active-account-sessions
   * @zm-api-field-description Count of number of active sessions
   */
  @XmlAttribute(name = AdminConstants.A_ACTIVE_SESSIONS, required = true)
  private final int activeSessions;

  /**
   * @zm-api-field-description If the request selected <b>"groupByAccount"</b> and
   *     <b>"listSessions"</b> then the session information will be grouped under here.
   */
  @XmlElement(name = AdminConstants.A_ZIMBRA_ID, required = false)
  private List<AccountSessionInfo> accounts = Lists.newArrayList();

  /**
   * @zm-api-field-description If the request selected <b>"listSessions"</b> but NOT
   *     <b>"groupByAccount"</b> then the session information will be under here.
   */
  @XmlElement(name = "s", required = false)
  private List<SessionInfo> sessions = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private InfoForSessionType() {
    this((Integer) null, -1);
  }

  public InfoForSessionType(Integer activeAccounts, int activeSessions) {
    this.activeAccounts = activeAccounts;
    this.activeSessions = activeSessions;
  }

  public void setAccounts(Iterable<AccountSessionInfo> accounts) {
    this.accounts.clear();
    if (accounts != null) {
      Iterables.addAll(this.accounts, accounts);
    }
  }

  public InfoForSessionType addAccount(AccountSessionInfo account) {
    this.accounts.add(account);
    return this;
  }

  public void setSessions(Iterable<SessionInfo> sessions) {
    this.sessions.clear();
    if (sessions != null) {
      Iterables.addAll(this.sessions, sessions);
    }
  }

  public InfoForSessionType addSession(SessionInfo session) {
    this.sessions.add(session);
    return this;
  }

  public Integer getActiveAccounts() {
    return activeAccounts;
  }

  public int getActiveSessions() {
    return activeSessions;
  }

  public List<AccountSessionInfo> getAccounts() {
    return Collections.unmodifiableList(accounts);
  }

  public List<SessionInfo> getSessions() {
    return Collections.unmodifiableList(sessions);
  }
}
