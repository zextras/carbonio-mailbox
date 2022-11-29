// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class RootVoiceFolder extends VoiceFolder {

    /**
     * @zm-api-field-description Folders
     */
    @XmlElement(name=MailConstants.E_FOLDER /* folder */, required=false)
    private List<VoiceFolder> folders = Lists.newArrayList();

    public RootVoiceFolder() {
    }

    public void setFolders(Iterable <VoiceFolder> folders) {
        this.folders.clear();
        if (folders != null) {
            Iterables.addAll(this.folders, folders);
        }
    }

    public void addFolder(VoiceFolder folder) {
        this.folders.add(folder);
    }

    public List<VoiceFolder> getFolders() {
        return folders;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("folders", folders);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
