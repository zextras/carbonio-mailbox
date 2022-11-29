// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.DLInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_ACCOUNT_DISTRIBUTION_LISTS_RESPONSE)
public class GetAccountDistributionListsResponse {

    /**
     * @zm-api-field-description Information on distribution lists
     */
    @XmlElement(name=AccountConstants.E_DL)
    private List<DLInfo> dlList = new ArrayList<DLInfo>();

    public GetAccountDistributionListsResponse() {
    }

    public GetAccountDistributionListsResponse setDlList(Collection<DLInfo> dls) {
        this.dlList.clear();
        if (dls != null) {
            this.dlList.addAll(dls);
        }
        return this;
    }

    public GetAccountDistributionListsResponse addDl(DLInfo dl) {
        dlList.add(dl);
        return this;
    }

    public List<DLInfo> getDlList() {
        return Collections.unmodifiableList(dlList);
    }
}
