package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-description Search Users enabled to a particular Feature
 * <br />
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name= AccountConstants.E_SEARCH_USERS_BY_FEATURE_REQUEST)
public class SearchdUsersByFeatureRequest {

  public enum Features {
    CHATS("carbonioFeatureChatsEnabled"),
    UNKNOWN("");

    private final String feature;

    Features(String feature) {
      this.feature = feature;
    }

    public String getFeature() {
      return feature;
    }
  }
  /**
   * @zm-api-field-description name to autocomplete (searched in uid, mail and displayName)
   */
  @XmlAttribute(name=AccountConstants.E_NAME, required=false)
  private String name;

  /**
   * @zm-api-field-description feature to check (only accounts with this feature enabled will be returned).
   */
  @XmlAttribute(name=AccountConstants.E_FEATURE, required=false)
  private Features feature;
  /**
   * @zm-api-field-description The maximum number of accounts to return (0 is default and means 10)
   */
  @XmlAttribute(name=AccountConstants.A_LIMIT, required=false)
  private Integer limit;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Features getFeature() {
    return feature;
  }

  public void setFeature(Features feature) {
    this.feature = feature;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }
}
