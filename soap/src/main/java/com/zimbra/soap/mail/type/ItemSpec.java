// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ItemSpec {

  /**
   * @zm-api-field-tag item-id
   * @zm-api-field-description Item ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = false)
  private String id;

  /**
   * @zm-api-field-tag folder-id
   * @zm-api-field-description Folder ID
   */
  @XmlAttribute(name = MailConstants.A_FOLDER /* l */, required = false)
  private String folder;

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Name
   */
  @XmlAttribute(name = MailConstants.A_NAME /* name */, required = false)
  private String name;

  /**
   * @zm-api-field-tag fully-qualified-path
   * @zm-api-field-description Fully qualified path
   */
  @XmlAttribute(name = MailConstants.A_PATH /* path */, required = false)
  private String path;

  public ItemSpec() {}

  public void setId(String id) {
    this.id = id;
  }

  public void setFolder(String folder) {
    this.folder = folder;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getId() {
    return id;
  }

  public String getFolder() {
    return folder;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("folder", folder).add("name", name).add("path", path);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
