// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Remove Distribution List Member
 * <br />
 * Unlike add, remove of a non-existent member causes an exception and no modification to the list.
 * <br />
 * <b>Access</b>: domain admin sufficient
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_REMOVE_DISTRIBUTION_LIST_MEMBER_REQUEST)
public class RemoveDistributionListMemberRequest {

    /**
     * @zm-api-field-tag value-of-id
     * @zm-api-field-description Zimbra ID
     */
    @XmlAttribute(name=AdminConstants.E_ID, required=true)
    private String id;

    /**
     * @zm-api-field-tag member
     * @zm-api-field-description Members
     */
    @XmlElement(name=AdminConstants.E_DLM, required=true)
    private List <String> members = Lists.newArrayList();

    /**
     * @zm-api-field-tag account
     * @zm-api-field-description specify Accounts insteaf of members if you want to remove all addresses that belong to an account from the list
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT, required=false)
    private List <String> accounts = Lists.newArrayList();

    public RemoveDistributionListMemberRequest() {
        this((String) null, (Collection<String>) null, (Collection<String>) null);
    }

    public RemoveDistributionListMemberRequest(String id, Collection<String> members) {
        setId(id);
        setMembers(members);
    }

    public RemoveDistributionListMemberRequest(String id, Collection<String> members, Collection<String> accounts) {
        setId(id);
        setMembers(members);
        setAccounts(accounts);
    }

    public RemoveDistributionListMemberRequest setMembers(Collection<String> members) {
        this.members.clear();
        if (members != null) {
            this.members.addAll(members);
        }
        return this;
    }

    public RemoveDistributionListMemberRequest setAccounts(Collection<String> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            this.accounts.addAll(accounts);
        }
        return this;
    }

    public RemoveDistributionListMemberRequest addMember(String member) {
        members.add(member);
        return this;
    }

    public RemoveDistributionListMemberRequest addAccount(String account) {
        accounts.add(account);
        return this;
    }

    public List<String> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<String> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
}
