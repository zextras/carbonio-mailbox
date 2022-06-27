// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.google.common.base.MoreObjects;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.BlobMetaData;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.MapUtil;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.cs.account.auth.AuthMechanism.AuthMech;
import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralInput.AbsoluteExpiration;
import com.zimbra.cs.ephemeral.EphemeralInput.Expiration;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralLocation;
import com.zimbra.cs.ephemeral.EphemeralStore;
import com.zimbra.cs.ephemeral.LdapEntryLocation;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * @since May 30, 2004
 * @author schemers
 */
public class ZimbraAuthToken extends AuthToken implements Cloneable {
  private static final Map<String, ZimbraAuthToken> CACHE =
      MapUtil.newLruMap(LC.zimbra_authtoken_cache_size.intValue());
  private static final Log LOG = LogFactory.getLog(AuthToken.class);
  private AuthTokenProperties properties;

  protected ZimbraAuthToken() {}

  protected ZimbraAuthToken(String encoded) throws AuthTokenException {
    try {
      int pos = encoded.indexOf('_');
      if (pos == -1) {
        throw new AuthTokenException("invalid authtoken format");
      }
      String ver = encoded.substring(0, pos);

      int pos2 = encoded.indexOf('_', pos + 1);
      if (pos2 == -1) {
        throw new AuthTokenException("invalid authtoken format");
      }
      String hmac = encoded.substring(pos + 1, pos2);
      String data = encoded.substring(pos2 + 1);

      AuthTokenKey key = AuthTokenKey.getVersion(ver);
      if (key == null) {
        throw new AuthTokenException("unknown key version");
      }
      String computedHmac = TokenUtil.getHmac(data, key.getKey());
      if (!computedHmac.equals(hmac)) {
        throw new AuthTokenException("hmac failure");
      }
      Map<?, ?> map = TokenUtil.getAttrs(data);
      properties = new AuthTokenProperties(map);
      properties.setEncoded(encoded);
    } catch (ServiceException e) {
      throw new AuthTokenException("service exception", e);
    }
  }

  public ZimbraAuthToken(Account acct) {
    this(acct, false, null);
  }

  public ZimbraAuthToken(Account acct, Usage usage) {
    this(acct, 0, false, null, null, usage);
  }

  public ZimbraAuthToken(Account acct, boolean isAdmin, AuthMech authMech) {
    this(acct, 0, isAdmin, null, authMech);
  }

  public ZimbraAuthToken(Account acct, long expires) {
    this(acct, expires, false, null, null);
  }

  public ZimbraAuthToken(
      Account acct, long expires, boolean isAdmin, Account adminAcct, AuthMech authMech) {
    this(acct, expires, isAdmin, adminAcct, authMech, Usage.AUTH);
  }

  /**
   * @param acct account authtoken will be valid for
   * @param expires when the token expires
   * @param isAdmin true if acct is an admin account
   * @param adminAcct the admin account accessing acct's information, if this token was created by
   *     an admin. mainly used * for auditing.
   * @param authMech authentication mechanism
   * @param usage usage
   */
  public ZimbraAuthToken(
      Account acct,
      long expires,
      boolean isAdmin,
      Account adminAcct,
      AuthMech authMech,
      Usage usage) {
    long expireTime = expires;
    if (expireTime == 0) {
      long lifetime;
      switch (usage) {
        case ENABLE_TWO_FACTOR_AUTH:
          lifetime =
              acct.getTimeInterval(
                  Provisioning.A_zimbraTwoFactorAuthTokenLifetime,
                  DEFAULT_TWO_FACTOR_AUTH_LIFETIME * 1000);
          break;
        case TWO_FACTOR_AUTH:
          lifetime =
              acct.getTimeInterval(
                  Provisioning.A_zimbraTwoFactorAuthEnablementTokenLifetime,
                  DEFAULT_TWO_FACTOR_ENABLEMENT_AUTH_LIFETIME * 1000);
          break;
        case AUTH:
        default:
          lifetime =
              isAdmin
                  ? acct.getTimeInterval(
                      Provisioning.A_zimbraAdminAuthTokenLifetime, DEFAULT_AUTH_LIFETIME * 1000)
                  : acct.getTimeInterval(
                      Provisioning.A_zimbraAuthTokenLifetime, DEFAULT_AUTH_LIFETIME * 1000);
          break;
      }
      expireTime = System.currentTimeMillis() + lifetime;
    }
    properties = new AuthTokenProperties(acct, isAdmin, adminAcct, expireTime, authMech, usage);
    register();
  }

