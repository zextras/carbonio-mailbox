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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Create a Class of Service (COS) <br>
 *     Notes: <br>
 *     Extra attrs: <b>description</b>, <b>zimbraNotes</b>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CREATE_COS_REQUEST)
@XmlType(propOrder = {})
public class CreateCosRequest extends AdminAttrsImpl {

  /**
   * @zm-api-field-tag cos-name
   * @zm-api-field-description Name
   */
  @XmlElement(name = AdminConstants.E_NAME, required = true)
  private String name;

  public CreateCosRequest() {
    this(null, (Collection<Attr>) null);
  }

  public CreateCosRequest(String name) {
    this(name, (Collection<Attr>) null);
  }

  public CreateCosRequest(String name, Collection<Attr> attrs) {
    setName(name);
    super.setAttrs(attrs);
  }

  public CreateCosRequest(String name, Map<String, ? extends Object> attrs)
      throws ServiceException {
    setName(name);
    super.setAttrs(attrs);
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
