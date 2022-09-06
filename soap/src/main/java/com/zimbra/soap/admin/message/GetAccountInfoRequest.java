// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get information about an account <br>
 *     Currently only 2 attrs are returned:
 *     <table>
 * <tr> <td> <b>zimbraId</b> </td> <td> the unique UUID of the zimbra account </td> </tr>
 * <tr> <td> <b>zimbraMailHost</b> </td> <td> the server on which this user's mail resides </td> </tr>
 * </table>
 *     <b>Access</b>: domain admin sufficient
 */
@XmlRootElement(name = AdminConstants.E_GET_ACCOUNT_INFO_REQUEST)
public class GetAccountInfoRequest {

  @XmlElement(name = AdminConstants.E_ACCOUNT, required = true)
  private AccountSelector account;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetAccountInfoRequest() {}

  public GetAccountInfoRequest(AccountSelector account) {
    this.account = account;
  }

  public AccountSelector getAccount() {
    return account;
  }
}
