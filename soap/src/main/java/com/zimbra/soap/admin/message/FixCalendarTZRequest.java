// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.TZFixupRule;
import com.zimbra.soap.type.NamedElement;
import com.zimbra.soap.type.ZmBoolean;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Fix timezone definitions in appointments and tasks to reflect changes
 *     in daylight savings time rules in various timezones.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_FIX_CALENDAR_TZ_REQUEST)
public class FixCalendarTZRequest {

  /**
   * @zm-api-field-tag sync
   * @zm-api-field-description Sync flag
   *     <table>
   * <tr> <td> <b>1 (true)</b> </td> <td> command blocks until processing finishes </td> </tr>
   * <tr> <td> <b>0 (false) [default]</b> </td> <td> command returns right away </td> </tr>
   * </table>
   */
  @XmlAttribute(name = AdminConstants.A_TZFIXUP_SYNC /* sync */, required = false)
  private ZmBoolean sync;

  /**
   * @zm-api-field-tag millis
   * @zm-api-field-description Fix appts/tasks that have instances after this time <br>
   *     default = January 1, 2008 00:00:00 in GMT+13:00 timezone.
   */
  @XmlAttribute(name = AdminConstants.A_TZFIXUP_AFTER /* after */, required = false)
  private Long after;

  /**
   * @zm-api-field-description Accounts
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT /* account */, required = false)
  private List<NamedElement> accounts = Lists.newArrayList();

  /**
   * @zm-api-field-description Fixup rules
   */
  @XmlElementWrapper(name = AdminConstants.E_TZFIXUP /* tzfixup */, required = false)
  @XmlElement(name = AdminConstants.E_FIXUP_RULE /* fixupRule */, required = false)
  private List<TZFixupRule> fixupRules = Lists.newArrayList();

  public FixCalendarTZRequest() {}

  public void setSync(Boolean sync) {
    this.sync = ZmBoolean.fromBool(sync);
  }

  public void setAfter(Long after) {
    this.after = after;
  }

  public void setAccounts(Iterable<NamedElement> accounts) {
    this.accounts.clear();
    if (accounts != null) {
      Iterables.addAll(this.accounts, accounts);
    }
  }

  public void addAccount(NamedElement account) {
    this.accounts.add(account);
  }

  public void setFixupRules(Iterable<TZFixupRule> fixupRules) {
    this.fixupRules.clear();
    if (fixupRules != null) {
      Iterables.addAll(this.fixupRules, fixupRules);
    }
  }

  public void addFixupRule(TZFixupRule fixupRule) {
    this.fixupRules.add(fixupRule);
  }

  public Boolean getSync() {
    return ZmBoolean.toBool(sync);
  }

  public Long getAfter() {
    return after;
  }

  public List<NamedElement> getAccounts() {
    return Collections.unmodifiableList(accounts);
  }

  public List<TZFixupRule> getFixupRules() {
    return Collections.unmodifiableList(fixupRules);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("sync", sync)
        .add("after", after)
        .add("accounts", accounts)
        .add("fixupRules", fixupRules);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
