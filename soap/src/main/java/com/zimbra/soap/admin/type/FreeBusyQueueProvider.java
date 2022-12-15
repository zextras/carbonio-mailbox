// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.Id;

@XmlAccessorType(XmlAccessType.NONE)
public class FreeBusyQueueProvider {

    /**
     * @zm-api-field-tag provider-name
     * @zm-api-field-description Provider name
     */
    @XmlAttribute(name=AdminConstants.A_NAME, required=true)
    private final String name;

    /**
     * @zm-api-field-description Information on accounts
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT, required=false)
    private List<Id> accounts = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FreeBusyQueueProvider() {
        this((String) null);
    }

    public FreeBusyQueueProvider(String name) {
        this.name = name;
    }

    public void setAccounts(Iterable <Id> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            Iterables.addAll(this.accounts,accounts);
        }
    }

    public FreeBusyQueueProvider addAccount(Id account) {
        this.accounts.add(account);
        return this;
    }

    public String getName() { return name; }
    public List<Id> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }
}
