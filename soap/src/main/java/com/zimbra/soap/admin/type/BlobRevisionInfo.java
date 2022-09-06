// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class BlobRevisionInfo {

  /**
   * @zm-api-field-tag path
   * @zm-api-field-description Path
   */
  @XmlAttribute(name = AdminConstants.A_PATH /* path */, required = true)
  private final String path;

  /**
   * @zm-api-field-tag file-size
   * @zm-api-field-description File size
   */
  @XmlAttribute(name = AdminConstants.A_FILE_SIZE /* fileSize */, required = true)
  private final Long fileSize;

  /**
   * @zm-api-field-tag revision
   * @zm-api-field-description Revision number
   */
  @XmlAttribute(name = AdminConstants.A_REVISION /* rev */, required = true)
  private final int revision;

  /**
   * @zm-api-field-tag external-flag
   * @zm-api-field-description Set if the blob is stored in an ExternalStoreManager rather than
   *     locally in FileBlobStore
   */
  @XmlAttribute(name = AdminConstants.A_EXTERNAL /* external */, required = true)
  private final ZmBoolean external;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private BlobRevisionInfo() {
    this((String) null, (Long) null, -1, false);
  }

  public BlobRevisionInfo(String path, Long fileSize, int revision, boolean external) {
    this.path = path;
    this.fileSize = fileSize;
    this.revision = revision;
    this.external = ZmBoolean.fromBool(external);
  }

  public String getPath() {
    return path;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public int getRevision() {
    return revision;
  }

  public boolean getExternal() {
    return ZmBoolean.toBool(external);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("path", path)
        .add("fileSize", fileSize)
        .add("revision", revision)
        .add("external", external);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
