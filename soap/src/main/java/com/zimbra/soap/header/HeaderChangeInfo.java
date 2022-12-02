// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.header;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.HeaderConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class HeaderChangeInfo {

    /**
     * @zm-api-field-tag change-id
     * @zm-api-field-description The highest change ID the client knows about.  Default value "-1"
     */
    @XmlAttribute(name=HeaderConstants.A_CHANGE_ID /* token */, required=false)
    private String changeId;

    /**
     * @zm-api-field-tag change-type
     * @zm-api-field-description Change type.  Valid values "mod" (default) and "new"
     * <p> The default behavior (type="mod") will cause mail.MODIFY_CONFLICT to be thrown if we try to modify an
     * object that has been touched (flags, tags, folders, etc.) since the specified change ID.
     * </p><p> Alternatively, type="new" will throw mail.MODIFY_CONFLICT if we try to modify an object that has been
     * created or whose content has been modified since the specified change ID.
     * </p><p> In general, sync clients will use type="mod" and the web client will use type="new".
     */
    @XmlAttribute(name=HeaderConstants.A_TYPE /* type */, required=false)
    private String changeType;

    public HeaderChangeInfo() {
    }

    public void setChangeId(String changeId) { this.changeId = changeId; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getChangeId() { return changeId; }
    public String getChangeType() { return changeType; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("changeId", changeId)
            .add("changeType", changeType);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
