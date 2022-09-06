// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;

/**
 * @author zimbra
 */
@XmlAccessorType(XmlAccessType.NONE)
public class HABGroupOperation {

  @XmlEnum
  public enum HabGroupOp {
    // case must match
    move,
    assignSeniority;

    public static HabGroupOp fromString(String s) throws ServiceException {
      try {
        return HabGroupOp.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("unknown key: " + s, e);
      }
    }
  }

  /**
   * @zm-api-field-tag the HAB group Id
   * @zm-api-field-description the id of the HAB group whose parent is to be changed.
   */
  @XmlAttribute(name = AdminConstants.A_HAB_GROUP_ID /* habGroupId */, required = true)
  private String habGroupId;

  /**
   * @zm-api-field-tag the current parentHAB group Id
   * @zm-api-field-description the id of the HAB group which is the currrent parent of the HAB
   *     group.
   */
  @XmlAttribute(name = AdminConstants.A_CURRENT_PARENT_HAB_GROUP_ID /*
                                                                       * habCurretParentGroupId
                                                                       */, required = false)
  private String currentHabGroupId;

  /**
   * @zm-api-field-tag the target parent HAB group Id
   * @zm-api-field-description the id of the target parent HAB group.
   */
  @XmlAttribute(name = AdminConstants.A_TARGET_PARENT_HAB_GROUP_ID /*
                                                                      * habTargetParentGroupId
                                                                      */, required = false)
  private String targetHabGroupId;

  /**
   * @zm-api-field-tag seniorityIndex, not required, defaults to 0
   * @zm-api-field-description the seniorityInex of HAB group
   */
  @XmlAttribute(
      name = AccountConstants.A_HAB_SENIORITY_INDEX /* seniorityIndex */,
      required = false)
  private Integer seniorityIndex;

  /**
   * @zm-api-field-tag op
   * @zm-api-field-description operation
   */
  @XmlAttribute(name = AdminConstants.A_OPERATION /* op */, required = true)
  private HabGroupOp op;

  public HABGroupOperation() {}

  /**
   * @param habGroupId
   * @param currentHabGroupId
   * @param targetHabGroupId
   * @param op
   */
  public HABGroupOperation(
      String habGroupId, String currentHabGroupId, String targetHabGroupId, HabGroupOp op) {
    super();
    this.habGroupId = habGroupId;
    this.currentHabGroupId = currentHabGroupId;
    this.targetHabGroupId = targetHabGroupId;
    this.op = op;
  }

  /**
   * @param habGroupId id of HAB group
   * @param seniorityIndex seniorityIndex
   * @param op type of modify operation
   */
  public HABGroupOperation(String habGroupId, int seniorityIndex, HabGroupOp op) {
    super();
    this.habGroupId = habGroupId;
    this.seniorityIndex = seniorityIndex;
    this.op = op;
  }

  public String getHabGroupId() {
    return habGroupId;
  }

  public void setHabGroupId(String habGroupId) {
    this.habGroupId = habGroupId;
  }

  public String getCurrentHabGroupId() {
    return currentHabGroupId;
  }

  public void setCurrentHabGroupId(String currentHabGroupId) {
    this.currentHabGroupId = currentHabGroupId;
  }

  public String getTargetHabGroupId() {
    return targetHabGroupId;
  }

  public void setTargetHabGroupId(String targetHabGroupId) {
    this.targetHabGroupId = targetHabGroupId;
  }

  public HabGroupOp getOp() {
    return op;
  }

  public void setOp(HabGroupOp op) {
    this.op = op;
  }

  public Integer getSeniorityIndex() {
    return seniorityIndex;
  }

  public void setSeniorityIndex(Integer seniorityIndex) {
    this.seniorityIndex = seniorityIndex;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("HABGroupOperation [");
    if (habGroupId != null) {
      builder.append("habGroupId=");
      builder.append(habGroupId);
      builder.append(", ");
    }
    if (currentHabGroupId != null) {
      builder.append("currentHabGroupId=");
      builder.append(currentHabGroupId);
      builder.append(", ");
    }
    if (targetHabGroupId != null) {
      builder.append("targetHabGroupId=");
      builder.append(targetHabGroupId);
      builder.append(", ");
    }

    builder.append("seniorityIndex=");
    builder.append(seniorityIndex);
    builder.append(", ");
    if (op != null) {
      builder.append("op=");
      builder.append(op);
    }
    builder.append("]\n");
    return builder.toString();
  }
}
