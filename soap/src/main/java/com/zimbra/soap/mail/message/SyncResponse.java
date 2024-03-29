// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CalendarItemInfo;
import com.zimbra.soap.mail.type.ChatSummary;
import com.zimbra.soap.mail.type.ContactInfo;
import com.zimbra.soap.mail.type.ConversationSummary;
import com.zimbra.soap.mail.type.MessageSummary;
import com.zimbra.soap.mail.type.SyncDeletedInfo;
import com.zimbra.soap.mail.type.SyncFolder;
import com.zimbra.soap.mail.type.TagInfo;
import com.zimbra.soap.type.ZmBoolean;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_SYNC_RESPONSE)
@XmlType(propOrder = {"deleted", "items"})
public class SyncResponse {

  /**
   * @zm-api-field-tag change-date
   * @zm-api-field-description Change date
   */
  @XmlAttribute(name = MailConstants.A_CHANGE_DATE /* md */, required = true)
  private long changeDate;

  /**
   * @zm-api-field-tag new-sync-token
   * @zm-api-field-description New sync token
   */
  @XmlAttribute(name = MailConstants.A_TOKEN /* token */, required = false)
  private String token;

  /**
   * @zm-api-field-tag size
   * @zm-api-field-description Size
   */
  @XmlAttribute(name = MailConstants.A_SIZE /* s */, required = false)
  private Long size;

  /**
   * @zm-api-field-tag more-flag
   * @zm-api-field-description If set, the response does <b>not</b> bring the client completely up
   *     to date. <br>
   *     More changes are still queued, and another SyncRequest (using the new returned token) is
   *     necessary.
   */
  @XmlAttribute(name = MailConstants.A_QUERY_MORE /* more */, required = false)
  private ZmBoolean more;

  /**
   * @zm-api-field-description Information on deletes
   */
  @XmlElement(name = MailConstants.E_DELETED /* deleted */, required = false)
  private SyncDeletedInfo deleted;

  /**
   * @zm-api-field-description Item information
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_FOLDER /* folder */, type = SyncFolder.class),
    @XmlElement(name = MailConstants.E_TAG /* tag */, type = TagInfo.class),
    @XmlElement(name = MailConstants.E_CONTACT /* cn */, type = ContactInfo.class),
    @XmlElement(name = MailConstants.E_APPOINTMENT /* appt */, type = CalendarItemInfo.class),
    @XmlElement(name = MailConstants.E_CONV /* c */, type = ConversationSummary.class),
    @XmlElement(name = MailConstants.E_MSG /* m */, type = MessageSummary.class),
    @XmlElement(name = MailConstants.E_CHAT /* chat */, type = ChatSummary.class)
  })
  private List<Object> items = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SyncResponse() {}

  public SyncResponse(long changeDate) {
    this.changeDate = changeDate;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public void setMore(Boolean more) {
    this.more = ZmBoolean.fromBool(more);
  }

  public void setDeleted(SyncDeletedInfo deleted) {
    this.deleted = deleted;
  }

  public void setItems(Iterable<Object> items) {
    this.items.clear();
    if (items != null) {
      Iterables.addAll(this.items, items);
    }
  }

  public SyncResponse addItem(Object item) {
    this.items.add(item);
    return this;
  }

  public long getChangeDate() {
    return changeDate;
  }

  public String getToken() {
    return token;
  }

  public Long getSize() {
    return size;
  }

  public Boolean getMore() {
    return ZmBoolean.toBool(more);
  }

  public SyncDeletedInfo getDeleted() {
    return deleted;
  }

  public List<Object> getItems() {
    return Collections.unmodifiableList(items);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("changeDate", changeDate)
        .add("token", token)
        .add("size", size)
        .add("more", more)
        .add("deleted", deleted)
        .add("items", items);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
