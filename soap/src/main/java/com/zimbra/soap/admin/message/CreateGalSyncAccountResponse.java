// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AccountInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_GAL_SYNC_ACCOUNT_RESPONSE)
public class CreateGalSyncAccountResponse {

    /**
     * @zm-api-field-description Information about the newly created GalSync account
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT, required=true)
    private final AccountInfo account;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CreateGalSyncAccountResponse() {
        this(null);
    }

    public CreateGalSyncAccountResponse(AccountInfo account) {
        this.account = account;
    }

    public AccountInfo getAccount() { return account; }
}
