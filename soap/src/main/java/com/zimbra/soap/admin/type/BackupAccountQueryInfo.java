// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class BackupAccountQueryInfo {

    @XmlAttribute(name=BackupConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-tag more-flag
     * @zm-api-field-description Present if there are more backups to page through
     */
    @XmlAttribute(name=BackupConstants.A_MORE /* more */, required=false)
    private ZmBoolean more;

    /**
     * @zm-api-field-description Information about backup
     */
    @XmlElement(name=BackupConstants.E_BACKUP /* backup */, required=false)
    private List<BackupAccountQueryBackupInfo> backups = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private BackupAccountQueryInfo() {
        this(null);
    }

    public BackupAccountQueryInfo(String name) {
        this.name = name;
    }

    public void setMore(Boolean more) { this.more = ZmBoolean.fromBool(more); }

    public void setBackups(Iterable <BackupAccountQueryBackupInfo> backups) {
        this.backups.clear();
        if (backups != null) {
            Iterables.addAll(this.backups,backups);
        }
    }

    public void addBackup(BackupAccountQueryBackupInfo backup) {
        this.backups.add(backup);
    }


    public String getName() { return name; }
    public Boolean getMore() { return ZmBoolean.toBool(more); }

    public List<BackupAccountQueryBackupInfo> getBackups() {
        return backups;
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("more", more)
            .add("backups", backups);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
