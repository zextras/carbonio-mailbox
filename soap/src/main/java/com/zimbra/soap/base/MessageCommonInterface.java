// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

public interface MessageCommonInterface {
    void setSize(Long size);
    void setDate(Long date);
    void setFolder(String folder);
    void setConversationId(String conversationId);
    void setFlags(String flags);
    @Deprecated
    void setTags(String tags);
    void setTagNames(String tagNames);
    void setRevision(Integer revision);
    void setChangeDate(Long changeDate);
    void setModifiedSequence(Integer modifiedSequence);
    void setMetadataInterfaces(Iterable<CustomMetadataInterface> metadatas);
    void addMetadataInterfaces(CustomMetadataInterface metadata);

    Long getSize();
    Long getDate();
    String getFolder();
    String getConversationId();
    String getFlags();
    @Deprecated
    String getTags();
    String getTagNames();
    Integer getRevision();
    Long getChangeDate();
    Integer getModifiedSequence();

    List<CustomMetadataInterface> getMetadataInterfaces();
}
