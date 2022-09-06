// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Apr 18, 2004
 */
package com.zimbra.cs.mime;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.mime.ContentType;
import com.zimbra.common.mime.MimeConstants;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.MimePart;

public class MPartInfo {
  MimePart mPart;
  MPartInfo mParent;
  List<MPartInfo> mChildren;
  String mPartName;
  String mContentType;
  String mDisposition;
  String mFilename;
  int mPartNum;
  int mSize;
  boolean mIsFilterableAttachment;
  boolean mIsToplevelAttachment;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MPartInfo: {");
    sb.append("partName: ").append(mPartName).append(", ");
    sb.append("contentType: ").append(mContentType).append(", ");
    sb.append("size: ").append(mSize).append(", ");
    sb.append("disposition: ").append(mDisposition).append(", ");
    sb.append("filename: ").append(mFilename).append(", ");
    sb.append("partNum: ").append(mPartNum).append(", ");
    sb.append("isFilterableAttachment: ").append(mIsFilterableAttachment);
    sb.append("isToplevelAttachment: ").append(mIsToplevelAttachment);
    sb.append("}");
    return sb.toString();
  }

  /**
   * Returns true if we consider this to be an attachment for the sake of "filtering" by
   * attachments. i.e., if someone searches for messages with attachment types of "text/plain", we
   * probably wouldn't want every multipart/mixed message showing up, since 99% of them will have a
   * first body part of text/plain.
   *
   * @param part
   * @return
   */
  public boolean isFilterableAttachment() {
    return mIsFilterableAttachment;
  }

  public boolean isTopLevelAttachment() {
    return mIsToplevelAttachment;
  }

  public MimePart getMimePart() {
    return mPart;
  }

  public MPartInfo getParent() {
    return mParent;
  }

  public boolean hasChildren() {
    return mChildren != null && !mChildren.isEmpty();
  }

  public List<MPartInfo> getChildren() {
    return mChildren;
  }

  public String getPartName() {
    return mPartName;
  }

  public int getPartNum() {
    return mPartNum;
  }

  public int getSize() {
    return mSize;
  }

  public String getContentType() {
    return mContentType;
  }

  @VisibleForTesting
  String getFullContentType() {
    try {
      return mPart.getContentType();
    } catch (MessagingException e) {
      return mContentType;
    }
  }

  public boolean isMultipart() {
    return mContentType.startsWith(MimeConstants.CT_MULTIPART_PREFIX);
  }

  public boolean isMessage() {
    return mContentType.equals(MimeConstants.CT_MESSAGE_RFC822);
  }

  public String getContentTypeParameter(String name) {
    try {
      return new ContentType(mPart.getContentType()).getParameter(name);
    } catch (MessagingException e) {
      return null;
    }
  }

  public String getContentID() {
    try {
      return mPart.getContentID();
    } catch (MessagingException me) {
      return null;
    }
  }

  public String getDisposition() {
    return mDisposition;
  }

  public String getFilename() {
    return mFilename;
  }
}
