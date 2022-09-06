// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp;

import java.util.List;
import java.util.Set;
import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/*
 * Instantiate owasp policy instance with neuter images true/false at load time
 */
public class OwaspPolicyProducer {

  private static PolicyFactory policyNeuterImagesTrue;
  private static PolicyFactory policyNeuterImagesFalse;

  private static void setUp(boolean neuterImages) {
    HtmlElementsBuilder builder =
        new HtmlElementsBuilder(new HtmlAttributesBuilder(), neuterImages);
    List<HtmlElement> allowedElements = builder.build();
    HtmlPolicyBuilder policyBuilder = new HtmlPolicyBuilder();
    policyBuilder.requireRelNofollowOnLinks();
    for (HtmlElement htmlElement : allowedElements) {
      htmlElement.configure(policyBuilder, neuterImages);
    }
    Set<String> disallowTextElements = OwaspPolicy.getDisallowTextElements();
    for (String disAllowTextElement : disallowTextElements) {
      policyBuilder.disallowTextIn(disAllowTextElement.trim());
    }
    Set<String> allowTextElements = OwaspPolicy.getAllowTextElements();
    for (String allowTextElement : allowTextElements) {
      policyBuilder.allowTextIn(allowTextElement.trim());
    }
    /**
     * The following CSS properties do not appear in the default whitelist from OWASP, but they
     * improve the fidelity of the HTML display without unacceptable risk.
     */
    Set<String> cssWhitelist = OwaspPolicy.getCssWhitelist();
    CssSchema ADDITIONAL_CSS = null;
    if (!cssWhitelist.isEmpty()) {
      ADDITIONAL_CSS = CssSchema.withProperties(cssWhitelist);
    }
    Set<String> urlProtocols = OwaspPolicy.getURLProtocols();
    for (String urlProtocol : urlProtocols) {
      policyBuilder.allowUrlProtocols(urlProtocol.trim());
    }
    if (neuterImages) {
      if (policyNeuterImagesTrue == null) {
        policyNeuterImagesTrue =
            policyBuilder
                .allowStyling(
                    ADDITIONAL_CSS == null
                        ? CssSchema.DEFAULT
                        : CssSchema.union(CssSchema.DEFAULT, ADDITIONAL_CSS))
                .toFactory();
      }
    } else {
      if (policyNeuterImagesFalse == null) {
        policyNeuterImagesFalse =
            policyBuilder
                .allowStyling(
                    ADDITIONAL_CSS == null
                        ? CssSchema.DEFAULT
                        : CssSchema.union(CssSchema.DEFAULT, ADDITIONAL_CSS))
                .toFactory();
      }
    }
  }

  public static PolicyFactory getPolicyFactoryInstance(boolean neuterImages) {
    if (neuterImages) {
      return policyNeuterImagesTrue;
    } else {
      return policyNeuterImagesFalse;
    }
  }

  static {
    setUp(true); // setup policy for neuter image true
    setUp(false); // setup policy for neuter image false
  }
}
