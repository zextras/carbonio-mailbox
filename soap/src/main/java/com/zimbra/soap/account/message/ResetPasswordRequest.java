// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.util.StringUtil;

/**
 <ResetPasswordRequest>
   <password>...</password>
 </ResetPasswordRequest>
 * @zm-api-command-auth-required true - This request should be sent after authentication.
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Reset Password
*/
@XmlRootElement(name=AccountConstants.E_RESET_PASSWORD_REQUEST /* ResetPasswordRequest */)
@XmlType(propOrder = {})
public class ResetPasswordRequest {
    /**
     * @zm-api-field-description New Password to assign
     */
    @XmlElement(name=AccountConstants.E_PASSWORD /* password */, required=true)
    private String password;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String newPassword) {
        this.password = newPassword;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public void validateResetPasswordRequest() throws ServiceException {
        if (StringUtil.isNullOrEmpty(this.password)) {
            throw ServiceException.INVALID_REQUEST("Invalid or missing password", null);
        }
    }
}
