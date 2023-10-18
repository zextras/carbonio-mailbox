// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ImapDataSource;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAttribute;

public class MailImapDataSource extends MailDataSource implements ImapDataSource {

  /**
   * @zm-api-field-tag test-data-source
   * @zm-api-field-description boolean field for client to denote if it wants to test the data
   *     source before creating
   */
  @XmlAttribute(name = MailConstants.A_DS_TEST, required = false)
  private ZmBoolean test;

  public MailImapDataSource() {}

  public MailImapDataSource(ImapDataSource data) {
    super(data);
  }

  public void setTest(boolean test) {
    this.test = ZmBoolean.fromBool(test, false);
  }

  public boolean isTest() {
    return ZmBoolean.toBool(test, false);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
