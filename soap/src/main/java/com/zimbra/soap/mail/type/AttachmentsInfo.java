// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"attachments", "extraElements"})
@GraphQLType(name = GqlConstants.CLASS_ATTACHMENTS_INFO, description = "Attachments Information")
public class AttachmentsInfo {

  /**
   * @zm-api-field-tag attach-upload-id
   * @zm-api-field-description Attachment upload ID
   */
  @XmlAttribute(name = MailConstants.A_ATTACHMENT_ID /* aid */, required = false)
  private String attachmentId;

  /**
   * @zm-api-field-description Attachment details
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_MIMEPART /* mp */, type = MimePartAttachSpec.class),
    @XmlElement(name = MailConstants.E_MSG /* m */, type = MsgAttachSpec.class),
    @XmlElement(name = MailConstants.E_CONTACT /* cn */, type = ContactAttachSpec.class),
    @XmlElement(name = MailConstants.E_DOC /* doc */, type = DocAttachSpec.class)
  })
  private final List<AttachSpec> attachments = Lists.newArrayList();

  /**
   * @zm-api-field-description Other elements
   */
  @XmlAnyElement private final List<org.w3c.dom.Element> extraElements = Lists.newArrayList();

  public AttachmentsInfo() {}

  @GraphQLInputField(name = GqlConstants.ATTACHMENT_ID, description = "Attachment upload ID")
  public void setAttachmentId(String attachmentId) {
    this.attachmentId = attachmentId;
  }

  @GraphQLInputField(name = GqlConstants.ATTACHMENTS, description = "Attachment details")
  public void setAttachments(Iterable<AttachSpec> attachments) {
    this.attachments.clear();
    if (attachments != null) {
      Iterables.addAll(this.attachments, attachments);
    }
  }

  @GraphQLIgnore
  public AttachmentsInfo addAttachment(AttachSpec attachment) {
    this.attachments.add(attachment);
    return this;
  }

  @GraphQLIgnore
  public void setExtraElements(Iterable<org.w3c.dom.Element> extraElements) {
    this.extraElements.clear();
    if (extraElements != null) {
      Iterables.addAll(this.extraElements, extraElements);
    }
  }

  @GraphQLIgnore
  public AttachmentsInfo addExtraElement(org.w3c.dom.Element extraElement) {
    this.extraElements.add(extraElement);
    return this;
  }

  @GraphQLQuery(name = GqlConstants.ATTACHMENT_ID, description = "Attachment upload ID")
  public String getAttachmentId() {
    return attachmentId;
  }

  @GraphQLQuery(name = GqlConstants.ATTACHMENTS, description = "Attachment details")
  public List<AttachSpec> getAttachments() {
    return Collections.unmodifiableList(attachments);
  }

  @GraphQLIgnore
  public List<org.w3c.dom.Element> getExtraElements() {
    return Collections.unmodifiableList(extraElements);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("attachmentId", attachmentId)
        .add("attachments", attachments)
        .add("extraElements", extraElements);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
