package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.admin.type.Attr;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class EnabledUserInfo {

  public EnabledUserInfo() {
    this(null, null, null);
  }

  public EnabledUserInfo(String id, String name, Collection<Attr> attrs) {
    this.id = id;
    this.name = name;
    this.attrList = new ArrayList<>();
    if (attrs != null) {
      this.attrList.addAll(attrs);
    }
  }

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Name
   */
  @XmlAttribute(name= AccountConstants.A_NAME /* name */, required=true)
  private final String name;

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description ID
   */
  @XmlAttribute(name=AccountConstants.A_ID /* id */, required=true)
  private final String id;

  /**
   * @zm-api-field-description Attributes
   */
  @XmlElement(name=AccountConstants.E_A /* a */, required=false)
  private final List<Attr> attrList;

  public String getName() { return name; }

  public String getId() { return id; }

  public List<Attr> getAttrList() {
    return Collections.unmodifiableList(attrList);
  }
}
