// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.triton;

import com.zimbra.common.util.ZimbraLog;

/**
 * String wrapper for passing upload URL between output stream instantiations
 *
 */
public class TritonUploadUrl {
    private String uploadUrl = null;

    public TritonUploadUrl() {
        super();
    }

    public void setUploadUrl(String uploadUrl) {
        if (isInitialized()) {
            ZimbraLog.store.warn("TritonUploadUrl already set to %s but changing to %s", this.uploadUrl, uploadUrl);
        }
        this.uploadUrl = uploadUrl;
    }

    public boolean isInitialized() {
        return uploadUrl != null;
    }

    @Override
    public String toString() {
        return uploadUrl;
    }
}
