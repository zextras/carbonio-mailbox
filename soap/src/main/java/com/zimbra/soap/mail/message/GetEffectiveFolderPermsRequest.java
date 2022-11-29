// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.FolderSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Returns the effective permissions of the specified folder
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_EFFECTIVE_FOLDER_PERMS_REQUEST)
public class GetEffectiveFolderPermsRequest {

    /**
     * @zm-api-field-description Folder specification
     */
    @XmlElement(name=MailConstants.E_FOLDER /* folder */, required=true)
    private final FolderSpec folder;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetEffectiveFolderPermsRequest() {
        this((FolderSpec) null);
    }

    public GetEffectiveFolderPermsRequest(FolderSpec folder) {
        this.folder = folder;
    }

    public FolderSpec getFolder() { return folder; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("folder", folder);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
