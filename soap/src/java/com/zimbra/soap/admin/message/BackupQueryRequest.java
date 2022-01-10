// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.BackupQuerySpec;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Backup Query
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=BackupConstants.E_BACKUP_QUERY_REQUEST)
public class BackupQueryRequest {

    /**
     * @zm-api-field-description Query
     */
    @XmlElement(name=BackupConstants.E_QUERY /* query */, required=true)
    private final BackupQuerySpec query;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private BackupQueryRequest() {
        this((BackupQuerySpec) null);
    }

    public BackupQueryRequest(BackupQuerySpec query) {
        this.query = query;
    }

    public BackupQuerySpec getQuery() { return query; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("query", query);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
