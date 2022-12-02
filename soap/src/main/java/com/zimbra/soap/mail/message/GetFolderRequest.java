// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.GetFolderSpec;
import com.zimbra.soap.type.ZmBoolean;

/*
<GetFolderRequest [visible="0|1"] [needGranteeName="0|1"] [view="{folder-view-constraint}"] [depth="{subfolder-levels}"]>
  [<folder [l="{base-folder-id}"] [path="{fully-qualified-path}"]/>]
</GetFolderRequest>

 */

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Folder
 * <br />
 * A {base-folder-id}, a {base-folder-uuid} or a {fully-qualified-path} can optionally be specified in the folder
 * element; if none is present, the descent of the folder hierarchy begins at the mailbox's root folder (id 1).
 * <br />
 * If {fully-qualified-path} is present and {base-folder-id} or {base-folder-uuid} is also present, the path is
 * treated as relative to the folder that was specified by id/uuid.  {base-folder-id} is ignored if {base-folder-uuid}
 * is present.
 */
@XmlRootElement(name=MailConstants.E_GET_FOLDER_REQUEST)
public class GetFolderRequest {

    /**
     * @zm-api-field-tag is-visible
     * @zm-api-field-description If set we include all visible subfolders of the specified folder.
     * When you have full rights on the mailbox, this is indistinguishable from the normal
     * <b>&lt;GetFolderResponse></b>
     * <br />
     * When you don't:
     * <ul>
     * <li> folders you can see appear normally,
     * <li> folders you can't see (and can't see any subfolders) are omitted
     * <li> folders you can't see (but *can* see >=1 subfolder) appear as
     *      &lt;folder id="{id}" name="{name}"> hierarchy placeholders
     * </ul>
     */
    @XmlAttribute(name=MailConstants.A_VISIBLE /* visible */, required=false)
    private ZmBoolean isVisible;

    /**
     * @zm-api-field-tag need-grantee-name
     * @zm-api-field-description If set then grantee names are supplied in the <b>d</b> attribute in <b>&lt;grant></b>.
     * Default: <b>unset</b>
     */
    @XmlAttribute(name=MailConstants.A_NEED_GRANTEE_NAME /* needGranteeName */, required=false)
    private ZmBoolean needGranteeName;

    /**
     * @zm-api-field-tag view
     * @zm-api-field-description If "view" is set then only the folders with matching view will be returned.
     * Otherwise folders with any default views will be returned.
     */
    @XmlAttribute(name=MailConstants.A_DEFAULT_VIEW /* view */, required=false)
    private String viewConstraint;

    /**
     * @zm-api-field-tag depth
     * @zm-api-field-description If "depth" is set to a non-negative number, we include that many levels of
     * subfolders in the response.  (so if depth="1", we'll include only the folder and its direct subfolders)
     * If depth is missing or negative, the entire folder hierarchy is returned
     */
    @XmlAttribute(name=MailConstants.A_FOLDER_DEPTH /* depth */, required=false)
    private Integer treeDepth;

    /**
     * @zm-api-field-tag traverse-mountpoints
     * @zm-api-field-description If  true, one level of mountpoints are traversed and the target folder's counts are
     * applied to the local mountpoint.  if the root folder as referenced by <b>{base-folder-id}</b> and/or
     * <b>{fully-qualified-path}</b> is a mountpoint, "tr" is regarded as being automatically set.
     * Mountpoints under mountpoints are not themselves expanded.
     */
    @XmlAttribute(name=MailConstants.A_TRAVERSE /* tr */, required=false)
    private ZmBoolean traverseMountpoints;
    /**
     * @zm-api-field-description Folder specification
     */
    @XmlElement(name=MailConstants.E_FOLDER /* folder */, required=false)
    private GetFolderSpec folder;

    public GetFolderRequest() {
    }

    public GetFolderRequest(GetFolderSpec folder) {
        this(folder, null);
    }

    public GetFolderRequest(GetFolderSpec folder, Boolean isVisible) {
        setFolder(folder);
        setVisible(isVisible);
    }

    public Boolean isVisible() {
        return ZmBoolean.toBool(isVisible);
    }

    public boolean isNeedGranteeName() {
        return ZmBoolean.toBool(needGranteeName);
    }

    public String getViewConstraint() {
        return viewConstraint;
    }

    public Integer getTreeDepth() {
        return treeDepth;
    }

    public GetFolderSpec getFolder() {
        return folder;
    }

    public void setVisible(Boolean isVisible) {
        this.isVisible = ZmBoolean.fromBool(isVisible);
    }

    public void setNeedGranteeName(boolean needGranteeName) {
        this.needGranteeName = ZmBoolean.fromBool(needGranteeName);
    }

    public void setViewConstraint(String viewConstraint) {
        this.viewConstraint = viewConstraint;
    }

    public void setTreeDepth(Integer treeDepth) {
        this.treeDepth = treeDepth;
    }

    public void setFolder(GetFolderSpec folder) {
        this.folder = folder;
    }

    public Boolean isTraverseMountpoints() { return ZmBoolean.toBool(traverseMountpoints); }
    public void setTraverseMountpoints(Boolean traverseMountpoints) {
        this.traverseMountpoints = ZmBoolean.fromBool(traverseMountpoints);
    }
}
