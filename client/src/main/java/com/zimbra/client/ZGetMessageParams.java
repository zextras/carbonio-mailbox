// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import java.util.Objects;

public class ZGetMessageParams {

    private String mId;
    private boolean mMarkRead;
    private boolean mWantHtml;
    private boolean mNeuterImages;
    private boolean mRawContent;
    private String mPart;
    private Integer mMax;
    private String mRequestHeaders;
    
    public ZGetMessageParams() { }

    public boolean isWantHtml() {
        return mWantHtml;
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
        if (mPart != null)
            return (mId+mPart).hashCode();
        else
            return mId.hashCode();
    }

	public String getReqHeaders() {
        return this.mRequestHeaders;
    }
    /**
     *
     * @return true if get message params are equal.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (! (obj instanceof ZGetMessageParams)) return false;

        ZGetMessageParams that = (ZGetMessageParams) obj;

        if (this.mNeuterImages != that.mNeuterImages ||
            this.mRawContent != that.mRawContent ||
            this.mWantHtml != that.mWantHtml ||
            this.mMarkRead != that.mMarkRead ||
            !Objects.equals(this.mId, that.mId)) return false;
        return Objects.equals(this.mPart, that.mPart) &&
            this.mMax == that.mMax;
    }

}
