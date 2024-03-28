// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class MailboxStats {

    /**
     * @zm-api-field-tag num-mailboxes
     * @zm-api-field-description Total number of mailboxes
     */
    @XmlAttribute(name=AdminConstants.A_NUM_MBOXES /* numMboxes */, required=true)
    private final long numMboxes;

    /**
     * @zm-api-field-tag total-size
     * @zm-api-field-description Total size of all mailboxes
     */
    @XmlAttribute(name=AdminConstants.A_TOTAL_SIZE /* totalSize */, required=true)
    private final long totalSize;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MailboxStats() {
        this(0L, 0L);
    }

    public MailboxStats(long numMboxes, long totalSize) {
        this.numMboxes = numMboxes;
        this.totalSize = totalSize;
    }

    public long getNumMboxes() { return numMboxes; }
    public long getTotalSize() { return totalSize; }
}
