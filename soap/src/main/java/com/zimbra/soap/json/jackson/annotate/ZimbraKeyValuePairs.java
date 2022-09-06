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

/**
 * Marker annotation used in Zimbra JAXB classes to affect how they are serialized to Zimbra style
 * JSON. The {@code ZimbraKeyValuePairs} in the following code snippet:
 *
 * <pre>
 *     @ZimbraKeyValuePairs
 *     @XmlElement(name=Element.XMLElement.E_ATTRIBUTE, required=false)
 *     private final List<KeyValuePair> attrList;
 * </pre>
 *
 * causes serialization to JSON in the form :
 *
 * <pre>
 * "_attrs": {
 *         "mail": "fun@example.test",
 *         "zimbraMailStatus": "enabled"
 *       }
 * </pre>
 *
 * instead of:
 *
 * <pre>
 * "a": [
 *          {
 *            "n": "mail",
 *            "_content": "fun@example.test"
 *          },
 *          {
 *            "n": "zimbraMailStatus",
 *            "_content": "enabled"
 *          }
 *      ]
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface ZimbraKeyValuePairs {
  /**
   * Optional argument that defines whether this annotation is active or not. The only use for value
   * 'false' is for overriding purposes. Overriding may be necessary when used with "mix-in
   * annotations" (aka "annotation overrides"). For most cases, however, default value of "true" is
   * just fine and should be omitted.
   */
  boolean value() default true;
}
