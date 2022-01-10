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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminZimletInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ADMIN_EXTENSION_ZIMLETS_RESPONSE)
@XmlType(propOrder = {})
public class GetAdminExtensionZimletsResponse {

    /**
     * @zm-api-field-description Information about Admin Extension Zimlets
     */
    @XmlElementWrapper(name=AccountConstants.E_ZIMLETS, required=true)
    @XmlElement(name=AccountConstants.E_ZIMLET /* zimlet */, required=false)
    private List<AdminZimletInfo> zimlets = Lists.newArrayList();

    public GetAdminExtensionZimletsResponse() {
    }

    public void setZimlets(Iterable <AdminZimletInfo> zimlets) {
        this.zimlets.clear();
        if (zimlets != null) {
            Iterables.addAll(this.zimlets,zimlets);
        }
    }

    public void addZimlet(AdminZimletInfo zimlet) {
        this.zimlets.add(zimlet);
    }

    public List<AdminZimletInfo> getZimlets() {
        return Collections.unmodifiableList(zimlets);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("zimlets", zimlets);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
