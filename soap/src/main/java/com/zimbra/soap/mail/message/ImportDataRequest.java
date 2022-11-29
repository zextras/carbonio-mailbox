// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CalDataSourceNameOrId;
import com.zimbra.soap.mail.type.CaldavDataSourceNameOrId;
import com.zimbra.soap.mail.type.DataSourceNameOrId;
import com.zimbra.soap.mail.type.GalDataSourceNameOrId;
import com.zimbra.soap.mail.type.ImapDataSourceNameOrId;
import com.zimbra.soap.mail.type.Pop3DataSourceNameOrId;
import com.zimbra.soap.mail.type.RssDataSourceNameOrId;
import com.zimbra.soap.mail.type.YabDataSourceNameOrId;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Triggers the specified data sources to kick off their import processes.
 * Data import runs asynchronously, so the response immediately returns.  Status of an import can be queried via
 * the <b>&lt;GetImportStatusRequest></b> message.  If the server receives an <b>&lt;ImportDataRequest></b> while
 * an import is already running for a given data source, the second request is ignored.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_IMPORT_DATA_REQUEST)
public class ImportDataRequest {

    /**
     * @zm-api-field-description
     */
    @XmlElements({
        @XmlElement(name=MailConstants.E_DS_IMAP /* imap */, type=ImapDataSourceNameOrId.class),
        @XmlElement(name=MailConstants.E_DS_POP3 /* pop3 */, type=Pop3DataSourceNameOrId.class),
        @XmlElement(name=MailConstants.E_DS_CALDAV /* caldav */, type=CaldavDataSourceNameOrId.class),
        @XmlElement(name=MailConstants.E_DS_YAB /* yab */, type=YabDataSourceNameOrId.class),
        @XmlElement(name=MailConstants.E_DS_RSS /* rss */, type=RssDataSourceNameOrId.class),
        @XmlElement(name=MailConstants.E_DS_GAL /* gal */, type=GalDataSourceNameOrId.class),
        @XmlElement(name=MailConstants.E_DS_CAL /* cal */, type=CalDataSourceNameOrId.class),
        @XmlElement(name=MailConstants.E_DS_UNKNOWN /* unknown */, type=DataSourceNameOrId.class)
    })
    private List<DataSourceNameOrId> dataSources = Lists.newArrayList();

    public ImportDataRequest() {
    }

    public void setDataSources(Iterable <DataSourceNameOrId> dataSources) {
        this.dataSources.clear();
        if (dataSources != null) {
            Iterables.addAll(this.dataSources,dataSources);
        }
    }

    public void addDataSource(DataSourceNameOrId dataSource) {
        this.dataSources.add(dataSource);
    }

    public List<DataSourceNameOrId> getDataSources() {
        return dataSources;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("dataSources", dataSources);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
