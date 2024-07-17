package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.account.type.ContactInfo;
import com.zimbra.soap.admin.type.AccountInfo;
import com.zimbra.soap.base.ContactInterface;
import com.zimbra.soap.type.ZmBoolean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name= AccountConstants.E_SEARCH_ENABLED_USERS_RESPONSE)
@XmlType(propOrder = {})
public class SearchEnabledUsersResponse {
  /**
   * @zm-api-field-tag more-flag
   * @zm-api-field-description Set to 1 if the results were truncated
   */
  @XmlAttribute(name= MailConstants.A_QUERY_MORE /* more */, required=false)
  private ZmBoolean more;

  /**
   * @zm-api-field-description Flag if pagination is supported
   */
  @XmlAttribute(name=AccountConstants.A_PAGINATION_SUPPORTED /* paginationSupported */, required=false)
  private ZmBoolean pagingSupported;

  /**
   * @zm-api-field-description Contacts matching the autocomplete request
   */
  @XmlElement(name=AccountConstants.E_ACCOUNT /* cn */, required=false)
  private List<AccountInfo> accounts = Lists.newArrayList();

  // Believe that GalSearchResultCallback.handleDeleted(ItemId id) is not used for AutoCompleteGal
  // If it were used - it would matter what order as it would probably be a list of Id

  public SearchEnabledUsersResponse() {
  }

  private SearchEnabledUsersResponse(Boolean more) {
    this.setMore(more);
  }

  public void setMore(Boolean more) { this.more = ZmBoolean.fromBool(more); }

  public void setPagingSupported(Boolean pagingSupported) { this.pagingSupported = ZmBoolean.fromBool(pagingSupported); }

  public void setAccounts(Iterable <AccountInfo> accounts) {
    this.accounts.clear();
    if (accounts != null) {
      Iterables.addAll(this.accounts,accounts);
    }
  }

  public void addAccount(AccountInfo account) {
    this.accounts.add(account);
  }

  public Boolean getMore() { return ZmBoolean.toBool(more); }

  public Boolean getPagingSupported() { return ZmBoolean.toBool(pagingSupported); }
  public List<AccountInfo> getAccounts() {
    return accounts;
  }

  public MoreObjects.ToStringHelper addToStringInfo(
      MoreObjects.ToStringHelper helper) {
    return helper
        .add("more", more)
        .add("pagingSupported", pagingSupported)
        .add("accounts", accounts);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this))
        .toString();
  }
}
