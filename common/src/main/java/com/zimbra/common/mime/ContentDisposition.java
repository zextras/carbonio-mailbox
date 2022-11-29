// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

public class ContentDisposition extends MimeCompoundHeader {
    private static final String ATTACHMENT = "attachment";
    private static final String INLINE     = "inline";

    private String disposition;

    public ContentDisposition(String value) {
        super("Content-Disposition", value);
        normalizeDisposition();
    }

    public ContentDisposition(String value, boolean use2231) {
        super("Content-Disposition", value, use2231);
        normalizeDisposition();
    }

    ContentDisposition(String name, byte[] content, int start, String defaultType) {
        super(name, content, start);
        normalizeDisposition();
    }

    public ContentDisposition(MimeHeader header) {
        super(header);
        normalizeDisposition();
    }

    @Override protected ContentDisposition clone() {
        return new ContentDisposition(this);
    }


    public ContentDisposition setDisposition(String disposition) {
        return setPrimaryValue(disposition);
    }

    @Override public ContentDisposition setPrimaryValue(String value) {
        super.setPrimaryValue(value);
        normalizeDisposition();
        return this;
    }

    @Override public ContentDisposition setParameter(String name, String value) {
        super.setParameter(name, value);
        return this;
    }

    public String getDisposition() {
        return disposition;
    }

    private void normalizeDisposition() {
        String value = getPrimaryValue() == null ? "" : getPrimaryValue().trim().toLowerCase();
        this.disposition = value.equals(ATTACHMENT) || value.equals(INLINE) ? value : ATTACHMENT;
    }

    @Override protected void reserialize() {
        if (content == null) {
            super.setPrimaryValue(getDisposition());
            super.reserialize();
        }
    }

    @Override public ContentDisposition cleanup() {
        super.setPrimaryValue(getDisposition());
        super.cleanup();
        return this;
    }
}