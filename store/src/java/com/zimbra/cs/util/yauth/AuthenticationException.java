// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.yauth;

import com.zimbra.common.service.ServiceException;

@SuppressWarnings("serial")
public class AuthenticationException extends ServiceException {
    private final ErrorCode code;
    private String captchaUrl;
    private String captchaData;

    public AuthenticationException(ErrorCode code, String msg) {
        super(msg, "yauth." + code.name(), false);
        this.code = code;
    }

    public AuthenticationException(ErrorCode code) {
        this(code, code.getDescription());
    }
    
    public ErrorCode getErrorCode() {
        return code;
    }

    public void setCaptchaUrl(String url) {
        captchaUrl = url;
    }

    public void setCaptchaData(String data) {
        captchaData = data;
    }

    public String getCaptchaUrl() { return captchaUrl; }
    public String getCaptchaData() { return captchaData; }
}
