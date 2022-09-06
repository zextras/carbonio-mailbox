// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ExceptionRuleInfoInterface;
import com.zimbra.soap.base.RecurrenceInfoInterface;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(
    name = GqlConstants.CLASS_EXCEPTION_RULE_INFORMATION,
    description = "Exception rule information")
public class ExceptionRuleInfo extends RecurIdInfo
    implements RecurRuleBase, ExceptionRuleInfoInterface {

  /**
   * @zm-api-field-description Dates or rules which ADD instances. ADDs are evaluated before
   *     EXCLUDEs
   */
  @XmlElement(name = MailConstants.E_CAL_ADD /* add */, required = false)
  private RecurrenceInfo add;

  /**
   * @zm-api-field-description Dates or rules which EXCLUDE instances
   */
  @XmlElement(name = MailConstants.E_CAL_EXCLUDE /* exclude */, required = false)
  private RecurrenceInfo exclude;

  public ExceptionRuleInfo() {}

  @GraphQLInputField(
      name = GqlConstants.ADD,
      description = "Dates or rules which ADD instances.  ADDs are evaluated before EXCLUDEs")
  public void setAdd(RecurrenceInfo add) {
    this.add = add;
  }

  @GraphQLInputField(
      name = GqlConstants.EXCLUDE,
      description = "Dates or rules which EXCLUDE instances")
  public void setExclude(RecurrenceInfo exclude) {
    this.exclude = exclude;
  }

  @GraphQLQuery(
      name = GqlConstants.ADD,
      description = "Dates or rules which ADD instances.  ADDs are evaluated before EXCLUDEs")
  public RecurrenceInfo getAdd() {
    return add;
  }

  @GraphQLQuery(name = GqlConstants.EXCLUDE, description = "Dates or rules which EXCLUDE instances")
  public RecurrenceInfo getExclude() {
    return exclude;
  }

  @Override
  @GraphQLIgnore
  public void setAddInterface(RecurrenceInfoInterface add) {
    setAdd((RecurrenceInfo) add);
  }

  @Override
  @GraphQLIgnore
  public void setExcludeInterface(RecurrenceInfoInterface exclude) {
    setExclude((RecurrenceInfo) exclude);
  }

  @Override
  @GraphQLIgnore
  public RecurrenceInfoInterface getAddInterface() {
    return (RecurrenceInfo) add;
  }

  @Override
  @GraphQLIgnore
  public RecurrenceInfoInterface getExcludeInterface() {
    return (RecurrenceInfo) exclude;
  }

  @Override
  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper.add("add", add).add("exclude", exclude);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
