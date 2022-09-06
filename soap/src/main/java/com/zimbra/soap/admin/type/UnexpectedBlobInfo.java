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
public class UnexpectedBlobInfo {

  /**
   * @zm-api-field-tag volume-id
   * @zm-api-field-description volume ID
   */
  @XmlAttribute(name = AdminConstants.A_VOLUME_ID /* volumeId */, required = true)
  private final short volumeId;

  /**
   * @zm-api-field-tag path
   * @zm-api-field-description Path
   */
  @XmlAttribute(name = AdminConstants.A_PATH /* path */, required = true)
  private final String path;

  /**
   * @zm-api-field-tag size
   * @zm-api-field-description Size
   */
  @XmlAttribute(name = AdminConstants.A_FILE_SIZE /* fileSize */, required = true)
  private final Long fileSize;

  /**
   * @zm-api-field-tag external-flag
   * @zm-api-field-description Set if the blob is stored in an ExternalStoreManager rather than
   *     locally in FileBlobStore
   */
  @XmlAttribute(name = AdminConstants.A_EXTERNAL /* external */, required = true)
  private final ZmBoolean external;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private UnexpectedBlobInfo() {
    this((short) -1, (String) null, (Long) null, false);
  }

  public UnexpectedBlobInfo(short volumeId, String path, Long fileSize, boolean external) {
    this.volumeId = volumeId;
    this.path = path;
    this.fileSize = fileSize;
    this.external = ZmBoolean.fromBool(external);
  }

  public short getVolumeId() {
    return volumeId;
  }

  public String getPath() {
    return path;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public boolean getExternal() {
    return ZmBoolean.toBool(external);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("volumeId", volumeId)
        .add("path", path)
        .add("fileSize", fileSize)
        .add("external", external);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
