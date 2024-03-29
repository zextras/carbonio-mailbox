// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.account.type.EntrySearchFilterInfo;
import com.zimbra.soap.type.AttributeSelectorImpl;
import com.zimbra.soap.type.CursorInfo;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Search Global Address List (GAL) for calendar resources <br>
 *     "attrs" attribute - comma-separated list of attrs to return ("displayName", "zimbraId",
 *     "zimbraCalResType")
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_SEARCH_CALENDAR_RESOURCES_REQUEST)
public class SearchCalendarResourcesRequest extends AttributeSelectorImpl {

  // TODO: Is this appropriate to SearchCalendarResourcesRequest?
  /**
   * @zm-api-field-tag
   * @zm-api-field-description "Quick" flag. <br>
   *     For performance reasons, the index system accumulates messages with not-indexed-yet state
   *     until a certain threshold and indexes them as a batch. To return up-to-date search results,
   *     the index system also indexes those pending messages right before a search. To lower
   *     latencies, this option gives a hint to the index system not to trigger this catch-up index
   *     prior to the search by giving up the freshness of the search results, i.e. recent messages
   *     may not be included in the search results.
   */
  @XmlAttribute(name = MailConstants.A_QUICK /* quick */, required = false)
  private ZmBoolean quick;

  // Based on SortBy which is NOT an enum and appears to support runtime construction
  /**
   * @zm-api-field-tag sort-by
   * @zm-api-field-description Name of attribute to sort on. default is the calendar resource name.
   */
  @XmlAttribute(name = MailConstants.A_SORTBY /* sortBy */, required = false)
  private String sortBy;

  /**
   * @zm-api-field-description An integer specifying the 0-based offset into the results list to
   *     return as the first result for this search operation
   */
  @XmlAttribute(name = MailConstants.A_QUERY_LIMIT /* limit */, required = false)
  private Integer limit;

  /**
   * @zm-api-field-description The 0-based offset into the results list to return as the first
   *     result for this search operation.
   */
  @XmlAttribute(name = MailConstants.A_QUERY_OFFSET /* offset */, required = false)
  private Integer offset;

  /**
   * @zm-api-field-tag locale-name
   * @zm-api-field-description Client locale identification. <br>
   *     Value is of the form LL-CC[-V+] where: <br>
   *     LL is two character language code <br>
   *     CC is two character country code <br>
   *     V+ is optional variant identifier string <br>
   *     <br>
   *     See: <br>
   *     ISO Language Codes: http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt <br>
   *     ISO Country Codes: http://www.chemie.fu-berlin.de/diverse/doc/ISO_3166.html
   */
  @XmlElement(name = MailConstants.E_LOCALE /* locale */, required = false)
  private String locale;

  /**
   * @zm-api-field-description Cursor specification
   */
  @XmlElement(name = MailConstants.E_CURSOR /* cursor */, required = false)
  private CursorInfo cursor;

  // May be added by GalSearchControl.proxyGalAccountSearch
  /**
   * @zm-api-field-tag gal-account-id
   * @zm-api-field-description GAL Account ID
   */
  @XmlAttribute(name = AccountConstants.A_GAL_ACCOUNT_ID /* galAcctId */, required = false)
  private String galAccountId;

  // The text of this is supplied to GalSearchParams.setQuery(String)
  /**
   * @zm-api-field-tag gal-search-key
   * @zm-api-field-description If specified, passed through to the GAL search as the search key
   */
  @XmlElement(name = AccountConstants.E_NAME /* name */, required = false)
  private final String name;

  /**
   * @zm-api-field-description Search filter specification
   */
  @XmlElement(name = AccountConstants.E_ENTRY_SEARCH_FILTER /* searchFilter */, required = false)
  private EntrySearchFilterInfo searchFilter;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SearchCalendarResourcesRequest() {
    this(null);
  }

  public SearchCalendarResourcesRequest(String name) {
    this.name = name;
  }

  public void setQuick(Boolean quick) {
    this.quick = ZmBoolean.fromBool(quick);
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setCursor(CursorInfo cursor) {
    this.cursor = cursor;
  }

  public void setGalAccountId(String galAccountId) {
    this.galAccountId = galAccountId;
  }

  public void setSearchFilter(EntrySearchFilterInfo searchFilter) {
    this.searchFilter = searchFilter;
  }

  public Boolean getQuick() {
    return ZmBoolean.toBool(quick);
  }

  public String getSortBy() {
    return sortBy;
  }

  public Integer getLimit() {
    return limit;
  }

  public Integer getOffset() {
    return offset;
  }

  public String getLocale() {
    return locale;
  }

  public CursorInfo getCursor() {
    return cursor;
  }

  public String getGalAccountId() {
    return galAccountId;
  }

  public String getName() {
    return name;
  }

  public EntrySearchFilterInfo getSearchFilter() {
    return searchFilter;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper
        .add("quick", quick)
        .add("sortBy", sortBy)
        .add("limit", limit)
        .add("offset", offset)
        .add("locale", locale)
        .add("cursor", cursor)
        .add("galAccountId", galAccountId)
        .add("name", name)
        .add("searchFilter", searchFilter);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
