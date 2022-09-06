// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp;

import static com.zimbra.cs.html.owasp.HtmlElementsBuilder.COMMA;

import com.google.common.base.Optional;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.html.owasp.policies.BackgroundPolicy;
import com.zimbra.cs.html.owasp.policies.SrcAttributePolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.owasp.html.AttributePolicy;
import org.owasp.html.ElementPolicy;
import org.owasp.html.FilterUrlByProtocolAttributePolicy;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlPolicyBuilder.AttributeBuilder;

/*
 * HTML element along with its policy and attributes
 */
public class HtmlElement {

  private String element;
  private Optional<ElementPolicy> elementPolicy;
  private Map<String, AttributePolicy> attributesAndPolicies;

  HtmlElement(
      String element,
      ElementPolicy elementPolicy,
      Map<String, AttributePolicy> attributesAndPolicies) {
    this.element = element;
    this.elementPolicy = Optional.fromNullable(elementPolicy);
    this.attributesAndPolicies = attributesAndPolicies;
  }

  public String getElement() {
    return element;
  }

  public void configure(HtmlPolicyBuilder policyBuilder, boolean neuterImages) {
    String elementName = getElement();
    if (elementPolicy.isPresent()) {
      policyBuilder.allowElements(elementPolicy.get(), elementName);
    } else {
      policyBuilder.allowElements(elementName);
    }
    if (neuterImages) {
      policyBuilder.allowElements(new BackgroundPolicy(), elementName);
    }
    Set<String> allowedAttributes = attributesAndPolicies.keySet();
    AttributeBuilder attributesBuilder = null;
    for (String attribute : allowedAttributes) {
      attributesBuilder = policyBuilder.allowAttributes(attribute);
      if (attributesBuilder != null) {
        Set<String> urlProtocolAttributes = OwaspPolicy.getElementUrlProtocolAttributes();
        if (urlProtocolAttributes.contains(attribute)) {
          String urlProtocols = OwaspPolicy.getElementUrlProtocols(element);
          if (!StringUtil.isNullOrEmpty(urlProtocols)) {
            String[] allowedProtocols = urlProtocols.split(COMMA);
            List<String> urlList = Arrays.asList(allowedProtocols);
            AttributePolicy URLPolicy = new FilterUrlByProtocolAttributePolicy(urlList);
            attributesBuilder.matching(URLPolicy);
          }
        }
        AttributePolicy attrPolicy = attributesAndPolicies.get(attribute);
        if (attrPolicy != null) {
          if (attrPolicy instanceof SrcAttributePolicy) {
            if (neuterImages && (elementName.equals("img") || elementName.equals("input"))) {
              attributesBuilder.matching(attrPolicy);
            }
          } else {
            attributesBuilder.matching(attrPolicy);
          }
        }
        attributesBuilder.onElements(elementName);
      }
    }
  }
}
