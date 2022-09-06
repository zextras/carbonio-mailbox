// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json.jackson.annotate;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Marker annotation used in Zimbra JAXB classes to affect how they are serialized to Zimbra style
 * JSON. Used in conjunction with {@link XmlElementWrapper} to indicate that the property for the
 * wrapper should be treated as an array. {@link ZimbraJsonArrayForWrapper} should only be used in
 * JAXB for legacy API compatibility where wrapper elements were added with {@code addElement}
 * instead of {@code addUniqueElement}.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface ZimbraJsonArrayForWrapper {
  /**
   * Optional argument that defines whether this annotation is active or not. The only use for value
   * 'false' is for overriding purposes. Overriding may be necessary when used with "mix-in
   * annotations" (aka "annotation overrides"). For most cases, however, default value of "true" is
   * just fine and should be omitted.
   */
  boolean value() default true;
}
