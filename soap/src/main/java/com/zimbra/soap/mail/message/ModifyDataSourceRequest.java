// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Changes attributes of the given data source. Only the attributes
 *     specified in the request are modified. If the username, host or leaveOnServer settings are
 *     modified, the server wipes out saved state for this data source. As a result, any previously
 *     downloaded messages that are still stored on the remote server will be downloaded again.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_MODIFY_DATA_SOURCE_REQUEST)
public class ModifyDataSourceRequest {

  /**
   * @zm-api-field-description Specification of data source changes
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
  private DataSource dataSource;

  public ModifyDataSourceRequest() {}

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("dataSource", dataSource);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
