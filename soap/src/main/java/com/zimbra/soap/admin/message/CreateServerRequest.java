// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import com.zimbra.soap.admin.type.Attr;
import java.util.Collection;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Create a Server <br>
 *     Extra attrs: <b>description</b>, <b>zimbraNotes</b>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CREATE_SERVER_REQUEST)
@XmlType(propOrder = {})
public class CreateServerRequest extends AdminAttrsImpl {

  /**
   * @zm-api-field-tag new-server-name
   * @zm-api-field-description New server name
   */
  @XmlAttribute(name = AdminConstants.E_NAME, required = true)
  private String name;

  public CreateServerRequest() {
    this(null, (Collection<Attr>) null);
  }

  public CreateServerRequest(String name) {
    this(name, (Collection<Attr>) null);
  }

  public CreateServerRequest(String name, Collection<Attr> attrs) {
    super(attrs);
    this.name = name;
  }

  public CreateServerRequest(String name, Map<String, ? extends Object> attrs)
      throws ServiceException {
    super(attrs);
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
