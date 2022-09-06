// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Verify Store Manager
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_VERIFY_STORE_MANAGER_REQUEST)
public class VerifyStoreManagerRequest {

  @XmlAttribute(name = AdminConstants.A_FILE_SIZE, required = false)
  private Integer fileSize;

  @XmlAttribute(name = AdminConstants.A_NUM, required = false)
  private Integer num;

  @XmlAttribute(name = AdminConstants.A_CHECK_BLOBS, required = false)
  private Boolean checkBlobs;

  public Integer getFileSize() {
    return fileSize;
  }

  public void setFileSize(Integer fileSize) {
    this.fileSize = fileSize;
  }

  public Integer getNum() {
    return num;
  }

  public void setNum(Integer num) {
    this.num = num;
  }

  public Boolean getCheckBlobs() {
    return checkBlobs;
  }

  public void setCheckBlobs(Boolean checkBlobs) {
    this.checkBlobs = checkBlobs;
  }

  /** no-argument constructor wanted by JAXB */
  private VerifyStoreManagerRequest() {}
}
