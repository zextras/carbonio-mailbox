// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class TagSpec {

  /**
   * @zm-api-field-tag tag-name
   * @zm-api-field-description Tag name
   */
  @XmlAttribute(name = MailConstants.A_NAME, required = true)
  private final String name;

  /**
   * @zm-api-field-tag rgb-color
   * @zm-api-field-description RGB color in format #rrggbb where r,g and b are hex digits
   */
  @XmlAttribute(name = MailConstants.A_RGB, required = false)
  private String rgb;

  /**
   * @zm-api-field-tag color
   * @zm-api-field-description color numeric; range 0-127; defaults to 0 if not present; client can
   *     display only 0-7
   */
  @XmlAttribute(name = MailConstants.A_COLOR, required = false)
  private Byte color;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private TagSpec() {
    this((String) null);
  }

  public TagSpec(String name) {
    this.name = name;
  }

  public void setRgb(String rgb) {
    this.rgb = rgb;
  }

  public void setColor(Byte color) {
    this.color = color;
  }

  public String getName() {
    return name;
  }

  public String getRgb() {
    return rgb;
  }

  public Byte getColor() {
    return color;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("rgb", rgb)
        .add("color", color)
        .toString();
  }
}
