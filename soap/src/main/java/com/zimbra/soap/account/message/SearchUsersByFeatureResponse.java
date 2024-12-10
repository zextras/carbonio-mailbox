package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.ZmBoolean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name= AccountConstants.E_SEARCH_USERS_BY_FEATURE_RESPONSE)
@XmlType(propOrder = {})
public class SearchUsersByFeatureResponse {
  /**
   * @zm-api-field-description Accounts matching the autocomplete request
   */
  @XmlElement(name=AccountConstants.E_ACCOUNT /* cn */, required=false)
  private List<UserInfo> accounts = Lists.newArrayList();

  /**
   * @zm-api-field-description The total number of accounts matching the request
   */
  @XmlAttribute(name=AccountConstants.A_TOTAL, required=false)
  private Integer total;

  /**
   * @zm-api-field-description Whether there are more accounts to fetch (for pagination)
   */
  @XmlAttribute(name=AccountConstants.A_MORE, required=false)
  private ZmBoolean more;

  public SearchUsersByFeatureResponse() {
  }

  public void setAccounts(Iterable <UserInfo> accounts) {
    this.accounts.clear();
    if (accounts != null) {
      Iterables.addAll(this.accounts,accounts);
    }
  }

  public void addAccount(UserInfo account) {
    this.accounts.add(account);
  }

  public List<UserInfo> getAccounts() {
    return accounts;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getTotal() {
    return total;
  }

  public void setMore(boolean more) {
    this.more = ZmBoolean.fromBool(more);
  }

  public ZmBoolean getMore() {
    return more;
  }

  public MoreObjects.ToStringHelper addToStringInfo(
      MoreObjects.ToStringHelper helper) {
    return helper
        .add("accounts", accounts);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this))
        .toString();
  }
}
