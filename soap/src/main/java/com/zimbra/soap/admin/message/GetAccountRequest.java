// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.AccountSelector;
import com.zimbra.soap.type.AttributeSelectorImpl;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get attributes related to an account
 * <br />
 * <b>{request-attrs}</b> - comma-seperated list of attrs to return
 * <br />
 * <br />
 * Note: this request is by default proxied to the account's home server
 * <br />
 * <br />
 * <b>Access</b>: domain admin sufficient
 */
@XmlRootElement(name=AdminConstants.E_GET_ACCOUNT_REQUEST)
public class GetAccountRequest extends AttributeSelectorImpl {

    /**
     * @zm-api-field-tag apply-cos
     * @zm-api-field-description Flag whether or not to apply class of service (COS) rules
     * <table>
     * <tr> <td> <b>1 (true) [default]</b> </td> <td> COS rules apply and unset attrs on an account will get their
     *                                                value from the COS </td> </tr>
     * <tr> <td> <b>0 (false)</b> </td> <td> only attributes directly set on the account will be returned </td> </tr>
     * </table>
     */
    @XmlAttribute(name=AdminConstants.A_APPLY_COS, required=false)
    private ZmBoolean applyCos = ZmBoolean.ONE /* true */;

    /**
     * @zm-api-field-description Account
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT)
    private AccountSelector account;

    public GetAccountRequest() {
    }

    public GetAccountRequest(AccountSelector account) {
        this(account, null, null);
    }

    public GetAccountRequest(AccountSelector account, Boolean applyCos) {
        this(account, applyCos, null);
    }

    public GetAccountRequest(AccountSelector account, Boolean applyCos,
            Iterable<String> attrs) {
        super(attrs);
        this.account = account;
        this.applyCos = ZmBoolean.fromBool(applyCos);
    }

    public void setAccount(AccountSelector account) {
        this.account = account;
    }

    public void setApplyCos(Boolean applyCos) {
        this.applyCos = ZmBoolean.fromBool(applyCos);
    }

    public AccountSelector getAccount() { return account; }
    public Boolean isApplyCos() { return ZmBoolean.toBool(applyCos); }
}
