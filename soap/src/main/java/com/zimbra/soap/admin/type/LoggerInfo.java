// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.LoggerInfo;
import com.zimbra.soap.type.LoggingLevel;

@XmlAccessorType(XmlAccessType.NONE)
public final class LoggerInfo {

    /**
     * @zm-api-field-tag category-name
     * @zm-api-field-description name of the logger category
     */
    @XmlAttribute(name=AdminConstants.A_CATEGORY, required=true)
    private String category;
    /**
     * @zm-api-field-description level of the logging.
     */
    @XmlAttribute(name=AdminConstants.A_LEVEL, required=false)
    private LoggingLevel level;

    /**
     * no-argument constructor wanted by JAXB
     */
    private LoggerInfo() {
        this((String) null, (LoggingLevel) null);
    }

    private LoggerInfo(String category, LoggingLevel level) {
        this.category = category;
        this.level = level;
    }

    public static LoggerInfo createForCategoryAndLevel(String category, LoggingLevel level) {
        return new LoggerInfo(category, level);
    }

    public static LoggerInfo createForCategoryAndLevelString(String category, String level) throws ServiceException {
        return new LoggerInfo(category, LoggingLevel.fromString(level));
    }

    public String getCategory() { return category; }
    public LoggingLevel getLevel() { return level; }
}
