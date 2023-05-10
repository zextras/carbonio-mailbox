// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class NewSearchFolderSpec {

    /**
     * @zm-api-field-tag name
     * @zm-api-field-description Name
     */
    @XmlAttribute(name=MailConstants.A_NAME /* name */, required=true)
    private String name;

    /**
     * @zm-api-field-tag query
     * @zm-api-field-description query
     */
    @XmlAttribute(name=MailConstants.A_QUERY /* query */, required=true)
    private String query;

    /**
     * @zm-api-field-tag search-types
     * @zm-api-field-description Search types
     */
    @XmlAttribute(name=MailConstants.A_SEARCH_TYPES /* types */, required=false)
    private String searchTypes;

    /**
     * @zm-api-field-tag sort-by
     * @zm-api-field-description Sort by
     */
    @XmlAttribute(name=MailConstants.A_SORTBY /* sortBy */, required=false)
    private String sortBy;

    /**
     * @zm-api-field-tag flags
     * @zm-api-field-description Flags
     */
    @XmlAttribute(name=MailConstants.A_FLAGS /* f */, required=false)
    private String flags;

    /**
     * @zm-api-field-tag color
     * @zm-api-field-description color numeric; range 0-127; defaults to 0 if not present; client can display only 0-7
     */
    @XmlAttribute(name=MailConstants.A_COLOR /* color */, required=false)
    private Byte color;

    /**
     * @zm-api-field-tag rgb-color
     * @zm-api-field-description RGB color in format #rrggbb where r,g and b are hex digits
     */
    @XmlAttribute(name=MailConstants.A_RGB /* rgb */, required=false)
    private String rgb;

    /**
     * @zm-api-field-tag parent-folder-id
     * @zm-api-field-description Parent folder ID
     */
    @XmlAttribute(name=MailConstants.A_FOLDER /* l */, required=false)
    private String parentFolderId;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private NewSearchFolderSpec() {
        this((String) null, (String) null, (String) null);
    }

    private NewSearchFolderSpec(String name, String query, String parentFolderId) {
        this.name = name;
        this.query = query;
        this.parentFolderId = parentFolderId;
    }

    public static NewSearchFolderSpec forNameQueryAndFolder(String folderName, String searchQuery, String parentId) {
        NewSearchFolderSpec spec = new NewSearchFolderSpec(folderName, searchQuery, parentId);
        return spec;
    }

    public void setName( String name) { this.name = name; }
    public void setQuery( String query) { this.query = query; }
    public void setSearchTypes(String searchTypes) { this.searchTypes = searchTypes; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public void setFlags(String flags) { this.flags = flags; }
    public void setColor(Byte color) { this.color = color; }
    public void setParentFolderId( String parentFolderId) { this.parentFolderId = parentFolderId; }
    public void setRgb(String rgb) { this.rgb = rgb; }
    public String getName() { return name; }
    public String getQuery() { return query; }
    public String getSearchTypes() { return searchTypes; }
    public String getSortBy() { return sortBy; }
    public String getFlags() { return flags; }
    public Byte getColor() { return color; }
    public String getParentFolderId() { return parentFolderId; }
    public String getRgb() { return rgb; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("query", query)
            .add("searchTypes", searchTypes)
            .add("sortBy", sortBy)
            .add("flags", flags)
            .add("color", color)
            .add("rgb", rgb)
            .add("parentFolderId", parentFolderId);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
