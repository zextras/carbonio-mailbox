// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(
    name = GqlConstants.CLASS_MODIFY_CONTACT_GROUP_MEMBER,
    description = "Contact group members to modify")
public class ModifyContactGroupMember extends NewContactGroupMember {

  /**
   * @zm-api-field-tag member-operation
   * @zm-api-field-description Operation - <b>+|-|reset</b> <br>
   *     <b>Required</b> when replace-mode is unset, otherwise, it <b>must not be set </b> -
   *     INVALID_REQUEST will be thrown
   *     <table>
   * <tr> <td> <b>+</b>     </td> <td> add the member - {member-type} and {member-value} required </td> </tr>
   * <tr> <td> <b>-</b>     </td> <td> remove the member - {member-type} and {member-value} required </td> </tr>
   * <tr> <td> <b>reset</b> </td> <td> delete all pre-existing members </td> </tr>
   * </table>
   */
  @XmlAttribute(name = MailConstants.A_OPERATION /* op */, required = false)
  private ModifyGroupMemberOperation operation;

  public ModifyContactGroupMember() {}

  public ModifyContactGroupMember(String type, String value) {
    super(type, value);
  }

  public static ModifyContactGroupMember createForTypeAndValue(String type, String value) {
    return new ModifyContactGroupMember(type, value);
  }

  @GraphQLInputField(name = GqlConstants.OPERATION, description = "Specify + or - to add or remove")
  public void setOperation(ModifyGroupMemberOperation operation) {
    this.operation = operation;
  }

  public ModifyGroupMemberOperation getOperation() {
    return operation;
  }

  @Override
  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return super.addToStringInfo(helper).add("operation", operation);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
