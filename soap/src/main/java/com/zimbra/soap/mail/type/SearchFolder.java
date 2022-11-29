// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.EnumSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.SearchSortBy;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

/*
  <search id="..." name="..." query="..." [types="..."] [sortBy="..."] l="{folder}"/>+

 */
// Root element name needed to differentiate between types of folder
// MailConstants.E_SEARCH == "search"
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_SEARCH)
@GraphQLType(name=GqlConstants.CLASS_SEARCH_FOLDER, description="Search folder details")
public final class SearchFolder extends Folder {

    /**
     * @zm-api-field-tag query
     * @zm-api-field-description Query
     */
    @XmlAttribute(name=MailConstants.A_QUERY /* query */, required=false)
    private String query;

    /**
     * @zm-api-field-tag sort-by
     * @zm-api-field-description Sort by
     */
    @XmlAttribute(name=MailConstants.A_SORTBY /* sortBy */, required=false)
    private SearchSortBy sortBy;

    /**
     * @zm-api-field-tag comma-sep-search-types
     * @zm-api-field-description Comma-separated list.  Legal values in list are:
     * <br />
     * <b>appointment|chat|contact|conversation|document|message|tag|task|wiki</b>
     * (default is &quot;conversation&quot;)
     */
    @XmlAttribute(name=MailConstants.A_SEARCH_TYPES /* types */, required=false)
    @XmlJavaTypeAdapter(ItemType.CSVAdapter.class)
    private final Set<ItemType> types = EnumSet.noneOf(ItemType.class);

    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.QUERY, description="query to search")
    public String getQuery() {
        return query;
    }

    @GraphQLInputField(name=GqlConstants.QUERY, description="query to search")
    public void setQuery(String query) {
        this.query = query;
    }

    @GraphQLQuery(name=GqlConstants.SORT_BY, description="sort order for the search results")
    public SearchSortBy getSortBy() {
        return sortBy;
    }

    @GraphQLInputField(name=GqlConstants.SORT_BY, description="sort order for the search results")
    public void setSortBy(SearchSortBy sortBy) {
        this.sortBy = sortBy;
    }

    @GraphQLQuery(name=GqlConstants.SEARCH_TYPES, description="type of the new folder created")
    public Set<ItemType> getTypes() {
        return types;
    }

    @GraphQLInputField(name=GqlConstants.SEARCH_TYPES, description="type of the new folder created")
    public void setTypes(Set<ItemType> set) {
        types.clear();
        types.addAll(set);
    }

    public void addType(ItemType type) {
        types.add(type);
    }
}