  public ZimbraAuthToken(
      String acctId, String externalEmail, String pass, String digest, long expires) {
    this(acctId, false, externalEmail, pass, digest, expires);
  }

  public ZimbraAuthToken(
      String acctId,
      boolean zmgApp,
      String externalEmail,
      String pass,
      String digest,
      long expires) {
    properties = new AuthTokenProperties(acctId, zmgApp, externalEmail, pass, digest, expires);
  }

  protected static AuthTokenKey getCurrentKey() throws AuthTokenException {
    return AuthTokenUtil.getCurrentKey();
  }

  /**
   * Return an AuthToken object using an encoded authtoken. Caller should call isExpired on returned
   * authToken before using it.
   */
  public static synchronized AuthToken getAuthToken(String encoded) throws AuthTokenException {
    ZimbraAuthToken at = CACHE.get(encoded);
    if (at == null) {
      at = new ZimbraAuthToken(encoded);
      if (!at.isExpired()) {
        CACHE.put(encoded, at);
      }
    } else {
      // remove it if expired
      if (at.isExpired()) {
        CACHE.remove(encoded);
      }
    }
    return at;
  }

  public static Map<?, ?> getInfo(String encoded) throws AuthTokenException {
    String[] parts = encoded.split("_");
    if (parts.length != 3) {
      throw new AuthTokenException("invalid authtoken format");
    }
    return TokenUtil.getAttrs(parts[2]);
  }

