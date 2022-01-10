// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.Name;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=BackupConstants.E_RESTORE_RESPONSE)
@XmlType(propOrder = {})
public class RestoreResponse {

    /**
     * @zm-api-field-description Status - one of <b>ok|interrupted|err</b>
     */
    @XmlAttribute(name=BackupConstants.A_STATUS /* status */, required=false)
    private String status;

    /**
     * @zm-api-field-tag rebuilt-schema
     * @zm-api-field-description Flag whether schema was rebuilt
     */
    @XmlAttribute(name=BackupConstants.A_REBUILTSCHEMA /* rebuiltSchema */, required=false)
    private ZmBoolean rebuildSchema;

    /**
     * @zm-api-field-description Accounts
     */
    @XmlElement(name=BackupConstants.E_ACCOUNT /* account */, required=false)
    private List<Name> accounts = Lists.newArrayList();

    public RestoreResponse() {
    }

    public void setStatus(String status) { this.status = status; }
    public void setRebuildSchema(Boolean rebuildSchema) { this.rebuildSchema = ZmBoolean.fromBool(rebuildSchema); }
    public void setAccounts(Iterable <Name> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            Iterables.addAll(this.accounts,accounts);
        }
    }

    public void addAccount(Name account) {
        this.accounts.add(account);
    }

    public String getStatus() { return status; }
    public Boolean getRebuildSchema() { return ZmBoolean.toBool(rebuildSchema); }
    public List<Name> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("status", status)
            .add("rebuildSchema", rebuildSchema)
            .add("accounts", accounts);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
