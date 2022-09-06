// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DirPathInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHECK_DIRECTORY_RESPONSE)
public class CheckDirectoryResponse {

  /**
   * @zm-api-field-description Information for directories
   */
  @XmlElement(name = AdminConstants.E_DIRECTORY, required = false)
  private List<DirPathInfo> paths = Lists.newArrayList();

  public CheckDirectoryResponse() {}

  public CheckDirectoryResponse(Collection<DirPathInfo> paths) {
    setPaths(paths);
  }

  public CheckDirectoryResponse setPaths(Collection<DirPathInfo> paths) {
    this.paths.clear();
    if (paths != null) {
      this.paths.addAll(paths);
    }
    return this;
  }

  public CheckDirectoryResponse addPath(DirPathInfo path) {
    paths.add(path);
    return this;
  }

  public List<DirPathInfo> getPaths() {
    return Collections.unmodifiableList(paths);
  }
}
