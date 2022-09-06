// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ConstraintAttr;
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
 * @zm-api-command-description Modify constraint (zimbraConstraint) for delegated admin on global
 *     config or a COS <br>
 *     If constraints for an attribute already exists, it will be replaced by the new constraints.
 *     If <b>&lt;constraint></b> is an empty element, constraints for the attribute will be removed.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_MODIFY_DELEGATED_ADMIN_CONSTRAINTS_REQUEST)
public class ModifyDelegatedAdminConstraintsRequest {

  /**
   * @zm-api-field-description Target type
   */
  @XmlAttribute(name = AdminConstants.A_TYPE, required = true)
  private final TargetType type;

  /**
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = AdminConstants.A_ID, required = false)
  private final String id;

  /**
   * @zm-api-field-description Name
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = false)
  private final String name;

  /**
   * @zm-api-field-description Constaint attributes
   */
  @XmlElement(name = AdminConstants.E_A, required = false)
  private List<ConstraintAttr> attrs = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ModifyDelegatedAdminConstraintsRequest() {
    this((TargetType) null, (String) null, (String) null);
  }

  public ModifyDelegatedAdminConstraintsRequest(TargetType type, String id, String name) {
    this.type = type;
    this.id = id;
    this.name = name;
  }

  public void setAttrs(Iterable<ConstraintAttr> attrs) {
    this.attrs.clear();
    if (attrs != null) {
      Iterables.addAll(this.attrs, attrs);
    }
  }

  public ModifyDelegatedAdminConstraintsRequest addAttr(ConstraintAttr attr) {
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

  public List<ConstraintAttr> getAttrs() {
    return Collections.unmodifiableList(attrs);
  }
}
