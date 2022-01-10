// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ZimletStatusParent {

    /**
     * @zm-api-field-description Status information
     */
    @XmlElement(name=AdminConstants.E_ZIMLET /* zimlet */, required=false)
    private List<ZimletStatus> zimlets = Lists.newArrayList();

    public ZimletStatusParent() {
    }

    public void setZimlets(Iterable <ZimletStatus> zimlets) {
        this.zimlets.clear();
        if (zimlets != null) {
            Iterables.addAll(this.zimlets,zimlets);
        }
    }

    public ZimletStatusParent addZimlet(ZimletStatus zimlet) {
        this.zimlets.add(zimlet);
        return this;
    }

    public List<ZimletStatus> getZimlets() {
        return Collections.unmodifiableList(zimlets);
    }
}
