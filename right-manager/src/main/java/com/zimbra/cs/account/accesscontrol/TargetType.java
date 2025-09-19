// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeClass;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlEnum;

/**
 * @author pshao
 */
public enum TargetType {
  account(true, true, AttributeClass.account, SoapTargetType.account, "Account"),
  calresource(
      true,
      true,
      AttributeClass.calendarResource,
      SoapTargetType.calresource,
      "CalendarResource"),
  cos(true, false, AttributeClass.cos, SoapTargetType.cos, "Cos"),
  dl(
      true,
      true,
      AttributeClass.distributionList,
      SoapTargetType.dl,
      "DistributionList"), // static group
  group(
      true,
      true,
      AttributeClass.group,
      SoapTargetType.group,
      "DynamicGroup"), // dynamic group
  domain(true, false, AttributeClass.domain, SoapTargetType.domain, "Domain"),
  server(true, false, AttributeClass.server, SoapTargetType.server, "Server"),
  xmppcomponent(
      true,
      false,
      AttributeClass.xmppComponent,
      SoapTargetType.xmppcomponent,
      "XMPPComponent"),
  zimlet(true, false, AttributeClass.zimletEntry, SoapTargetType.zimlet, "Zimlet"),
  config(
      false,
      false,
      AttributeClass.globalConfig,
      SoapTargetType.config,
      "GlobalConfig"),
  global(
      false,
      false,
      AttributeClass.aclTarget,
      SoapTargetType.global,
      "GlobalGrant");

  private boolean mNeedsTargetIdentity;
  private boolean mIsDomained;
  private AttributeClass mAttrClass;
  private SoapTargetType jaxbTargetType;
  private String mPrettyName;

  //
  // mInheritedByTargetTypes and mInheritFromTargetTypes represents
  // the same fact from two opposite directions
  //

  // set of target types that can inherit from this target type
  // e.g. if this target type is domain, the set would be
  //      account, calresource, dl, group, domain
  private Set<TargetType> mInheritedByTargetTypes;

  // set of target types this target type can inherit from
  // e.g. if this target type is domain, the set would be
  //      globalGrant, domain
  private Set<TargetType> mInheritFromTargetTypes;

  // pretty much like mInheritedByTargetTypes, but this is for LDAP
  // search of sub-targets of a target type.  This Set is different
  // from the mInheritedByTargetTypes that it does not contain self
  private Set<TargetType> mSubTargetTypes;

  static {
    init();
  }

  TargetType(
      boolean NeedsTargetIdentity,
      boolean isDomained,
      AttributeClass attrClass,
      SoapTargetType jaxbTargetType,
      String prettyName) {
    mNeedsTargetIdentity = NeedsTargetIdentity;
    mIsDomained = isDomained;
    mAttrClass = attrClass;
    this.jaxbTargetType = jaxbTargetType;
    mPrettyName = prettyName;
  }

  /* return equivalent JAXB enum */
  public SoapTargetType toJaxb() {
    return jaxbTargetType;
  }

  public static TargetType fromJaxb(SoapTargetType jaxbTT) {
    for (TargetType tt : TargetType.values()) {
      if (tt.toJaxb() == jaxbTT) {
        return tt;
      }
    }
    throw new IllegalArgumentException("Unrecognised TargetType" + jaxbTT);
  }

  private void setInheritedByTargetTypes(TargetType[] targetTypes) {
    mInheritedByTargetTypes = new HashSet<>(Arrays.asList(targetTypes));
  }

