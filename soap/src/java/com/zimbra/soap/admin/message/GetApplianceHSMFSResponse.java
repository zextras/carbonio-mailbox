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
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.HsmConstants;
import com.zimbra.soap.admin.type.HsmFileSystemInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=HsmConstants.E_GET_APPLIANCE_HSM_FS_RESPONSE)
@XmlType(propOrder = {})
public class GetApplianceHSMFSResponse {

    /**
     * @zm-api-field-description HSM filesystem information
     */
    @XmlElement(name=HsmConstants.E_FS /* fs */, required=false)
    private List<HsmFileSystemInfo> fileSystems = Lists.newArrayList();

    public GetApplianceHSMFSResponse() {
    }

    public void setFileSystems(Iterable <HsmFileSystemInfo> fileSystems) {
        this.fileSystems.clear();
        if (fileSystems != null) {
            Iterables.addAll(this.fileSystems,fileSystems);
        }
    }

    public void addFileSystem(HsmFileSystemInfo fileSystem) {
        this.fileSystems.add(fileSystem);
    }

    public List<HsmFileSystemInfo> getFileSystems() {
        return Collections.unmodifiableList(fileSystems);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("fileSystems", fileSystems);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
