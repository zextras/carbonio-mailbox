// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.NamedElement;

// XmlRootElement is needed for classes referenced via @XmlElementRef
@XmlRootElement(name=AdminConstants.E_VALUES)
@XmlAccessorType(XmlAccessType.NONE)
public class StatsValueWrapper {

    /**
     * @zm-api-field-description Stats specification
     */
    @XmlElement(name=AdminConstants.E_STAT /* stat */, required=false)
    private List<NamedElement> stats = Lists.newArrayList();

    public StatsValueWrapper() {
    }

    public void setStats(Iterable <NamedElement> stats) {
        this.stats.clear();
        if (stats != null) {
            Iterables.addAll(this.stats, stats);
        }
    }

    public StatsValueWrapper addStat(NamedElement stat) {
        this.stats.add(stat);
        return this;
    }

    public List<NamedElement> getStats() {
        return Collections.unmodifiableList(stats);
    }
}
