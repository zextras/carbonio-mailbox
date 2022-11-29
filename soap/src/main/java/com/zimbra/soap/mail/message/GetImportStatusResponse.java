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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CaldavImportStatusInfo;
import com.zimbra.soap.mail.type.CalImportStatusInfo;
import com.zimbra.soap.mail.type.GalImportStatusInfo;
import com.zimbra.soap.mail.type.ImapImportStatusInfo;
import com.zimbra.soap.mail.type.ImportStatusInfo;
import com.zimbra.soap.mail.type.Pop3ImportStatusInfo;
import com.zimbra.soap.mail.type.RssImportStatusInfo;
import com.zimbra.soap.mail.type.UnknownImportStatusInfo;
import com.zimbra.soap.mail.type.YabImportStatusInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_IMPORT_STATUS_RESPONSE)
public class GetImportStatusResponse {

    /**
     * @zm-api-field-description Import Status information
     */
    @XmlElements({
        @XmlElement(name=MailConstants.E_DS_IMAP /* imap */, type=ImapImportStatusInfo.class),
        @XmlElement(name=MailConstants.E_DS_POP3 /* pop3 */, type=Pop3ImportStatusInfo.class),
        @XmlElement(name=MailConstants.E_DS_CALDAV /* caldav */, type=CaldavImportStatusInfo.class),
        @XmlElement(name=MailConstants.E_DS_YAB /* yab */, type=YabImportStatusInfo.class),
        @XmlElement(name=MailConstants.E_DS_RSS /* rss */, type=RssImportStatusInfo.class),
        @XmlElement(name=MailConstants.E_DS_GAL /* gal */, type=GalImportStatusInfo.class),
        @XmlElement(name=MailConstants.E_DS_CAL /* cal */, type=CalImportStatusInfo.class),
        @XmlElement(name=MailConstants.E_DS_UNKNOWN /* unknown */, type=UnknownImportStatusInfo.class)
    })
    private List<ImportStatusInfo> statuses = Lists.newArrayList();

    public GetImportStatusResponse() {
    }

    public void setStatuses(Iterable <ImportStatusInfo> statuses) {
        this.statuses.clear();
        if (statuses != null) {
            Iterables.addAll(this.statuses,statuses);
        }
    }

    public GetImportStatusResponse addStatuse(ImportStatusInfo statuse) {
        this.statuses.add(statuse);
        return this;
    }

    public List<ImportStatusInfo> getStatuses() {
        return Collections.unmodifiableList(statuses);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("statuses", statuses);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
