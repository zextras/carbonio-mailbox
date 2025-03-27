
package org.owasp.html;

import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.List;
import org.owasp.html.HtmlStreamEventProcessor.Processors;

public final class HtmlSanitizer {
  public HtmlSanitizer() {
  }

  public static void sanitize(String html, Policy policy) {
    sanitize(html, policy, Processors.IDENTITY);
  }

  public static void sanitize(String html, Policy policy, HtmlStreamEventProcessor preprocessor) {
    String htmlContent = html != null ? html : "";
    HtmlStreamEventReceiver receiver = initializePolicy(policy, preprocessor);
    receiver.openDocument();
    HtmlLexer lexer = new HtmlLexer(htmlContent);
    LinkedList<String> attrs = Lists.newLinkedList();

    while(true) {
      while(true) {
        while(lexer.hasNext()) {
          HtmlToken token = lexer.next();
          switch (token.type) {
            case TEXT:
              receiver.text(Encoding.decodeHtml(htmlContent.substring(token.start, token.end)));
              break;
            case UNESCAPED:
              receiver.text(Encoding.stripBannedCodeunits(htmlContent.substring(token.start, token.end)));
              break;
            case TAGBEGIN:
              if (htmlContent.charAt(token.start + 1) == '/') {
                receiver.closeTag(HtmlLexer.canonicalElementName(htmlContent.substring(token.start + 2, token.end)));

                while(lexer.hasNext() && lexer.next().type != HtmlTokenType.TAGEND) {
                }
              } else {
                attrs.clear();
                boolean attrsReadyForName = true;

                label59:
                while(lexer.hasNext()) {
                  HtmlToken tagBodyToken = lexer.next();
                  switch (tagBodyToken.type) {
                    case ATTRNAME:
                      if (!attrsReadyForName) {
                        attrs.add(attrs.getLast());
                      } else {
                        attrsReadyForName = false;
                      }

                      attrs.add(HtmlLexer.canonicalAttributeName(htmlContent.substring(tagBodyToken.start, tagBodyToken.end)));
                      break;
                    case ATTRVALUE:
                      attrs.add(Encoding.decodeHtml(stripQuotes(htmlContent.substring(tagBodyToken.start, tagBodyToken.end))));
                      attrsReadyForName = true;
                      break;
                    case TAGEND:
                      break label59;
                  }
                }

                if (!attrsReadyForName) {
                  attrs.add(attrs.getLast());
                }

                receiver.openTag(HtmlLexer.canonicalElementName(htmlContent.substring(token.start + 1, token.end)), attrs);
              }
          }
        }

        receiver.closeDocument();
        return;
      }
    }
  }

  private static String stripQuotes(String encodedAttributeValue) {
    int n = encodedAttributeValue.length();
    if (n > 0) {
      char last = encodedAttributeValue.charAt(n - 1);
      if (last == '"' || last == '\'') {
        int start = 0;
        if (n != 1 && last == encodedAttributeValue.charAt(0)) {
          start = 1;
        }

        return encodedAttributeValue.substring(start, n - 1);
      }
    }

    return encodedAttributeValue;
  }

  private static HtmlStreamEventReceiver initializePolicy(Policy policy, HtmlStreamEventProcessor preprocessor) {
    TagBalancingHtmlStreamEventReceiver balancer = new TagBalancingHtmlStreamEventReceiver(policy);
    balancer.setNestingLimit(512);
    return preprocessor.wrap(balancer);
  }

  @TCB
  public interface Policy extends HtmlStreamEventReceiver {
    void openTag(String var1, List<String> var2);

    void closeTag(String var1);

    void text(String var1);
  }
}
