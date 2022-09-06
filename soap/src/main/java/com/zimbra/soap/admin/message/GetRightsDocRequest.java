// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.PackageSelector;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required false
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Rights Document
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_RIGHTS_DOC_REQUEST)
public class GetRightsDocRequest {

  /**
   * @zm-api-field-description Packages
   */
  @XmlElement(name = AdminConstants.E_PACKAGE, required = false)
  private List<PackageSelector> pkgs = Lists.newArrayList();

  public GetRightsDocRequest() {}

  public GetRightsDocRequest(Collection<PackageSelector> pkgs) {
    setPkgs(pkgs);
  }

  public GetRightsDocRequest setPkgs(Collection<PackageSelector> pkgs) {
    this.pkgs.clear();
    if (pkgs != null) {
      this.pkgs.addAll(pkgs);
    }
    return this;
  }

  public GetRightsDocRequest addPkg(PackageSelector pkg) {
    pkgs.add(pkg);
    return this;
  }

  public List<PackageSelector> getPkgs() {
    return Collections.unmodifiableList(pkgs);
  }
}
