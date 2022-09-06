// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp;

import static com.zimbra.cs.html.owasp.HtmlElementsBuilder.COMMA;

import com.zimbra.cs.html.owasp.policies.ActionAttributePolicy;
import com.zimbra.cs.html.owasp.policies.BackgroundAttributePolicy;
import com.zimbra.cs.html.owasp.policies.SrcAttributePolicy;
import java.util.HashMap;
import java.util.Map;
import org.owasp.html.AttributePolicy;

public class HtmlAttributesBuilder {

  private Map<String, AttributePolicy> attributePolicies = new HashMap<String, AttributePolicy>();

  public HtmlAttributesBuilder() {}

  public void setUp() {
    attributePolicies.put("src", new SrcAttributePolicy());
    attributePolicies.put("action", new ActionAttributePolicy());
    attributePolicies.put("background", new BackgroundAttributePolicy());
    // add any other attribute policies here
  }

  public Map<String, AttributePolicy> build(String element) {
    setUp();
    Map<String, AttributePolicy> attributesAndPolicies = new HashMap<>();
    String allowedString = OwaspPolicy.getAttributes(element);
    String[] allowedAttributes = allowedString.split(COMMA);
    for (String attribute : allowedAttributes) {
      AttributePolicy attrPolicy = attributePolicies.get(attribute);
      attributesAndPolicies.put(attribute, attrPolicy);
    }
    return attributesAndPolicies;
  }
}
