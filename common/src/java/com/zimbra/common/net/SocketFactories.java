// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.net.ProxySelector;
import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;

/*
 * Factory class for various SocketFactory types.
 */
public final class SocketFactories {

  private static final NetConfig config = NetConfig.getInstance();
  private static final String HTTPS = "https";
  private static final String HTTP = "http";
  private static boolean registered;
  private static Registry<ConnectionSocketFactory> registry;

  private SocketFactories() {
    throw new java.lang.UnsupportedOperationException("Utility class and cannot be instantiated");
  }

  /**
   * Registers default HTTP and HTTPS protocol handlers for ZCS using CustomSSLSocketFactory. If
   * local config attribute "socks_enabled" is true then optional SOCKS proxy support will be
   * enabled. If attribute "ssl_allow_untrusted_certs" is true then a dummy SSLSocketFactory will be
   * configured that trusts all certificates.
   */
  public static void registerProtocolsServer() {
    register(
        config.isAllowUntrustedCerts()
            ? TrustManagers.dummyTrustManager()
            : TrustManagers.customTrustManager());
  }

  /**
   * Registers default HTTP and HTTPS protocol handlers for CLI tools and standalone unit tests
   * using the system default SSLSocketFactory. SOCKS proxy support is not enabled. If attribute
   * "ssl_allow_untrusted_certs" is true then a dummy SSLSocketFactory will be configured that
   * trusts all certificates.
   */
  public static void registerProtocols() {
    registerProtocols(config.isAllowUntrustedCerts());
  }

  /**
   * Registers default HTTP and HTTPS protocol handlers for CLI tools and standalone unit tests
   * using the system default SSLSocketFactory. SOCKS proxy support is not enabled. If
   * allowUntrustedCerts is true then a dummy SSLSocketFactory will be configured that trusts all
   * certificates.
   */
  public static void registerProtocols(boolean allowUntrustedCerts) {
    register(allowUntrustedCerts ? TrustManagers.dummyTrustManager() : null);
  }

  private static synchronized void register(X509TrustManager tm) {
    if (registered) return;

    // Set default TrustManager
    TrustManagers.setDefaultTrustManager(tm);

    // Register Apache Commons HTTP/HTTPS protocol socket factories
    ConnectionSocketFactory psf = defaultProtocolSocketFactory();
    LayeredConnectionSocketFactory spsf = defaultSecureProtocolSocketFactory();
    registry =
        RegistryBuilder.<ConnectionSocketFactory>create()
            .register(HTTP, psf)
            .register(HTTPS, spsf)
            .build();

    // HttpURLConnection already uses system ProxySelector by default
    // Set HttpsURLConnection SSL socket factory and optional hostname verifier
    HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory(false));
    if (tm instanceof CustomTrustManager) {
      HttpsURLConnection.setDefaultHostnameVerifier(new CustomHostnameVerifier());
    }

    // Set the system-wide default ProxySelector
    ProxySelector.setDefault(ProxySelectors.defaultProxySelector());

    registered = true;
  }

  public static synchronized void resetRegisteredFlag() {
    registered = false;
  }

  /**
   * Returns a ProtocolSocketFactory that wraps the specified SocketFactory.
   *
   * @param sf the SocketFactory
   * @return a ProtocolSocketFactory wrapping the SocketFactory
   */
  public static ConnectionSocketFactory protocolSocketFactory(SocketFactory sf) {
    return new ProtocolSocketFactoryWrapper(sf);
  }

  /**
   * Returns a ProtocolSocketFactory that wraps the default SocketFactory.
   *
   * @return ProtocolSocketFactory wrapping the default SocketFactory
   */
  public static ConnectionSocketFactory defaultProtocolSocketFactory() {
    return protocolSocketFactory(defaultSocketFactory());
  }

  /**
   * Returns a SecureProtocolSocketFactory that wraps the specified SSLSocketFactory.
   *
   * @param ssf the SSLSocketFactory
   * @return a SecureProtocolSocketFactory wrapping the SSLSocketFactory
   */
  public static LayeredConnectionSocketFactory secureProtocolSocketFactory(SSLSocketFactory ssf) {
    return new SecureProtocolSocketFactoryWrapper(ssf);
  }

  /**
   * Returns a SecureProtocolSocketFactory that wraps the default SSLSocketFactory.
   *
   * @return a SecureProtocolSocketFactory wrapping the default SSLSocketFactory
   */
  public static LayeredConnectionSocketFactory defaultSecureProtocolSocketFactory() {
    return secureProtocolSocketFactory(defaultSSLSocketFactory());
  }

  /**
   * Returns a SecureProtocolSocketFactory that trusts all certificates.
   *
   * @return a SecureProtocolSocketFactory trusting all certificates
   */
  public static LayeredConnectionSocketFactory dummySecureProtocolSocketFactory() {
    return secureProtocolSocketFactory(dummySSLSocketFactory());
  }

  /**
   * Returns a SocketFactory that uses the specified ProxySelector for new connections.
   *
   * @param ps the ProxySelector to use
   * @return the SocketFactory using the ProtocolSelector
   */
  public static SocketFactory proxySelectorSocketFactory(ProxySelector ps) {
    return new ProxySelectorSocketFactory(ps);
  }

  /**
   * Returns a SocketFactory that uses the default ProxySelector for new connections.
   *
   * @return the SocketFactory using the default ProxySelector
   */
  public static SocketFactory proxySelectorSocketFactory() {
    return new ProxySelectorSocketFactory();
  }

  /**
   * Returns the default SSLSocketFactory based the default TrustManager (see
   * TrustManagers.defaultTrustManager).
   *
   * @return the default SSLSocketFactory
   */
  public static SSLSocketFactory defaultSSLSocketFactory() {
    return defaultSSLSocketFactory(config.isAllowMismatchedCerts());
  }

  private static SSLSocketFactory defaultSSLSocketFactory(boolean verifyHostname) {
    return defaultSSLSocketFactory(TrustManagers.defaultTrustManager(), verifyHostname);
  }

  private static SSLSocketFactory defaultSSLSocketFactory(TrustManager tm, boolean verifyHostname) {
    SocketFactory sf = config.isSocksEnabled() ? defaultSocketFactory() : null;
    try {
      return new CustomSSLSocketFactory(tm, sf, verifyHostname);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to create CustomSSLSocketFactory", e);
    }
  }

  /**
   * Returns the default SocketFactory using SOCKS if configured.
   *
   * @return the default SocketFactoryh
   */
  public static SocketFactory defaultSocketFactory() {
    return config.isSocksEnabled() ? proxySelectorSocketFactory() : SocketFactory.getDefault();
  }

  // Factory methods used specifically for testing

  /**
   * Returns an SSLSocketFactory that trusts all certificates.
   *
   * @return an SSLSocketFactory that trusts all certificates
   */
  public static SSLSocketFactory dummySSLSocketFactory() {
    return defaultSSLSocketFactory(TrustManagers.dummyTrustManager(), true);
  }

  public static Registry<ConnectionSocketFactory> getRegistry() {
    return registry;
  }
}
