// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.mail.Action;

import java.util.List;

/**
 */
public class ActionNotify implements Action {

    private final String emailAddr;
    private final String subjectTemplate;
    private final String bodyTemplate;
    // -1 implies no limit
    private final int maxBodyBytes;
    private final List<String> origHeaders;

    public ActionNotify(
            String emailAddr, String subjectTemplate, String bodyTemplate, int maxBodyBytes, List<String> origHeaders) {
        this.emailAddr = emailAddr;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
        this.maxBodyBytes = maxBodyBytes;
        this.origHeaders = origHeaders;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public int getMaxBodyBytes() {
        return maxBodyBytes;
    }

    public List<String> getOrigHeaders() {
        return origHeaders;
    }
}
