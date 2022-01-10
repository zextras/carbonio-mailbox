// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

public interface MessageCommonInterface {
    public void setSize(Long size);
    public void setDate(Long date);
    public void setFolder(String folder);
    public void setConversationId(String conversationId);
    public void setFlags(String flags);
    @Deprecated
    public void setTags(String tags);
    public void setTagNames(String tagNames);
    public void setRevision(Integer revision);
    public void setChangeDate(Long changeDate);
    public void setModifiedSequence(Integer modifiedSequence);
    public void setMetadataInterfaces(Iterable <CustomMetadataInterface> metadatas);
    public void addMetadataInterfaces(CustomMetadataInterface metadata);

    public Long getSize();
    public Long getDate();
    public String getFolder();
    public String getConversationId();
    public String getFlags();
    @Deprecated
    public String getTags();
    public String getTagNames();
    public Integer getRevision();
    public Long getChangeDate();
    public Integer getModifiedSequence();

    public List<CustomMetadataInterface> getMetadataInterfaces();
}
