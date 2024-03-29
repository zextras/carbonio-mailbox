// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class NewFolderSpec {

    /**
     * @zm-api-field-tag folder-name
     * @zm-api-field-description If "l" is unset, name is the full path of the new folder; otherwise, name may not
     * contain the folder separator '/'
     */
    @XmlAttribute(name=MailConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-tag default-type
     * @zm-api-field-description (optional) Default type for the folder; used by web client to decide which view to use;
     * <br />
     * possible values are the same as <b>&lt;SearchRequest></b>'s {types}: <b>conversation|message|contact|etc</b>
     */
    @XmlAttribute(name=MailConstants.A_DEFAULT_VIEW /* view */, required=false)
    private String defaultView;

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
     * @zm-api-field-tag remote-url
     * @zm-api-field-description URL (RSS, iCal, etc.) this folder syncs its contents to
     */
    @XmlAttribute(name=MailConstants.A_URL /* url */, required=false)
    private String url;

    /**
     * @zm-api-field-tag parent-folder-id
     * @zm-api-field-description Parent folder ID
     */
    @XmlAttribute(name=MailConstants.A_FOLDER /* l */, required=true)
    private String parentFolderId;

    /**
     * @zm-api-field-tag fetch-if-exists
     * @zm-api-field-description If set, the server will fetch the folder if it already exists rather than throwing
     * mail.ALREADY_EXISTS
     */
    @XmlAttribute(name=MailConstants.A_FETCH_IF_EXISTS /* fie */, required=false)
    private ZmBoolean fetchIfExists;

    /**
     * @zm-api-field-tag sync-to-url
     * @zm-api-field-description If set (default) then if "url" is set, synchronize folder content on folder creation
     */
    @XmlAttribute(name=MailConstants.A_SYNC /* sync */, required=false)
    private ZmBoolean syncToUrl;

    /**
     * @zm-api-field-description Grant specification
     */
    @XmlElementWrapper(name=MailConstants.E_ACL /* acl */, required=false)
    @XmlElement(name=MailConstants.E_GRANT /* grant */, required=false)
    private final List<ActionGrantSelector> grants = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private NewFolderSpec() {
        this(null);
    }

    public NewFolderSpec(String name) {
        this.name = name;
    }

    public static NewFolderSpec createForNameAndParentFolderId(String name, String parentFolderId) {
        final NewFolderSpec nfs = new NewFolderSpec(name);
        nfs.setParentFolderId(parentFolderId);
        return nfs;
    }

    public void setDefaultView(String defaultView) { this.defaultView = defaultView; }
    public void setFlags(String flags) { this.flags = flags; }
    public void setColor(Byte color) { this.color = color; }
    public void setRgb(String rgb) { this.rgb = rgb; }
    public void setUrl(String url) { this.url = url; }
    public void setParentFolderId( String parentFolderId) { this.parentFolderId = parentFolderId; }
    public void setFetchIfExists(Boolean fetchIfExists) { this.fetchIfExists = ZmBoolean.fromBool(fetchIfExists); }
    public void setSyncToUrl(Boolean syncToUrl) { this.syncToUrl = ZmBoolean.fromBool(syncToUrl); }
    public void setGrants(Iterable <ActionGrantSelector> grants) {
        this.grants.clear();
        if (grants != null) {
            Iterables.addAll(this.grants,grants);
        }
    }

    public void addGrant(ActionGrantSelector grant) {
        this.grants.add(grant);
    }

    public String getName() { return name; }
    public String getDefaultView() { return defaultView; }
    public String getFlags() { return flags; }
    public Byte getColor() { return color; }
    public String getRgb() { return rgb; }
    public String getUrl() { return url; }
    public String getParentFolderId() { return parentFolderId; }
    public Boolean getFetchIfExists() { return ZmBoolean.toBool(fetchIfExists); }
    public Boolean getSyncToUrl() { return ZmBoolean.toBool(syncToUrl); }
    public List<ActionGrantSelector> getGrants() {
        return grants;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("defaultView", defaultView)
            .add("flags", flags)
            .add("color", color)
            .add("rgb", rgb)
            .add("url", url)
            .add("parentFolderId", parentFolderId)
            .add("fetchIfExists", fetchIfExists)
            .add("syncToUrl", syncToUrl)
            .add("grants", grants);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
