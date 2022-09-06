// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.zimbra.common.soap.MailConstants;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ImportContact {

  private List<String> listOfCreatedIds = new ArrayList<String>();

  /**
   * @zm-api-field-tag num-imported
   * @zm-api-field-description Number imported
   */
  @XmlAttribute(name = MailConstants.A_NUM /* n */, required = false)
  private long numImported;

  public ImportContact() {}

  private static Splitter COMMA_SPLITTER = Splitter.on(",");
  private static Joiner COMMA_JOINER = Joiner.on(",");

  /**
   * @zm-api-field-tag comma-sep-created-ids
   * @zm-api-field-description Comma-separated list of created IDs
   */
  @XmlAttribute(name = MailConstants.A_IDS /* ids */, required = true)
  public String getListOfCreatedIds() {
    return COMMA_JOINER.join(listOfCreatedIds);
  }

  public void setListOfCreatedIds(String commaSepIds) {
    for (String id : COMMA_SPLITTER.split(commaSepIds)) {
      addCreatedId(id);
    }
  }

  public void addCreatedId(String id) {
    listOfCreatedIds.add(id);
  }

  public long getNumImported() {
    return numImported;
  }

  public void setNumImported(long numImported) {
    this.numImported = numImported;
  }
}
