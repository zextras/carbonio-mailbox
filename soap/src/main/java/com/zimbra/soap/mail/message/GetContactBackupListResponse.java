// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.StringUtil;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

/**
 * api to get list of available contact backups
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_CONTACT_BACKUP_LIST_RESPONSE)
public class GetContactBackupListResponse {
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=MailConstants.E_BACKUPS /* backups */, required=false)
    @XmlElement(name = MailConstants.E_BACKUP /* backup */, required = false)
    private List<String> backup;

    public GetContactBackupListResponse() {
        this(null);
    }

    /**
     * @param backup
     */
    public GetContactBackupListResponse(List<String> backup) {
        setBackup(backup);
    }

    /**
     * @return the backup
     */
    public List<String> getBackup() {
        return backup;
    }

    /**
     * @param backup the backup to set
     */
    public void setBackup(List<String> backup) {
        if (backup == null || backup.isEmpty()) {
            this.backup = null;
        } else {
            this.backup = backup;
        }
    }

    /**
     * @param backup the backup to add in the backup
     */
    public void addBackup(String backup) {
        if (!StringUtil.isNullOrEmpty(backup)) {
            if (this.backup == null) {
                this.backup = new ArrayList<String>();
            }
            this.backup.add(backup);
        }
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("backup", backup);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

    
}
