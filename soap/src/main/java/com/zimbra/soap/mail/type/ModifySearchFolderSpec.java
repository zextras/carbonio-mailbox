// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(
    name = GqlConstants.CLASS_MODIFY_SEARCH_FOLDER_SPEC,
    description = "Input for modifying an existing search folder")
public class ModifySearchFolderSpec {

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  @GraphQLNonNull
  @GraphQLInputField(
      name = GqlConstants.SEARCH_FOLDER_ID,
      description = "Search folder id to be edited")
  private final String id;

  /**
   * @zm-api-field-tag query
   * @zm-api-field-description Query
   */
  @XmlAttribute(name = MailConstants.A_QUERY /* query */, required = true)
  @GraphQLNonNull
  @GraphQLInputField(name = GqlConstants.QUERY, description = "New search query")
  private final String query;

  /**
   * @zm-api-field-tag search-types
   * @zm-api-field-description Search types
   */
  @XmlAttribute(name = MailConstants.A_SEARCH_TYPES /* types */, required = false)
  @GraphQLInputField(
      name = GqlConstants.SEARCH_TYPES,
      description = "New type for the search folder")
  private String searchTypes;

  /**
   * @zm-api-field-tag sort-by
   * @zm-api-field-description Sort by
   */
  @XmlAttribute(name = MailConstants.A_SORTBY /* sortBy */, required = false)
  @GraphQLInputField(name = GqlConstants.SORT_BY, description = "New sort order for ")
  private String sortBy;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ModifySearchFolderSpec() {
    this((String) null, (String) null);
  }

  public ModifySearchFolderSpec(String id, String query) {
    this.id = id;
    this.query = query;
  }

  public void setSearchTypes(String searchTypes) {
    this.searchTypes = searchTypes;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public String getId() {
    return id;
  }

  public String getQuery() {
    return query;
  }

  public String getSearchTypes() {
    return searchTypes;
  }

  public String getSortBy() {
    return sortBy;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("id", id)
        .add("query", query)
        .add("searchTypes", searchTypes)
        .add("sortBy", sortBy);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
