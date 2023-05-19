// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface PartInfoInterface {
    PartInfoInterface createFromPartAndContentType(String part,
        String contentType);
    void setSize(Integer size);
    void setContentDisposition(String contentDisposition);
    void setContentFilename(String contentFilename);
    void setContentId(String contentId);
    void setLocation(String location);
    void setBody(Boolean body);
    void setTruncatedContent(Boolean truncatedContent);
    void setContent(String content);
    String getPart();
    String getContentType();
    Integer getSize();
    String getContentDisposition();
    String getContentFilename();
    String getContentId();
    String getLocation();
    Boolean getBody();
    Boolean getTruncatedContent();
    String getContent();
    void setMimePartInterfaces(Iterable<PartInfoInterface> mimeParts);
    void addMimePartInterface(PartInfoInterface mimePart);
    List<PartInfoInterface> getMimePartInterfaces();
}
