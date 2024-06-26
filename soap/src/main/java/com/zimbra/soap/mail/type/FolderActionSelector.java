// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Collections;
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
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import com.zimbra.soap.type.ZmBoolean;




@XmlAccessorType(XmlAccessType.NONE)
public class FolderActionSelector extends ActionSelector {

    /**
     * @zm-api-field-tag delete-subfolders
     * @zm-api-field-description  For op="empty" - hard-delete all items in the folder (and all the folder's
     * subfolders if "recursive" is set)
     */
    @XmlAttribute(name=MailConstants.A_RECURSIVE /* recursive */, required=false)
    private ZmBoolean recursive;

    /**
     * @zm-api-field-tag target-url
     * @zm-api-field-description Target URL
     */
    @XmlAttribute(name=MailConstants.A_URL /* url */, required=false)
    private String url;

    /**
     * @zm-api-field-tag exclude-free-busy-boolean
     * @zm-api-field-description For <b>op="fb"</b> - set the excludeFreeBusy boolean for this folder (must specify
     * <b>{exclude-free-busy-boolean}</b> for op="fb")
     */
    @XmlAttribute(name=MailConstants.A_EXCLUDE_FREEBUSY /* excludeFreeBusy */, required=false)
    private ZmBoolean excludeFreebusy;

    /**
     * @zm-api-field-tag grantee-id
     * @zm-api-field-description Grantee Zimbra ID
     */
    @XmlAttribute(name=MailConstants.A_ZIMBRA_ID /* zid */, required=false)
    private String zimbraId;

    /**
     * @zm-api-field-tag grantee-type
     * @zm-api-field-description Grantee Type
     */
    @XmlAttribute(name=MailConstants.A_GRANT_TYPE /* gt */, required=false)
    private String grantType;

    /**
     * @zm-api-field-tag default-view
     * @zm-api-field-description Use with <b>op="update"</b> to change folder's default view (useful for migration)
     */
    @XmlAttribute(name=MailConstants.A_DEFAULT_VIEW /* view */, required=false)
    private String view;

    /**
     * @zm-api-field-tag
     * @zm-api-field-description
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_GRANT /* grant */, required=false)
    private ActionGrantSelector grant;

    /**
     * @zm-api-field-description Grants (used with <b>op="grant"</b> and <b>op="!grant"</b>
     */
    @XmlElementWrapper(name=MailConstants.E_ACL /* acl */, required=false)
    @XmlElement(name=MailConstants.E_GRANT /* grant */, required=false)
    private List<ActionGrantSelector> grants = Lists.newArrayList();

    /**
     * @zm-api-field-description Retention policy
     */
    @XmlElement(name=MailConstants.E_RETENTION_POLICY /* retentionPolicy */, required=false)
    private RetentionPolicy retentionPolicy;

    /**
     * @zm-api-field-tag web-offline-sync-days
     * @zm-api-field-description Number of days for which web client would sync folder data for offline use
     */
    @XmlAttribute(name=MailConstants.A_NUM_DAYS /* numDays */, required=false)
    private Integer numDays;

    /**
     * @zm-api-field-tag empty-folder-op-type
     * @zm-api-field-description Match type for folder action "op":"empty" <br>
     *     Legal values are: <b>emails|contacts|appointments</b> <br>
     */
    @XmlAttribute(name = MailConstants.A_FOLDER_ACTION_EMPTY_OP_MATCH_TYPE /* type */, required = false)
    private String type;

    public FolderActionSelector() {
        this(null, null);
    }

    public FolderActionSelector(String ids, String operation) {
        super(ids, operation);
    }

    public void setRecursive(Boolean recursive) { this.recursive = ZmBoolean.fromBool(recursive); }
    public void setUrl(String url) { this.url = url; }
    public void setExcludeFreebusy(Boolean excludeFreebusy) {
        this.excludeFreebusy = ZmBoolean.fromBool(excludeFreebusy);
    }
    public void setZimbraId(String zimbraId) { this.zimbraId = zimbraId; }
    public void setGrantType(String grantType) { this.grantType = grantType; }
    public void setGrant(ActionGrantSelector grant) { this.grant = grant; }
    public void setGrants(Iterable <ActionGrantSelector> grants) {
        this.grants = null;
        if (grants != null) {
            this.grants = Lists.newArrayList();
            Iterables.addAll(this.grants,grants);
        }
    }

    public void addGrant(ActionGrantSelector grant) {
        if (grants == null)
            this.grants = Lists.newArrayList();
        this.grants.add(grant);
    }

    public void setRetentionPolicy(RetentionPolicy retentionPolicy) { this.retentionPolicy = retentionPolicy; }
    public void setNumDays(Integer numDays) { this.numDays = numDays; }
    public void setType(final String type) {
        this.type = type;
    }
    public Boolean getRecursive() { return ZmBoolean.toBool(recursive); }
    public String getUrl() { return url; }
    public Boolean getExcludeFreebusy() { return ZmBoolean.toBool(excludeFreebusy); }
    public String getZimbraId() { return zimbraId; }
    public String getGrantType() { return grantType; }
    public String getView() { return view; }
    public ActionGrantSelector getGrant() { return grant; }
    public List<ActionGrantSelector> getGrants() {
        return Collections.unmodifiableList(grants);
    }
    public RetentionPolicy getRetentionPolicy() { return retentionPolicy; }
    public Integer getNumDays() { return numDays; }
    public String getType() {
        return type;
    }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("recursive", recursive)
            .add("url", url)
            .add("excludeFreebusy", excludeFreebusy)
            .add("zimbraId", zimbraId)
            .add("grantType", grantType)
            .add("view", view)
            .add("grant", grant)
            .add("grants", grants)
            .add("retentionPolicy", retentionPolicy)
            .add("numDays", numDays);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
