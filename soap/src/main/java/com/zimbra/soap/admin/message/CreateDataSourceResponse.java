// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DataSourceInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_DATA_SOURCE_RESPONSE)
public class CreateDataSourceResponse {

    /**
     * @zm-api-field-description Details of created data source
     */
    @XmlElement(name=AccountConstants.E_DATA_SOURCE, required=true)
    private final DataSourceInfo dataSource;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CreateDataSourceResponse() {
        this(null);
    }

    public CreateDataSourceResponse(DataSourceInfo dataSource) {
        this.dataSource = dataSource;
    }

    public DataSourceInfo getDataSource() { return dataSource; }
}
