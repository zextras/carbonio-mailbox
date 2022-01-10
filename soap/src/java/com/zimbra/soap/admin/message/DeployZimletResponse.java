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

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ZimletDeploymentStatus;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_DEPLOY_ZIMLET_RESPONSE)
@XmlType(propOrder = {})
public class DeployZimletResponse {

    /**
     * @zm-api-field-description Progress information on deployment to servers
     */
    @XmlElement(name=AdminConstants.E_PROGRESS /* progress */, required=false)
    private List<ZimletDeploymentStatus> progresses = Lists.newArrayList();

    public DeployZimletResponse() {
    }

    public void setProgresses(Iterable <ZimletDeploymentStatus> progresses) {
        this.progresses.clear();
        if (progresses != null) {
            Iterables.addAll(this.progresses,progresses);
        }
    }

    public void addProgress(ZimletDeploymentStatus progress) {
        this.progresses.add(progress);
    }

    public List<ZimletDeploymentStatus> getProgresses() {
        return Collections.unmodifiableList(progresses);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("progresses", progresses);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
