// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp;

import com.zimbra.cs.html.owasp.policies.AElementPolicy;
import com.zimbra.cs.html.owasp.policies.AreaElementPolicy;
import com.zimbra.cs.html.owasp.policies.BaseElementPolicy;
import com.zimbra.cs.html.owasp.policies.ImgInputElementPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.owasp.html.ElementPolicy;

/*
 * Build the list of HtmlElements from policy file
 */
public class HtmlElementsBuilder {

  static final String COMMA = "(\\s)?+,(\\s)?+";
  private HtmlAttributesBuilder builder;
  private boolean neuterImages;
  private Map<String, ElementPolicy> elementSpecificPolicies = new HashMap<String, ElementPolicy>();

  public HtmlElementsBuilder(HtmlAttributesBuilder builder, boolean neuterImages) {
    this.builder = builder;
    this.neuterImages = neuterImages;
  }

  public void setUp() {
    elementSpecificPolicies.put("a", new AElementPolicy());
    elementSpecificPolicies.put("area", new AreaElementPolicy());
    elementSpecificPolicies.put("base", new BaseElementPolicy());
    if (neuterImages) {
      elementSpecificPolicies.put("img", new ImgInputElementPolicy());
      elementSpecificPolicies.put("input", new ImgInputElementPolicy());
    }
    // add any other element policies
  }

  public List<HtmlElement> build() {
    setUp();
    Set<String> allowed = OwaspPolicy.getAllowedElements();
    List<HtmlElement> elements = new ArrayList<>();
    for (String element : allowed) {
      final ElementPolicy policy = elementSpecificPolicies.get(element);
      elements.add(new HtmlElement(element, policy, builder.build(element)));
    }

    return elements;
  }
}
