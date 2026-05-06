// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DataSourceInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_DATA_SOURCES_RESPONSE)
public class GetDataSourcesResponse {

    /**
     * @zm-api-field-description Information on data sources
     */
    @XmlElement(name=AccountConstants.E_DATA_SOURCE, required=false)
    private List<DataSourceInfo> dataSources = Lists.newArrayList();

    public GetDataSourcesResponse() {
    }

    public void setDataSources(Iterable <DataSourceInfo> dataSources) {
        this.dataSources.clear();
        if (dataSources != null) {
            Iterables.addAll(this.dataSources,dataSources);
        }
    }

    public GetDataSourcesResponse addDataSource(DataSourceInfo dataSource) {
        this.dataSources.add(dataSource);
        return this;
    }

    public List<DataSourceInfo> getDataSources() {
        return Collections.unmodifiableList(dataSources);
    }
}
