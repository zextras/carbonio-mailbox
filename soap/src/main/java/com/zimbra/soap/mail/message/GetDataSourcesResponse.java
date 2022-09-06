// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.MailCalDataSource;
import com.zimbra.soap.mail.type.MailCaldavDataSource;
import com.zimbra.soap.mail.type.MailDataSource;
import com.zimbra.soap.mail.type.MailGalDataSource;
import com.zimbra.soap.mail.type.MailImapDataSource;
import com.zimbra.soap.mail.type.MailPop3DataSource;
import com.zimbra.soap.mail.type.MailRssDataSource;
import com.zimbra.soap.mail.type.MailYabDataSource;
import com.zimbra.soap.type.DataSource;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_DATA_SOURCES_RESPONSE)
public class GetDataSourcesResponse {

  /**
   * @zm-api-field-description Data source information
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_DS_IMAP /* imap */, type = MailImapDataSource.class),
    @XmlElement(name = MailConstants.E_DS_POP3 /* pop3 */, type = MailPop3DataSource.class),
    @XmlElement(name = MailConstants.E_DS_CALDAV /* caldav */, type = MailCaldavDataSource.class),
    @XmlElement(name = MailConstants.E_DS_YAB /* yab */, type = MailYabDataSource.class),
    @XmlElement(name = MailConstants.E_DS_RSS /* rss */, type = MailRssDataSource.class),
    @XmlElement(name = MailConstants.E_DS_GAL /* gal */, type = MailGalDataSource.class),
    @XmlElement(name = MailConstants.E_DS_CAL /* cal */, type = MailCalDataSource.class),
    @XmlElement(name = MailConstants.E_DS_UNKNOWN /* unknown */, type = MailDataSource.class)
  })
  private List<DataSource> dataSources = Lists.newArrayList();

  public GetDataSourcesResponse() {}

  public void setDataSources(Iterable<DataSource> dataSources) {
    this.dataSources.clear();
    if (dataSources != null) {
      Iterables.addAll(this.dataSources, dataSources);
    }
  }

  public GetDataSourcesResponse addDataSource(DataSource dataSource) {
    this.dataSources.add(dataSource);
    return this;
  }

  public List<DataSource> getDataSources() {
    return Collections.unmodifiableList(dataSources);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("dataSources", dataSources);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
