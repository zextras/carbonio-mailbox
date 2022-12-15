// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.LoggerInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ACCOUNT_LOGGERS_RESPONSE)
public class GetAccountLoggersResponse {

    /**
     * @zm-api-field-description Information for custom loggers created for the given account since the last server
     * start.
     */
    @XmlElement(name=AdminConstants.E_LOGGER, required=false)
    private List <LoggerInfo> loggers = Lists.newArrayList();

    public GetAccountLoggersResponse() {
        this((Collection<LoggerInfo>) null);
    }

    public GetAccountLoggersResponse(Collection<LoggerInfo> loggers) {
        setLoggers(loggers);
    }

    public GetAccountLoggersResponse setLoggers(Collection<LoggerInfo> loggers) {
        this.loggers.clear();
        if (loggers != null) {
            this.loggers.addAll(loggers);
        }
        return this;
    }

    public GetAccountLoggersResponse addLogger(LoggerInfo logger) {
        loggers.add(logger);
        return this;
    }

    public List<LoggerInfo> getLoggers() {
        return Collections.unmodifiableList(loggers);
    }
}
