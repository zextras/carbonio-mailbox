// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.CertMgrConstants;
import com.zimbra.soap.admin.type.CertInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = CertMgrConstants.E_GET_CERT_RESPONSE)
@XmlType(propOrder = {})
public class GetCertResponse {

  /**
   * @zm-api-field-description Certificate information
   */
  @XmlElement(name = CertMgrConstants.E_cert /* cert */, required = false)
  private List<CertInfo> certs = Lists.newArrayList();

  public GetCertResponse() {}

  public void setCerts(Iterable<CertInfo> certs) {
    this.certs.clear();
    if (certs != null) {
      Iterables.addAll(this.certs, certs);
    }
  }

  public void addCert(CertInfo cert) {
    this.certs.add(cert);
  }

  public List<CertInfo> getCerts() {
    return Collections.unmodifiableList(certs);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("certs", certs);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
