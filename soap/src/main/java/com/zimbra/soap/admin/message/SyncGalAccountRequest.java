// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.SyncGalAccountSpec;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Sync GalAccount <br>
 *     Notes:
 *     <ul>
 *       <li>If fullSync is set to false (or unset) the default behavior is trickle sync which will
 *           pull in any new contacts or modified contacts since last sync.
 *       <li>If fullSync is set to true, then the server will go through all the contacts that
 *           appear in GAL, and resolve deleted contacts in addition to new or modified ones.
 *       <li>If reset attribute is set, then all the contacts will be populated again, regardless of
 *           the status since last sync. Reset needs to be done when there is a significant change
 *           in the configuration, such as filter, attribute map, or search base.
 *     </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_SYNC_GAL_ACCOUNT_REQUEST)
public class SyncGalAccountRequest {

  /**
   * @zm-api-field-description Sync GalAccount specification
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT /* account */, required = false)
  private List<SyncGalAccountSpec> accounts = Lists.newArrayList();

  public SyncGalAccountRequest() {}

  public void setAccounts(Iterable<SyncGalAccountSpec> accounts) {
    this.accounts.clear();
    if (accounts != null) {
      Iterables.addAll(this.accounts, accounts);
    }
  }

  public void addAccount(SyncGalAccountSpec account) {
    this.accounts.add(account);
  }

  public List<SyncGalAccountSpec> getAccounts() {
    return accounts;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("accounts", accounts);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
