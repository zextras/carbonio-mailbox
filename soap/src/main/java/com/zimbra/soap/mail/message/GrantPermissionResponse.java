// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.AccountACEinfo;

/*
 * Delete this class in bug 66989
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GRANT_PERMISSION_RESPONSE)
public class GrantPermissionResponse {

    /**
     * @zm-api-field-description Permissions that were successfully granted
     */
    @XmlElement(name=MailConstants.E_ACE /* ace */, required=false)
    private List<AccountACEinfo> aces = Lists.newArrayList();

    public GrantPermissionResponse() {
    }

    public void setAces(Iterable <AccountACEinfo> aces) {
        this.aces.clear();
        if (aces != null) {
            Iterables.addAll(this.aces,aces);
        }
    }

    public GrantPermissionResponse addAce(AccountACEinfo ace) {
        this.aces.add(ace);
        return this;
    }

    public List<AccountACEinfo> getAces() {
        return Collections.unmodifiableList(aces);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("aces", aces);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
