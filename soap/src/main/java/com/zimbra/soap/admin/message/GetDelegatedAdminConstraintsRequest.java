// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.NamedElement;
import com.zimbra.soap.type.TargetType;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get constraints (zimbraConstraint) for delegated admin on global
 *     config or a COS <br>
 *     none or several attributes can be specified for which constraints are to be returned. <br>
 *     If no attribute is specified, all constraints on the global config/cos will be returned. <br>
 *     If there is no constraint for a requested attribute, <b>&lt;a></b> element for the attribute
 *     will not appear in the response. <br>
 *     <br>
 *     e.g.
 *     <pre>
 *     &lt;GetDelegatedAdminConstraintsRequest type="cos" name="cos1">
 *       &lt;a name="zimbraMailQuota">
 *     &lt;/GetDelegatedAdminConstraintsRequest>
 *
 *     &lt;GetDelegatedAdminConstraintsResponse type="cos" id="e00428a1-0c00-11d9-836a-000d93afea2a" name="cos1">
 *       &lt;a n="zimbraMailQuota">
 *         &lt;constraint>
 *           &lt;max>524288000&lt;/max>
 *           &lt;min>20971520&lt;/min>
 *         &lt;/constraint>
 *       &lt;/a>
 *     &lt;/GetDelegatedAdminConstraintsResponse>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_DELEGATED_ADMIN_CONSTRAINTS_REQUEST)
public class GetDelegatedAdminConstraintsRequest {

  /**
   * @zm-api-field-description Target Type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE, required = true)
  private final TargetType type;

  /**
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = AdminConstants.A_ID, required = false)
  private final String id;

  /**
   * @zm-api-field-description name
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = false)
  private final String name;

  /**
   * @zm-api-field-description Attrs
   */
  @XmlElement(name = AdminConstants.E_A, required = false)
  private List<NamedElement> attrs = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetDelegatedAdminConstraintsRequest() {
    this((TargetType) null, (String) null, (String) null);
  }

  public GetDelegatedAdminConstraintsRequest(TargetType type, String id, String name) {
    this.type = type;
    this.id = id;
    this.name = name;
  }

  public void setAttrs(Iterable<NamedElement> attrs) {
    this.attrs.clear();
    if (attrs != null) {
      Iterables.addAll(this.attrs, attrs);
    }
  }

  public GetDelegatedAdminConstraintsRequest addAttr(NamedElement attr) {
    this.attrs.add(attr);
    return this;
  }

  public TargetType getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<NamedElement> getAttrs() {
    return Collections.unmodifiableList(attrs);
  }
}
