// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

public class ZContentDisposition extends ZCompoundHeader {
    private static final String ATTACHMENT = "attachment";
    private static final String INLINE     = "inline";

    private String disposition;

    public ZContentDisposition(String value) {
        super("Content-Disposition", value);
        normalizeDisposition();
    }

    public ZContentDisposition(String value, boolean use2231) {
        super("Content-Disposition", value, use2231);
        normalizeDisposition();
    }

    ZContentDisposition(String name, byte[] content, int start, String defaultType) {
        super(name, content, start);
        normalizeDisposition();
    }

    public ZContentDisposition(ZInternetHeader header) {
        super(header);
        normalizeDisposition();
    }

    @Override
    protected ZContentDisposition clone() {
        return new ZContentDisposition(this);
    }


    public ZContentDisposition setDisposition(String disposition) {
        return setPrimaryValue(disposition);
    }

    @Override
    public ZContentDisposition setPrimaryValue(String value) {
        super.setPrimaryValue(value);
        normalizeDisposition();
        return this;
    }

    @Override
    public ZContentDisposition setParameter(String name, String value) {
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

    @Override
    protected void reserialize() {
        if (content == null) {
            super.setPrimaryValue(getDisposition());
            super.reserialize();
        }
    }

    @Override
    public ZContentDisposition cleanup() {
        super.setPrimaryValue(getDisposition());
        super.cleanup();
        return this;
    }
}