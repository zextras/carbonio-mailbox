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
    public PartInfoInterface createFromPartAndContentType(String part,
            String contentType);
    public void setSize(Integer size);
    public void setContentDisposition(String contentDisposition);
    public void setContentFilename(String contentFilename);
    public void setContentId(String contentId);
    public void setLocation(String location);
    public void setBody(Boolean body);
    public void setTruncatedContent(Boolean truncatedContent);
    public void setContent(String content);
    public String getPart();
    public String getContentType();
    public Integer getSize();
    public String getContentDisposition();
    public String getContentFilename();
    public String getContentId();
    public String getLocation();
    public Boolean getBody();
    public Boolean getTruncatedContent();
    public String getContent();
    public void setMimePartInterfaces(Iterable<PartInfoInterface> mimeParts);
    public void addMimePartInterface(PartInfoInterface mimePart);
    public List<PartInfoInterface> getMimePartInterfaces();
}
