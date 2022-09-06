// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainAdminRight;
import com.zimbra.soap.admin.type.PackageRightsInfo;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_RIGHTS_DOC_RESPONSE)
public class GetRightsDocResponse {

  /**
   * @zm-api-field-description Information for packages
   */
  @XmlElement(name = AdminConstants.E_PACKAGE /* package */, required = false)
  private List<PackageRightsInfo> pkgs = Lists.newArrayList();

  /**
   * @zm-api-field-description Unused Admin rights
   */
  @XmlElement(name = "notUsed", required = false)
  private List<String> notUsed = Lists.newArrayList();

  /**
   * @zm-api-field-description Domain admin rights
   */
  @ZimbraJsonArrayForWrapper
  @XmlElementWrapper(
      name = "domainAdmin-copypaste-to-rights-domainadmin-xml-template",
      required = true)
  @XmlElement(name = AdminConstants.E_RIGHT /* right */, required = false)
  private List<DomainAdminRight> rights = Lists.newArrayList();

  public GetRightsDocResponse() {}

  public GetRightsDocResponse setPackages(Collection<PackageRightsInfo> pkgs) {
    this.pkgs.clear();
    if (pkgs != null) {
      this.pkgs.addAll(pkgs);
    }
    return this;
  }

  public GetRightsDocResponse addPackage(PackageRightsInfo pkg) {
    pkgs.add(pkg);
    return this;
  }

  public List<PackageRightsInfo> getPackages() {
    return Collections.unmodifiableList(pkgs);
  }

  public GetRightsDocResponse setRights(Collection<DomainAdminRight> rights) {
    this.rights.clear();
    if (rights != null) {
      this.rights.addAll(rights);
    }
    return this;
  }

  public GetRightsDocResponse addRight(DomainAdminRight right) {
    rights.add(right);
    return this;
  }

  public List<DomainAdminRight> getRights() {
    return Collections.unmodifiableList(rights);
  }

  public List<String> getNotUsed() {
    return notUsed;
  }

  public void setNotUsed(List<String> notUsed) {
    this.notUsed.clear();
    if (notUsed != null) {
      this.notUsed.addAll(notUsed);
    }
  }
}