  public static void main(String[] args) throws ServiceException, AuthTokenException {
    Account a = Provisioning.getInstance().get(AccountBy.name, "user1@example.zimbra.com");
    ZimbraAuthToken at = new ZimbraAuthToken(a);
    long start = System.currentTimeMillis();
    String encoded = at.getEncoded();
    for (int i = 0; i < 1000; i++) {
      new ZimbraAuthToken(encoded);
    }
    long finish = System.currentTimeMillis();
    System.out.println(finish - start);

    start = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
      getAuthToken(encoded);
    }
    finish = System.currentTimeMillis();
    System.out.println(finish - start);
  }

  public AuthTokenProperties getProperties() {
    return properties;
  }

  public void setProperties(AuthTokenProperties properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("acct", properties.getAccountId())
        .add("admin", properties.getAdminAccountId())
        .add("exp", properties.getExpires())
        .add("isAdm", properties.isAdmin())
        .add("isDlgAd", properties.isDelegatedAdmin())
        .toString();
  }

  @Override
  public String getAccountId() {
    return properties.getAccountId();
  }

  @Override
  public String getAdminAccountId() {
    return properties.getAdminAccountId();
  }

  @Override
  public long getExpires() {
    return properties.getExpires();
  }

  @Override
  public int getValidityValue() {
    return properties.getValidityValue();
  }

  @Override
  public boolean isExpired() {
    return System.currentTimeMillis() > properties.getExpires();
  }

  @Override
  public boolean isAdmin() {
    return properties.isAdmin();
  }

  @Override
  public boolean isDomainAdmin() {
    return properties.isDomainAdmin();
  }

  @Override
  public boolean isDelegatedAdmin() {
    return properties.isDelegatedAdmin();
  }

  @Override
  public boolean isZimbraUser() {
    // C_TYPE_ZMG_APP type indicates the bootstrap auth token issued for ZMG app. Technically
    // that too represents a Zimbra account/user
    return AuthTokenUtil.isZimbraUser(properties.getType());
  }

  @Override
  public String getExternalUserEmail() {
    return properties.getExternalUserEmail();
  }

  @Override
  public String getDigest() {
    return properties.getDigest();
  }

  @Override
  public String getAccessKey() {
    return properties.getAccessKey();
  }

  @Override
  public AuthMech getAuthMech() {
    return properties.getAuthMech();
  }

  private void register() {
    if (!isZimbraUser() || isZMGAppBootstrap()) {
      return;
    }
    try {
      Account acct = Provisioning.getInstance().get(AccountBy.id, properties.getAccountId());
      if (Provisioning.getInstance().getLocalServer().getLowestSupportedAuthVersion() > 1) {
        try {
          acct.cleanExpiredTokens(); // house keeping. If we are issuing a new token, clean up old
          // ones.
        } catch (ServiceException e) {
          LOG.error("unable to de-register auth token", e);
        }
        Expiration expiration = new AbsoluteExpiration(properties.getExpires());
        acct.addAuthTokens(
            String.valueOf(properties.getTokenID()), properties.getServerVersion(), expiration);
      }
    } catch (ServiceException e) {
      LOG.error("unable to register auth token", e);
    }
  }

  // remove the token from LDAP (token will be invalid for cookie-based auth after that
  @Override
  public void deRegister() throws AuthTokenException {
    try {
      Account acct = Provisioning.getInstance().getAccountById(properties.getAccountId());
      if (acct != null) {
        acct.removeAuthTokens(
            String.valueOf(properties.getTokenID()), properties.getServerVersion());
        if (acct.getBooleanAttr(Provisioning.A_zimbraLogOutFromAllServers, false)) {
          AuthTokenRegistry.addTokenToQueue(this);
        }
      }
    } catch (ServiceException e) {
      throw new AuthTokenException("unable to de-register auth token", e);
    }
  }

  @Override
  public String getEncoded() throws AuthTokenException {
    if (properties.getEncoded() == null) {
      StringBuilder encodedBuff = new StringBuilder(64);
      BlobMetaData.encodeMetaData(AuthTokenProperties.C_ID, properties.getAccountId(), encodedBuff);
      BlobMetaData.encodeMetaData(
          AuthTokenProperties.C_EXP, Long.toString(properties.getExpires()), encodedBuff);
      if (properties.getAdminAccountId() != null) {
        BlobMetaData.encodeMetaData(
            AuthTokenProperties.C_AID, properties.getAdminAccountId(), encodedBuff);
      }
      if (properties.isAdmin()) {
        BlobMetaData.encodeMetaData(AuthTokenProperties.C_ADMIN, "1", encodedBuff);
      }
      if (properties.isDomainAdmin()) {
        BlobMetaData.encodeMetaData(AuthTokenProperties.C_DOMAIN, "1", encodedBuff);
      }
      if (properties.isDelegatedAdmin()) {
        BlobMetaData.encodeMetaData(AuthTokenProperties.C_DLGADMIN, "1", encodedBuff);
      }
      if (properties.getValidityValue() != -1) {
        BlobMetaData.encodeMetaData(
            AuthTokenProperties.C_VALIDITY_VALUE, properties.getValidityValue(), encodedBuff);
      }
      BlobMetaData.encodeMetaData(AuthTokenProperties.C_TYPE, properties.getType(), encodedBuff);

      if (properties.getAuthMech() != null) {
        BlobMetaData.encodeMetaData(
            AuthTokenProperties.C_AUTH_MECH, properties.getAuthMech().name(), encodedBuff);
      }

      if (properties.getUsage() != null) {
        BlobMetaData.encodeMetaData(
            AuthTokenProperties.C_USAGE, properties.getUsage().getCode(), encodedBuff);
      }
      BlobMetaData.encodeMetaData(
          AuthTokenProperties.C_TOKEN_ID, properties.getTokenID(), encodedBuff);
      BlobMetaData.encodeMetaData(
          AuthTokenProperties.C_EXTERNAL_USER_EMAIL,
          properties.getExternalUserEmail(),
          encodedBuff);
      BlobMetaData.encodeMetaData(
          AuthTokenProperties.C_DIGEST, properties.getDigest(), encodedBuff);
      BlobMetaData.encodeMetaData(
          AuthTokenProperties.C_SERVER_VERSION, properties.getServerVersion(), encodedBuff);
      if (properties.isCsrfTokenEnabled()) {
        BlobMetaData.encodeMetaData(AuthTokenProperties.C_CSRF, "1", encodedBuff);
      }
      String data = new String(Hex.encodeHex(encodedBuff.toString().getBytes()));
      AuthTokenKey key = getCurrentKey();
      String hmac = TokenUtil.getHmac(data, key.getKey());
      properties.setEncoded(key.getVersion() + "_" + hmac + "_" + data);
    }
    return properties.getEncoded();
  }

  @Override
  public String getCrumb() throws AuthTokenException {
    String authToken = getEncoded();
    try {
      ByteKey bk = new ByteKey(getCurrentKey().getKey());
      Mac mac = Mac.getInstance("HmacMD5");
      mac.init(bk);
      return new String(Hex.encodeHex(mac.doFinal(authToken.getBytes())));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException("fatal error", e);
    }
  }

  @Override
  public boolean isRegistered() {
    if (!isZimbraUser() || isZMGAppBootstrap()) {
      return true;
    }
    try {
      Provisioning prov = Provisioning.getInstance();
      // support older clients if zimbraLowestSupportedAuthVersion is less
      // than 2
      Server localServer = Provisioning.getInstance().getLocalServer();
      if (localServer.getLowestSupportedAuthVersion() < 2) {
        return true;
      }
      Account acct = prov.getAccountById(properties.getAccountId());
      if (acct != null) {
        if (isRegisteredInternal(acct)) {
          return true;
        } else {
          /*
           * If we are here, we did not find the token in the cached
           * account object. Either token has been invalidated or it
           * was generated by another server, therefore reload the
           * object and check one more time.
           */
          prov.reload(acct);
          return isRegisteredInternal(acct);
        }
      }
    } catch (ServiceException e) {
      LOG.fatal("Unable to verify auth token registration in ephemeral store", e);
    }

    return false;
  }

  private boolean isRegisteredInternal(Account acct) throws ServiceException {
    return acct.hasAuthTokens(String.valueOf(properties.getTokenID()));
  }

  @Override
  public void encode(
      HttpClient client, HttpRequestBase method, boolean isAdminReq, String cookieDomain)
      throws ServiceException {
    String origAuthData = AuthTokenUtil.getOrigAuthData(this);

    BasicCookieStore state = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(isAdminReq), origAuthData);
    cookie.setDomain(cookieDomain);
    cookie.setPath("/");
    cookie.setSecure(false);
    state.addCookie(cookie);

    HttpClientBuilder clientBuilder =
        ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();
    clientBuilder.setDefaultCookieStore(state);

    RequestConfig reqConfig =
        RequestConfig.copy(
                ZimbraHttpConnectionManager.getInternalHttpConnMgr()
                    .getZimbraConnMgrParams()
                    .getReqConfig())
            .setCookieSpec(CookieSpecs.DEFAULT)
            .build();

    clientBuilder.setDefaultRequestConfig(reqConfig);
  }

  @Override
  public void encode(
      HttpClientBuilder clientBuilder,
      HttpRequestBase method,
      boolean isAdminReq,
      String cookieDomain)
      throws ServiceException {
    String origAuthData = AuthTokenUtil.getOrigAuthData(this);

    BasicCookieStore state = new BasicCookieStore();
    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(isAdminReq), origAuthData);
    cookie.setDomain(cookieDomain);
    cookie.setPath("/");
    cookie.setSecure(false);
    state.addCookie(cookie);

    clientBuilder.setDefaultCookieStore(state);

    RequestConfig reqConfig =
        RequestConfig.copy(
                ZimbraHttpConnectionManager.getInternalHttpConnMgr()
                    .getZimbraConnMgrParams()
                    .getReqConfig())
            .setCookieSpec(CookieSpecs.DEFAULT)
            .build();

    clientBuilder.setDefaultRequestConfig(reqConfig);
  }

  @Override
  public void encode(BasicCookieStore state, boolean isAdminReq, String cookieDomain)
      throws ServiceException {
    String origAuthData = AuthTokenUtil.getOrigAuthData(this);

    BasicClientCookie cookie =
        new BasicClientCookie(ZimbraCookie.authTokenCookieName(isAdminReq), origAuthData);
    cookie.setDomain(cookieDomain);
    cookie.setPath("/");
    cookie.setSecure(false);
    state.addCookie(cookie);
  }

  @Override
  public void encode(
      HttpServletResponse resp, boolean isAdminReq, boolean secureCookie, boolean rememberMe)
      throws ServiceException {
    String origAuthData = AuthTokenUtil.getOrigAuthData(this);

    int maxAge;
    if (rememberMe) {
      long timeLeft = properties.getExpires() - System.currentTimeMillis();
      maxAge = (int) (timeLeft / 1000);
    } else {
      maxAge = -1;
    }

    ZimbraCookie.addHttpOnlyCookie(
        resp,
        ZimbraCookie.authTokenCookieName(isAdminReq),
        origAuthData,
        ZimbraCookie.PATH_ROOT,
        maxAge,
        secureCookie);
  }

  @Override
  public void encodeAuthResp(Element parent, boolean isAdmin) throws ServiceException {
    AuthTokenUtil.encodeAuthResp(this, parent, isAdmin);
  }

  @Override
  public ZAuthToken toZAuthToken() throws ServiceException {
    return new ZAuthToken(AuthTokenUtil.getOrigAuthData(this), properties.getProxyAuthToken());
  }

  @Override
  public String getProxyAuthToken() {
    return properties.getProxyAuthToken();
  }

  @Override
  public void setProxyAuthToken(String encoded) {
    properties.setProxyAuthToken(encoded);
  }

  @Override
  public void resetProxyAuthToken() {
    properties.setProxyAuthToken(null);
  }

  /* (non-Javadoc)
   * @see com.zimbra.cs.account.AuthToken#isCsrfTokenEnabled()
   */
  @Override
  public boolean isCsrfTokenEnabled() {
    return properties.isCsrfTokenEnabled();
  }

  @Override
  public void setCsrfTokenEnabled(boolean csrfEnabled) {
    if (csrfEnabled != properties.isCsrfTokenEnabled()) {
      synchronized (ZimbraAuthToken.class) {
        if (properties.getEncoded() != null) {
          CACHE.remove(properties.getEncoded());
        }
      }
      properties.setCsrfTokenEnabled(csrfEnabled);
      // force re-encoding of the token
      properties.setEncoded(null);
    }
  }

  @Override
  public Usage getUsage() {
    return properties.getUsage();
  }

  /**
   * This method is used to reset tokenID. One use case is when new authtoken is created by cloning
   * existing authtoken, tokenID of newly authtoken is reset to make it unique and all the other
   * properties are same as of authtoken from which it is cloned. Cached encoded string is also
   * reset as the due to change in TokenID.
   */
  public void resetTokenId() {
    properties.setTokenID(new Random().nextInt(Integer.MAX_VALUE - 1) + 1);
    properties.setEncoded(null);
    this.register();
  }

  @Override
  public boolean isZMGAppBootstrap() {
    return AuthTokenProperties.C_TYPE_ZMG_APP.equals(properties.getType());
  }

  @Override
  public ZimbraAuthToken clone() throws CloneNotSupportedException {
    ZimbraAuthToken at = (ZimbraAuthToken) super.clone();
    at.setProperties(properties.clone());
    return at;
  }

  /*
   * Used when the auth token needs to be registered with a non-default
   * ephemeral backend
   */
  public void registerWithEphemeralStore(EphemeralStore store) throws ServiceException {
    Account acct = Provisioning.getInstance().get(AccountBy.id, properties.getAccountId());
    Expiration expiration = new AbsoluteExpiration(properties.getExpires());
    EphemeralLocation location = new LdapEntryLocation(acct);
    EphemeralKey key =
        new EphemeralKey(Provisioning.A_zimbraAuthTokens, String.valueOf(properties.getTokenID()));
    EphemeralInput input = new EphemeralInput(key, properties.getServerVersion(), expiration);
    store.update(input, location);
  }

  static class ByteKey implements SecretKey {
    private static final long serialVersionUID = -7237091299729195624L;
    private final byte[] mKey;

    ByteKey(byte[] key) {
      mKey = key.clone();
    }

    @Override
    public byte[] getEncoded() {
      return mKey;
    }

    @Override
    public String getAlgorithm() {
      return "HmacSHA1";
    }

    @Override
    public String getFormat() {
      return "RAW";
    }
  }
}
