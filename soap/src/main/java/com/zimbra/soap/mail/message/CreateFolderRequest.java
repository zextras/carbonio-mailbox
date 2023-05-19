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
import com.zimbra.soap.mail.type.NewFolderSpec;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Create folder
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_CREATE_FOLDER_REQUEST)
public class CreateFolderRequest {

    /**
     * @zm-api-field-description New folder specification
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_FOLDER /* folder */, required=true)
    private final NewFolderSpec folder;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CreateFolderRequest() {
        this(null);
    }

    public CreateFolderRequest(NewFolderSpec folder) {
        this.folder = folder;
    }

    public NewFolderSpec getFolder() { return folder; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("folder", folder)
            .toString();
    }
}
