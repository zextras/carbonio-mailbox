// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.doc;

import com.zimbra.common.service.ServiceException;

@SuppressWarnings("serial")
public class DocServiceException extends ServiceException {

    public static final String ERROR = "doc.ERROR";

    private DocServiceException(String message, String code, boolean isReceiversFault, Argument... args) {
        super(message, code, isReceiversFault, args);
    }
    private DocServiceException(String message, String code, boolean isReceiversFault, Throwable cause) {
        super(message, code, isReceiversFault, cause);
    }
    public static DocServiceException ERROR(String w) {
        return new DocServiceException("error: "+ w, ERROR, SENDERS_FAULT);
    }
    public static DocServiceException ERROR(String w, Throwable cause) {
        return new DocServiceException("error: "+ w, ERROR, SENDERS_FAULT, cause);
    }
    public static DocServiceException INVALID_PATH(String path) {
        return new DocServiceException("invalid path: "+ path, ERROR, SENDERS_FAULT);
    }
}
