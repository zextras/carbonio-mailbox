package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;

/**
 * Class representing a {@link ProxyConfGen} variable, Instance of this class overrides update
 * method and provide value based on the {@link ProxyConfVar#serverSource} attribute
 * <p>
 * The {@link ProxyConfVar#mValue} of this variable defines definition of proxy http compression
 * directives namely: GZip {@link ProxyCompressionServerVar#G_ZIP_COMPRESSION_DIRECTIVE} and Brotli
 * {@link ProxyCompressionServerVar#BROTLI_COMPRESSION_DIRECTIVE}.
 * <p>
 * Definition are some sane defaults hence hardcoded for simplicity.
 * <p>
 * The expansion of this variable depends on {@code ZAttrProvisioning.A_zimbraHttpCompressionEnabled}
 * server and global config variable
 *
 * @author Keshav Bhatt
 * @since 23.4.0
 */
@SuppressWarnings("StringBufferReplaceableByString")
public class ProxyCompressionServerVar extends ProxyConfVar {

  static final String DIRECTIVE_SEPARATOR = "    ";
  static final String TYPE_DIRECTIVE_SEPARATOR = DIRECTIVE_SEPARATOR + DIRECTIVE_SEPARATOR;

  private static final String TYPES = new StringBuilder()
      .append(String.format("%sapplication/atom+xml%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/geo+json%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/javascript%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/x-javascript%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/json%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/ld+json%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/manifest+json%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/rdf+xml%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/rss+xml%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/xhtml+xml%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sapplication/xml%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sfont/eot%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sfont/otf%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sfont/ttf%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%sfont/woff2%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%simage/svg+xml%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%stext/css%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%stext/javascript%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%stext/plain%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%stext/xml;%n", TYPE_DIRECTIVE_SEPARATOR))
      .append(String.format("%n"))
      .toString();

  private static final String G_ZIP_COMPRESSION_DIRECTIVE = new StringBuilder()
      .append(String.format("%n"))
      .append(String.format("%sgzip on;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sgzip_disable \"msie6\";%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sgzip_vary on;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sgzip_proxied any;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sgzip_comp_level 6;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sgzip_buffers 16 8k;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sgzip_http_version 1.1;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sgzip_min_length 256;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sgzip_types%n", DIRECTIVE_SEPARATOR))
      .append(TYPES)
      .toString();

  private static final String BROTLI_COMPRESSION_DIRECTIVE = new StringBuilder()
      .append(String.format("%n"))
      .append(String.format("%sbrotli on;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sbrotli_static on;%n", DIRECTIVE_SEPARATOR))
      .append(String.format("%sbrotli_types%n", DIRECTIVE_SEPARATOR))
      .append(TYPES)
      .toString();

  protected ProxyCompressionServerVar() {
    super(
        "proxy.http.compression",
        null,
        "",
        ProxyConfValueType.STRING,
        ProxyConfOverride.SERVER,
        "Proxy HTTP compression directives definition");
  }

  @Override
  public void update() throws ServiceException {

    final boolean httpCompressionEnabled = serverSource.getBooleanAttr(
        ZAttrProvisioning.A_zimbraHttpCompressionEnabled, true);

    mValue =
        httpCompressionEnabled ? G_ZIP_COMPRESSION_DIRECTIVE + BROTLI_COMPRESSION_DIRECTIVE : "";
  }
}
