// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlElement;

public class SmartLink {

  @XmlElement(name = "publicUrl")
  private String publicUrl;

  public SmartLink(String publicUrl) {
    this.publicUrl = publicUrl;
  }

  public SmartLink() {
  }

  public String getPublicUrl() {
    return publicUrl;
  }

}
