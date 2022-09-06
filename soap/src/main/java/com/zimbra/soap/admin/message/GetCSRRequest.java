// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.CertMgrConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get a certificate signing request (CSR)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = CertMgrConstants.E_GET_CSR_REQUEST)
public class GetCSRRequest {

  /**
   * @zm-api-field-tag server-id
   * @zm-api-field-description Server ID. Can be "--- All Servers ---" or the ID of a server
   */
  @XmlAttribute(name = AdminConstants.A_SERVER /* server */, required = false)
  private String server;

  /**
   * @zm-api-field-tag type
   * @zm-api-field-description Type of CSR (required)
   *     <table>
   * <tr> <td> <b>self</b> </td> <td> self-signed certificate </td> </tr>
   * <tr> <td> <b>comm</b> </td> <td> commercial certificate </td> </tr>
   * </table>
   */
  @XmlAttribute(name = AdminConstants.A_TYPE /* type */, required = false)
  private String type;

  public GetCSRRequest() {}

  private GetCSRRequest(String server, String type) {
    setServer(server);
    setType(type);
  }

  public static GetCSRRequest createForServerAndType(String server, String type) {
    return new GetCSRRequest(server, type);
  }

  public void setServer(String server) {
    this.server = server;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getServer() {
    return server;
  }

  public String getType() {
    return type;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("server", server).add("type", type);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
