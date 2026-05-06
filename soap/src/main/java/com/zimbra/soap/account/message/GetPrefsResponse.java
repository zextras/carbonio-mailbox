// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Pref;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_PREFS_RESPONSE)
@XmlType(propOrder = {AccountConstants.E_PREF})
public class GetPrefsResponse {

    /**
     * @zm-api-field-description Preferences
     */
    @XmlElement(name=AccountConstants.E_PREF)
    private List<Pref> pref = new ArrayList<>();

    public void setPref(List<Pref> pref) {
        this.pref = pref;
    }

    public List<Pref> getPref() {
        return Collections.unmodifiableList(pref);
    }
}
