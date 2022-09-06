// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.util.StringUtil;

public class ZGetMessageParams {

  private String mId;
  private boolean mMarkRead;
  private boolean mWantHtml;
  private boolean mNeuterImages;
  private boolean mRawContent;
  private String mPart;
  private Integer mMax;
  private String mRequestHeaders;

  public ZGetMessageParams() {}

  public boolean isWantHtml() {
    return mWantHtml;
  }

  public void setWantHtml(boolean wantHtml) {
    mWantHtml = wantHtml;
  }

  public String getId() {
    return mId;
  }

  public void setId(String id) {
    mId = id;
  }

  public boolean isMarkRead() {
    return mMarkRead;
  }

  public void setMarkRead(boolean markRead) {
    mMarkRead = markRead;
  }

  public boolean isNeuterImages() {
    return mNeuterImages;
  }

  public void setNeuterImages(boolean neuterImages) {
    this.mNeuterImages = neuterImages;
  }

  public boolean isRawContent() {
    return mRawContent;
  }

  public void setRawContent(boolean rawContent) {
    mRawContent = rawContent;
  }

  public String getPart() {
    return mPart;
  }

  public void setPart(String part) {
    mPart = part;
  }

  public Integer getMax() {
    return mMax;
  }

  public void setMax(Integer max) {
    mMax = max;
  }

  public int hashCode() {
    if (mPart != null) return (mId + mPart).hashCode();
    else return mId.hashCode();
  }

  public void setReqHeaders(String reqHeaders) {
    this.mRequestHeaders = reqHeaders;
  }

  public String getReqHeaders() {
    return this.mRequestHeaders;
  }
  /**
   * @return true if get message params are equal.
   */
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof ZGetMessageParams)) return false;

    ZGetMessageParams that = (ZGetMessageParams) obj;

    return this.mNeuterImages == that.mNeuterImages
        && this.mRawContent == that.mRawContent
        && this.mWantHtml == that.mWantHtml
        && this.mMarkRead == that.mMarkRead
        && StringUtil.equal(this.mId, that.mId)
        && StringUtil.equal(this.mPart, that.mPart)
        && this.mMax == that.mMax;
  }

  public ZGetMessageParams(ZGetMessageParams that) {
    this.mId = that.mId;
    this.mWantHtml = that.mWantHtml;
    this.mMarkRead = that.mMarkRead;
    this.mNeuterImages = that.mNeuterImages;
    this.mRawContent = that.mRawContent;
    this.mPart = that.mPart;
  }
}
