// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DLInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ACCOUNT_MEMBERSHIP_RESPONSE)
@XmlType(propOrder = {})
public class GetAccountMembershipResponse {

    /**
     * @zm-api-field-description List membership information
     */
    @XmlElement(name=AdminConstants.E_DL)
    private List<DLInfo> dlList = new ArrayList<>();

    public GetAccountMembershipResponse() {
    }

    public GetAccountMembershipResponse setDlList(Collection<DLInfo> dls) {
        this.dlList.clear();
        if (dls != null) {
            this.dlList.addAll(dls);
        }
        return this;
    }

    public GetAccountMembershipResponse addDl(DLInfo dl) {
        dlList.add(dl);
        return this;
    }

    public List<DLInfo> getDlList() {
        return Collections.unmodifiableList(dlList);
    }
}
