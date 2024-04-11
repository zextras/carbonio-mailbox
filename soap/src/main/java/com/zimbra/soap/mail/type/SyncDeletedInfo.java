// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

@XmlAccessorType(XmlAccessType.NONE)
public class SyncDeletedInfo {

  /**
   * @zm-api-field-tag deleted-ids
   * @zm-api-field-description IDs of deleted items
   */
  @XmlAttribute(name = MailConstants.A_IDS /* ids */, required = true)
  private final String ids;

  /**
   * @zm-api-field-description Details of deletes broken down by item type (present if "typed" was
   *     specified in the request)
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_FOLDER /* folder */, type = FolderIdsAttr.class),
    @XmlElement(name = MailConstants.E_SEARCH /* search */, type = SearchFolderIdsAttr.class),
    @XmlElement(name = MailConstants.E_MOUNT /* link */, type = MountIdsAttr.class),
    @XmlElement(name = MailConstants.E_TAG /* tag */, type = TagIdsAttr.class),
    @XmlElement(name = MailConstants.E_CONV /* c */, type = ConvIdsAttr.class),
    @XmlElement(name = MailConstants.E_CHAT /* chat */, type = ChatIdsAttr.class),
    @XmlElement(name = MailConstants.E_MSG /* m */, type = MsgIdsAttr.class),
    @XmlElement(name = MailConstants.E_CONTACT /* cn */, type = ContactIdsAttr.class),
    @XmlElement(name = MailConstants.E_APPOINTMENT /* appt */, type = ApptIdsAttr.class)
  })
  private List<IdsAttr> types = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SyncDeletedInfo() {
    this(null);
  }

  public SyncDeletedInfo(String ids) {
    this.ids = ids;
  }

  public void setTypes(Iterable<IdsAttr> types) {
    this.types.clear();
    if (types != null) {
      Iterables.addAll(this.types, types);
    }
  }

  public SyncDeletedInfo addType(IdsAttr type) {
    this.types.add(type);
    return this;
  }

  public String getIds() {
    return ids;
  }

  public List<IdsAttr> getTypes() {
    return Collections.unmodifiableList(types);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("ids", ids).add("types", types);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
