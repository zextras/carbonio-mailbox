package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