  static void init() {
    TargetType.account.setInheritedByTargetTypes(new TargetType[] {account});

    TargetType.calresource.setInheritedByTargetTypes(new TargetType[] {calresource});

    TargetType.dl.setInheritedByTargetTypes(new TargetType[] {account, calresource, dl});

    TargetType.group.setInheritedByTargetTypes(new TargetType[] {account, calresource, group});

    TargetType.domain.setInheritedByTargetTypes(
        new TargetType[] {account, calresource, dl, group, domain});

    TargetType.cos.setInheritedByTargetTypes(new TargetType[] {cos});

    TargetType.server.setInheritedByTargetTypes(new TargetType[] {server});

    TargetType.xmppcomponent.setInheritedByTargetTypes(new TargetType[] {xmppcomponent});

    TargetType.zimlet.setInheritedByTargetTypes(new TargetType[] {zimlet});

    TargetType.config.setInheritedByTargetTypes(new TargetType[] {config});

    TargetType.global.setInheritedByTargetTypes(
        new TargetType[] {
          account,
          calresource,
          cos,
          dl,
          group,
          domain,
          server,
          xmppcomponent,
          zimlet,
          config,
          global
        }); // inherited by all

    // compute mInheritFromTargetTypes and mSubTargetTypes
    // from mInheritedByTargetTypes
    for (TargetType inheritFrom : TargetType.values()) {
      inheritFrom.mInheritFromTargetTypes = new HashSet<>();
      inheritFrom.mSubTargetTypes = new HashSet<>();

      for (TargetType inheritedBy : TargetType.values()) {
        if (inheritedBy.mInheritedByTargetTypes.contains(inheritFrom)) {
          inheritFrom.mInheritFromTargetTypes.add(inheritedBy);
        }
      }

      for (TargetType tt : inheritFrom.mInheritedByTargetTypes) {
        if (inheritFrom != tt) {
          inheritFrom.mSubTargetTypes.add(tt);
        }
      }
    }

    for (TargetType tt : TargetType.values()) {
      tt.mInheritedByTargetTypes = Collections.unmodifiableSet(tt.mInheritedByTargetTypes);
      tt.mInheritFromTargetTypes = Collections.unmodifiableSet(tt.mInheritFromTargetTypes);
      tt.mSubTargetTypes = Collections.unmodifiableSet(tt.mSubTargetTypes);
    }

    /*
    for (TargetType tt : TargetType.values()) {
        tt.dump();
    }
    */
  }

  /**
   * returns if targetType can inherit from this targetType
   *
   * @param targetType the targetType of question
   * @return
   */
  boolean isInheritedBy(TargetType targetType) {
    return mInheritedByTargetTypes.contains(targetType);
  }

  /**
   * returns the set of sub target types this target type can be inherited by. do not include the
   * target type itself.
   *
   * <p>e.g. if this is domain, then account, calresource, and dl will be returned
   *
   * @return
   */
  Set<TargetType> subTargetTypes() {
    return mSubTargetTypes;
  }

  /**
   * returns the set of target types this target type can inherit from
   *
   * @return
   */
  Set<TargetType> inheritFrom() {
    return mInheritFromTargetTypes;
  }

  public static TargetType fromCode(String s) throws ServiceException {
    try {
      return TargetType.valueOf(s);
    } catch (IllegalArgumentException e) {
      throw ServiceException.INVALID_REQUEST("unknown target type: " + s, e);
    }
  }

  public String getCode() {
    return name();
  }

  public String getPrettyName() {
    return mPrettyName;
  }

  public boolean needsTargetIdentity() {
    return mNeedsTargetIdentity;
  }

  AttributeClass getAttributeClass() {
    return mAttrClass;
  }

  boolean isDomained() {
    return mIsDomained;
  }

  public boolean isGroup() {
    return (this == TargetType.dl || this == TargetType.group);
  }

  static String getCommonBase(String dn1, String dn2) {
    String top = "";

    if (top.equals(dn1) || top.equals(dn2)) return top;

    String[] rdns1 = dn1.split(",");
    String[] rdns2 = dn2.split(",");

    String[] shorter = rdns1.length < rdns2.length ? rdns1 : rdns2;

    int i = 0;
    while (i < shorter.length) {
      if (!rdns1[rdns1.length - 1 - i].equals(rdns2[rdns2.length - 1 - i])) break;
      else
        ;
      i++;
    }

    StringBuilder sb = new StringBuilder();
    for (int j = shorter.length - i; j < shorter.length; j++) {
      if (j != shorter.length - i) sb.append(",");
      sb.append(shorter[j]);
    }

    return sb.toString();
  }

  public static TargetType[] valuesWithoutXmppComponent() {
    TargetType[] values = values();
    return Arrays.stream(values)
            .filter(targetType -> !targetType.equals(TargetType.xmppcomponent))
            .toArray( TargetType[]::new );
  }

  /** JAXB analog to {@com.zimbra.cs.account.accesscontrol.TargetType} */
  @XmlEnum
  public enum SoapTargetType {
    // case must match protocol
    account,
    calresource,
    cos,
    dl,
    group,
    domain,
    server,
    xmppcomponent,
    zimlet,
    config,
    global;

    public static SoapTargetType fromString(String s) throws ServiceException {
      try {
        return SoapTargetType.valueOf(s);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST(
            "unknown 'TargetType' key: "
                + s
                + ", valid values: "
                + Arrays.asList(SoapTargetType.values()),
            null);
      }
    }
  }
}
