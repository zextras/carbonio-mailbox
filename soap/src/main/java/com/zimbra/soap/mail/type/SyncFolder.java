// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class SyncFolder extends Folder {

  /**
   * @zm-api-field-description list of item ids in the folder
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_TAG /* tag */, type = TagIdsAttr.class),
    @XmlElement(name = MailConstants.E_CONV /* c */, type = ConvIdsAttr.class),
    @XmlElement(name = MailConstants.E_CHAT /* chat */, type = ChatIdsAttr.class),
    @XmlElement(name = MailConstants.E_MSG /* m */, type = MsgIdsAttr.class),
    @XmlElement(name = MailConstants.E_CONTACT /* cn */, type = ContactIdsAttr.class),
    @XmlElement(name = MailConstants.E_APPOINTMENT /* appt */, type = ApptIdsAttr.class),
    @XmlElement(name = MailConstants.E_TASK /* task */, type = TaskIdsAttr.class),
    @XmlElement(name = MailConstants.E_NOTES /* notes */, type = NoteIdsAttr.class),
    @XmlElement(name = MailConstants.E_WIKIWORD /* w */, type = WikiIdsAttr.class),
    @XmlElement(name = MailConstants.E_DOC /* doc */, type = DocIdsAttr.class)
  })
  private List<IdsAttr> itemIds = Lists.newArrayList();

  public List<IdsAttr> getItemIds() {
    return itemIds;
  }

  public void setItemIds(List<IdsAttr> itemIds) {
    this.itemIds = itemIds;
  }

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SyncFolder() {}

  public SyncFolder(List<IdsAttr> itemIds) {
    this.itemIds = itemIds;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("itemIds", itemIds);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
