// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.LoggerInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_ADD_ACCOUNT_LOGGER_RESPONSE)
public class AddAccountLoggerResponse {

    /**
     * @zm-api-field-description Information on loggers
     */
    @XmlElement(name=AdminConstants.E_LOGGER /* logger */, required=false)
    private List<LoggerInfo> loggers = Lists.newArrayList();

    private AddAccountLoggerResponse() {
    }

    private AddAccountLoggerResponse(Iterable <LoggerInfo> loggers) {
        setLoggers(loggers);
    }

    public static AddAccountLoggerResponse create(Iterable <LoggerInfo> loggers) {
        return new AddAccountLoggerResponse(loggers);
    }

    public void setLoggers(Iterable <LoggerInfo> loggers) {
        this.loggers.clear();
        if (loggers != null) {
            Iterables.addAll(this.loggers,loggers);
        }
    }

    public void addLogger(LoggerInfo logger) {
        this.loggers.add(logger);
    }

    public List<LoggerInfo> getLoggers() {
        return Collections.unmodifiableList(loggers);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("loggers", loggers);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
