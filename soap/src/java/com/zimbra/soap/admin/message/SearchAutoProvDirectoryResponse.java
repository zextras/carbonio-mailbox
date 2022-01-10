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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AutoProvDirectoryEntry;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_SEARCH_AUTO_PROV_DIRECTORY_RESPONSE)
public class SearchAutoProvDirectoryResponse {

    /**
     * @zm-api-field-tag more-flag
     * @zm-api-field-description <b>1 (true)</b> if more entries to return
     */
    @XmlAttribute(name=AdminConstants.A_MORE /* more */, required=true)
    private ZmBoolean more;

    /**
     * @zm-api-field-tag search-total
     * @zm-api-field-description Total number of accounts that matched search (not affected by limit/offset)
     */
    @XmlAttribute(name=AdminConstants.A_SEARCH_TOTAL /* searchTotal */, required=true)
    private int searchTotal;

    /**
     * @zm-api-field-description Entries
     */
    @XmlElement(name=AdminConstants.E_ENTRY /* entry */, required=false)
    private List<AutoProvDirectoryEntry> entries = Lists.newArrayList();

    public SearchAutoProvDirectoryResponse() {
    }

    public void setMore(boolean more) { this.more = ZmBoolean.fromBool(more); }
    public void setSearchTotal(int searchTotal) { this.searchTotal = searchTotal; }
    public void setEntries(Iterable <AutoProvDirectoryEntry> entries) {
        this.entries.clear();
        if (entries != null) {
            Iterables.addAll(this.entries,entries);
        }
    }

    public void addEntry(AutoProvDirectoryEntry entry) {
        this.entries.add(entry);
    }

    public boolean getMore() { return ZmBoolean.toBool(more); }
    public int getSearchTotal() { return searchTotal; }
    public List<AutoProvDirectoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("more", more)
            .add("searchTotal", searchTotal)
            .add("entries", entries);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
