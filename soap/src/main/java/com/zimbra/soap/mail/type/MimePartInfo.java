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
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"mimeParts", "attachments"})
@GraphQLType(name = GqlConstants.CLASS_MIME_PART_INFO, description = "The mime part information")
public class MimePartInfo {

  /**
   * @zm-api-field-tag content-type
   * @zm-api-field-description Content type
   */
  @XmlAttribute(name = MailConstants.A_CONTENT_TYPE /* ct */, required = false)
  private String contentType;

  /**
   * @zm-api-field-tag content
   * @zm-api-field-description Content
   */
  @XmlAttribute(name = MailConstants.E_CONTENT /* content */, required = false)
  private String content;

  /**
   * @zm-api-field-tag content-id
   * @zm-api-field-description Content ID
   */
  @XmlAttribute(name = MailConstants.A_CONTENT_ID /* ci */, required = false)
  private String contentId;

  /**
   * @zm-api-field-description MIME Parts
   */
  @XmlElement(name = MailConstants.E_MIMEPART /* mp */, required = false)
  @GraphQLQuery(name = GqlConstants.MIME_PARTS, description = "Mime Parts")
  private final List<MimePartInfo> mimeParts = Lists.newArrayList();

  /**
   * @zm-api-field-description Attachments
   */
  @XmlElement(name = MailConstants.E_ATTACH /* attach */, required = false)
  private AttachmentsInfo attachments;

  public MimePartInfo() {}

  public static MimePartInfo createForContentType(String ct) {
    final MimePartInfo mp = new MimePartInfo();
    mp.setContentType(ct);
    return mp;
  }

  public static MimePartInfo createForContentTypeAndContent(String ct, String text) {
    final MimePartInfo mp = createForContentType(ct);
    mp.setContent(text);
    return mp;
  }

  @GraphQLInputField(name = GqlConstants.CONTENT_TYPE, description = "Content Type")
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @GraphQLInputField(name = GqlConstants.CONTENT, description = "Content")
  public void setContent(String content) {
    this.content = content;
  }

  @GraphQLInputField(name = GqlConstants.CONTENT_ID, description = "Content ID")
  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  @GraphQLInputField(name = GqlConstants.MIME_PARTS, description = "Mime Parts")
  public void setMimeParts(Iterable<MimePartInfo> mimeParts) {
    this.mimeParts.clear();
    if (mimeParts != null) {
      Iterables.addAll(this.mimeParts, mimeParts);
    }
  }

  @GraphQLIgnore
  public void addMimePart(MimePartInfo mimePart) {
    this.mimeParts.add(mimePart);
  }

  @GraphQLInputField(name = GqlConstants.ATTACHMENTS, description = "Attachments")
  public void setAttachments(AttachmentsInfo attachments) {
    this.attachments = attachments;
  }

  @GraphQLQuery(name = GqlConstants.CONTENT_TYPE, description = "Content Type")
  public String getContentType() {
    return contentType;
  }

  @GraphQLQuery(name = GqlConstants.CONTENT, description = "Content")
  public String getContent() {
    return content;
  }

  @GraphQLQuery(name = GqlConstants.CONTENT_ID, description = "Content ID")
  public String getContentId() {
    return contentId;
  }

  @GraphQLQuery(name = GqlConstants.MIME_PARTS, description = "Mime Parts")
  public List<MimePartInfo> getMimeParts() {
    return mimeParts;
  }

  @GraphQLQuery(name = GqlConstants.ATTACHMENTS, description = "Attachments")
  public AttachmentsInfo getAttachments() {
    return attachments;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("contentType", contentType)
        .add("content", content)
        .add("contentId", contentId)
        .add("mimeParts", mimeParts)
        .add("attachments", attachments);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
