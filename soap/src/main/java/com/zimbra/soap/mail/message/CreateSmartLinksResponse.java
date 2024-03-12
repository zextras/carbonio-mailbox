// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail.message;


import com.zimbra.soap.mail.type.SmartLink;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "CreateSmartLinksResponse")
public class CreateSmartLinksResponse {

  @XmlElement(name = "smartLinks")
  private List<SmartLink> smartLinks;

  public CreateSmartLinksResponse() {
  }

  public CreateSmartLinksResponse(List<SmartLink> smartLinks) {
    this.smartLinks = smartLinks;
  }

  public List<SmartLink> getSmartLinks() {
    return smartLinks;
  }
}
