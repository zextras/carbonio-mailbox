/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.nginx;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.common.account.ZAttrProvisioning.IPMode;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.CacheExtension;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.account.auth.AuthMechanism;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.extension.ExtensionUtil;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.cs.imap.ImapLoadBalancingMechanism;
import com.zimbra.cs.ldap.ILdapContext;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;
import com.zimbra.cs.nginx.AbstractNginxLookupLdapHelper.SearchDirResult;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.authenticator.ClientCertAuthenticator;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

public class NginxLookupExtension implements ZimbraExtension {

    public static final String NAME = "nginx-lookup";
    private static SecureRandom random = new SecureRandom();

    private static NginxLookupCache<DomainInfo> sDomainNameByVirtualIpCache =
        new NginxLookupCache<DomainInfo>(
                LC.ldap_cache_reverseproxylookup_domain_maxsize.intValue(),
                LC.ldap_cache_reverseproxylookup_domain_maxage.intValue() * Constants.MILLIS_PER_MINUTE);

    private static NginxLookupCache<DomainExternalRouteInfo> sDomainExternalRouteByDomainNameCache =
        new NginxLookupCache<DomainExternalRouteInfo>(
                LC.ldap_cache_reverseproxylookup_domain_maxsize.intValue(),
                LC.ldap_cache_reverseproxylookup_domain_maxage.intValue() * Constants.MILLIS_PER_MINUTE);

    private static NginxLookupCache<ServerInfo> sServerCache =
        new NginxLookupCache<ServerInfo>(
                LC.ldap_cache_reverseproxylookup_server_maxsize.intValue(),
                LC.ldap_cache_reverseproxylookup_server_maxage.intValue() * Constants.MILLIS_PER_MINUTE);

    private static final Map<String, Boolean> REV_PROXY_MAILHOST_ATTRS = new HashMap<>();
    private static final Set<String> REV_PROXY_MAILHOST_EXTRA_ATTRS = new HashSet<>();
    static {
                REV_PROXY_MAILHOST_ATTRS.put(Provisioning.A_zimbraReverseProxyMailHostAttribute, true);
                REV_PROXY_MAILHOST_ATTRS.put(Provisioning.A_zimbraReverseProxyUserNameAttribute, false);

                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraReverseProxyUseExternalRoute);
                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraExternalPop3Port);
                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraExternalPop3SSLPort);
                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraExternalImapPort);
                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraExternalImapSSLPort);
                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraExternalPop3Hostname);
                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraExternalPop3SSLHostname);
                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraExternalImapHostname);
                REV_PROXY_MAILHOST_EXTRA_ATTRS.add(Provisioning.A_zimbraExternalImapSSLHostname);
                ExtensionUtil.initAllMatching(new NginxLookupExtensionMatcher());
    }
    public static class NginxLookupExtensionMatcher implements ExtensionUtil.ExtensionMatcher {
        @Override
        public boolean matches(ZimbraExtension ext) {
            return true;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init() throws ExtensionException, ServiceException {
        ExtensionDispatcherServlet.register(this, new NginxLookupHandler());
        CacheExtension.register("reverseproxylookup", new ReverseProxyCache());
    }

    @Override
    public void destroy() {
        ExtensionDispatcherServlet.unregister(this);
    }

    @SuppressWarnings("serial")
    public static class NginxLookupException extends Exception {
        public NginxLookupException(String msg) {
            super(msg);
        }

        public NginxLookupException(Throwable cause) {
            super(cause);
            ZimbraLog.nginxlookup.debug("", cause);
        }

        public NginxLookupException(String msg, Throwable cause) {
            super(msg, cause);
            ZimbraLog.nginxlookup.debug("", cause);
        }
    }

    public static class EntryNotFoundException extends NginxLookupException {
        public EntryNotFoundException(String msg) {
            super(msg);
        }
    }

    static class ReverseProxyCache extends CacheExtension {

        @Override
        public void flushCache() throws ServiceException {
            sDomainNameByVirtualIpCache.clear();
            sDomainExternalRouteByDomainNameCache.clear();
            sServerCache.clear();
        }
    }

    public enum NginxLookupRequestType
    {
        web,
        admin,
        zx
    }

    public static class NginxLookupRequest {
        protected String user;
        protected String cuser;
        protected String pass;
        protected String proto;
        protected String authMethod;
        protected String clientIp;
        protected String serverIp;
        protected String serverHost;
        protected String principal;
        protected int loginAttempt;
        protected NginxLookupRequestType type;
        protected String adminUser;
        protected String adminPass;
        protected HttpServletRequest  httpReq;
        protected HttpServletResponse httpResp;
    }

    public static class NginxLookupHandler extends ExtensionHttpHandler {
        /* req headers */
        public static final String AUTH_METHOD        = "Auth-Method";
        public static final String AUTH_USER          = "Auth-User";
        public static final String AUTH_PASS          = "Auth-Pass";
        public static final String AUTH_PROTOCOL      = "Auth-Protocol";
        public static final String AUTH_ZIMBRA_ADMIN  = "Auth-Zimbra-Admin";
        public static final String AUTH_ZIMBRA_ZX     = "Auth-Zimbra-Zx";
        public static final String AUTH_LOGIN_ATTEMPT = "Auth-Login-Attempt";
        public static final String CLIENT_IP          = "Client-IP";
        public static final String SERVER_IP          = "X-Proxy-IP";
        public static final String SERVER_HOST        = "X-Proxy-Host";
        public static final String AUTH_ID            = "Auth-Id";
        public static final String AUTH_ADMIN_USER    = "Auth-Admin-User";
        public static final String AUTH_ADMIN_PASS    = "Auth-Admin-Pass";

        /* resp headers */
        public static final String AUTH_STATUS      = "Auth-Status";
        public static final String AUTH_SERVER      = "Auth-Server";
        public static final String AUTH_PORT        = "Auth-Port";
        public static final String AUTH_WAIT        = "Auth-Wait";
        public static final String AUTH_CACHE_ALIAS = "Auth-Cache-Alias";

        public static final long DEFAULT_WAIT_INTERVAL = 10;

        /* Generic Error Message for failure */
        public static final String ERRMSG = "login failed";
        public static final String ACCESS_DENIED_ERRMSG = "is not allowed on this domain";

        /* protocols */
        public static final String POP3     = "pop3";
        public static final String POP3_SSL = "pop3ssl";
        public static final String IMAP     = "imap";
        public static final String IMAP_SSL = "imapssl";
        public static final String HTTP     = "http";
        public static final String HTTP_SSL = "httpssl";

        /* auth methods */
        public static final String AUTHMETH_PLAIN = "plain";
        public static final String AUTHMETH_OTHER = "other";
        public static final String AUTHMETH_ZIMBRAID = "zimbraId";
        public static final String AUTHMETH_GSSAPI = "gssapi";
        public static final String AUTHMETH_CERTAUTH = "certauth";

        private LdapProv prov;
        private AbstractNginxLookupLdapHelper helper;

        @Override
        public boolean hideFromDefaultPorts() {
            return true;
        }

        public NginxLookupHandler() throws ExtensionException {
            try {
                prov = LdapProv.getInst();
                helper = new NginxLookupLdapHelper(prov);
            } catch (ServiceException e) {
                throw new ExtensionException("unable to initialize nginx lookup servlet", e);
            }
        }

        @Override
        public void init(ZimbraExtension ext) throws ServiceException {
            super.init(ext);
        }

        private String[] getUserSC(Config config) {
            String attr;
            ArrayList<String> attrs = new ArrayList<String>();

            attr = config.getAttr(Provisioning.A_zimbraReverseProxyMailHostAttribute);
            if (attr != null)
                attrs.add(attr);
            attr = config.getAttr(Provisioning.A_zimbraReverseProxyUserNameAttribute);
            if (attr != null)
                attrs.add(attr);
            attrs.add(Provisioning.A_zimbraReverseProxyUseExternalRoute);
            attrs.add(Provisioning.A_zimbraExternalPop3Port);
            attrs.add(Provisioning.A_zimbraExternalPop3SSLPort);
            attrs.add(Provisioning.A_zimbraExternalImapPort);
            attrs.add(Provisioning.A_zimbraExternalImapSSLPort);
            attrs.add(Provisioning.A_zimbraExternalPop3Hostname);
            attrs.add(Provisioning.A_zimbraExternalPop3SSLHostname);
            attrs.add(Provisioning.A_zimbraExternalImapHostname);
            attrs.add(Provisioning.A_zimbraExternalImapSSLHostname);

            return attrs.toArray(new String[attrs.size()]);
        }

        private String[] getServerSC(Config config) {
            String attr;
            ArrayList<String> attrs = new ArrayList<String>();

            attr = config.getAttr(Provisioning.A_zimbraReverseProxyPop3PortAttribute);
            if (attr != null)
                attrs.add(attr);
            attr = config.getAttr(Provisioning.A_zimbraReverseProxyPop3SSLPortAttribute);
            if (attr != null)
                attrs.add(attr);
            attr = config.getAttr(Provisioning.A_zimbraReverseProxyImapPortAttribute);
            if (attr != null)
                attrs.add(attr);
            attr = config.getAttr(Provisioning.A_zimbraReverseProxyImapSSLPortAttribute);
            if (attr != null)
                attrs.add(attr);
            attr = config.getAttr(Provisioning.A_zimbraReverseProxyHttpPortAttribute);
            if (attr != null)
                attrs.add(attr);
            attr = config.getAttr(Provisioning.A_zimbraReverseProxyHttpSSLPortAttribute);
            if (attr != null)
                attrs.add(attr);

            return attrs.toArray(new String[attrs.size()]);
        }

        private String[] getDomainSC(Config config) {
            String attr;
            ArrayList<String> attrs = new ArrayList<String>();

            attr = config.getAttr(Provisioning.A_zimbraReverseProxyDomainNameAttribute);
            if (attr != null)
                attrs.add(attr);

            attrs.add(Provisioning.A_zimbraReverseProxyUseExternalRoute);
            attrs.add(Provisioning.A_zimbraReverseProxyUseExternalRouteIfAccountNotExist);
            attrs.add(Provisioning.A_zimbraReverseProxyExternalRouteIncludeOriginalAuthusername);
            attrs.add(Provisioning.A_zimbraExternalPop3Port);
            attrs.add(Provisioning.A_zimbraExternalPop3SSLPort);
            attrs.add(Provisioning.A_zimbraExternalImapPort);
            attrs.add(Provisioning.A_zimbraExternalImapSSLPort);
            attrs.add(Provisioning.A_zimbraExternalPop3Hostname);
            attrs.add(Provisioning.A_zimbraExternalPop3SSLHostname);
            attrs.add(Provisioning.A_zimbraExternalImapHostname);
            attrs.add(Provisioning.A_zimbraExternalImapSSLHostname);

            return attrs.toArray(new String[attrs.size()]);
        }


        @Override
        public void doGet(HttpServletRequest httpReq, HttpServletResponse resp) throws IOException, ServletException {
            try {
                NginxLookupRequest req = checkRequest(httpReq);
                req.httpReq  = httpReq;
                req.httpResp = resp;
                search(req);
            } catch (NginxLookupException ex) {
                sendError(resp, ex.getMessage());
            }
        }

        /**
         * Parse the HTTP request headers and construct the NginxLookupRequest object
         * @param httpReq The HTTP Servlet Request object
         * @return    NginxLookupRequest object containing details of the lookup request
         * @throws NginxLookupException
         */
        private NginxLookupRequest checkRequest(HttpServletRequest httpReq) throws NginxLookupException
        {
            /* Build the request object and extract the various request headers */

            NginxLookupRequest req = new NginxLookupRequest();

            /* NGINX will never pass any suffixes to the lookup servlet
               So no need to look for /tb|/wm|/ni in req.user
             */
            try { //bug 51672, username and password need unescape
                req.user     = unescapeAuthUserAndPass(httpReq.getHeader(AUTH_USER)); /* User whose route is to be looked up */
                req.pass     = unescapeAuthUserAndPass(httpReq.getHeader(AUTH_PASS)); /* Password */
            } catch (IllegalArgumentException e) {
                throw new NginxLookupException(e);
            }
            req.proto           = httpReq.getHeader(AUTH_PROTOCOL);         /* Protocol {imap|imaps|pop3|pop3s|http} */
            req.authMethod      = httpReq.getHeader(AUTH_METHOD);           /* Auth Method {passwd|plain|gssapi|other|zimbraId} */
            req.cuser           = httpReq.getHeader(AUTH_ID);               /* (GSSAPI) Authenticating Principal */
            req.adminUser       = httpReq.getHeader(AUTH_ADMIN_USER);       /* auth admin user, required for GSSAPI */
            req.adminPass       = httpReq.getHeader(AUTH_ADMIN_PASS);       /* auth admin password, , required for GSSAPI */
            req.clientIp        = httpReq.getHeader(CLIENT_IP);             /* Upstream Client IP */
            req.serverIp        = httpReq.getHeader(SERVER_IP);             /* Incoming Proxy Interface IP */
            req.serverHost      = httpReq.getHeader(SERVER_HOST);           /* (HTTP) Host header */
            req.loginAttempt    = 1;
            req.type            = NginxLookupRequestType.web;


            /* Complain if any required fields are missing */

            if (req.user == null)
                throw new NginxLookupException("missing header field " + AUTH_USER);
            if (req.authMethod == null)
                throw new NginxLookupException("missing header field " + AUTH_METHOD);
            if (req.proto == null)
                throw new NginxLookupException("missing header field " + AUTH_PROTOCOL);
            if (req.authMethod.equalsIgnoreCase(AUTHMETH_GSSAPI)) {
                if (req.cuser == null)
                    throw new NginxLookupException("(GSSAPI) missing header field " + AUTH_ID);

                if (req.adminUser == null)
                    throw new NginxLookupException("(GSSAPI) missing header field " + AUTH_ADMIN_USER);

                if (req.adminPass == null)
                    throw new NginxLookupException("(GSSAPI) missing header field " + AUTH_ADMIN_PASS);

                if (req.serverIp == null)
                    throw new NginxLookupException("(GSSAPI) missing header field " + SERVER_IP);
            } else if (req.authMethod.equalsIgnoreCase(AUTHMETH_CERTAUTH)) {
                if (req.adminUser == null)
                    throw new NginxLookupException("(CERTAUTH) missing header field " + AUTH_ADMIN_USER);

                if (req.adminPass == null)
                    throw new NginxLookupException("(CERTAUTH) missing header field " + AUTH_ADMIN_PASS);

                if (req.serverIp == null)
                    throw new NginxLookupException("(CERTAUTH) missing header field " + SERVER_IP);

                req.user = unifyDNFormat(req.user);
            }

            if (req.pass == null)   /* We should not complain on null password */
                req.pass = "";

            String val = httpReq.getHeader(AUTH_LOGIN_ATTEMPT);
            if (val != null) {
                try {
                    req.loginAttempt = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                }
            }

            String isZimbraZx = httpReq.getHeader(AUTH_ZIMBRA_ZX);
            if (isZimbraZx != null) {
                if( Boolean.parseBoolean (isZimbraZx) ) {
                    req.type = NginxLookupRequestType.zx;
                }
            }

            String isZimbraAdmin = httpReq.getHeader(AUTH_ZIMBRA_ADMIN);
            if (isZimbraAdmin != null) {
                if( Boolean.parseBoolean (isZimbraAdmin) ) {
                    req.type = NginxLookupRequestType.admin;
                }
            }

            return req;
        }

        /**
         * The DN returned by nginx looks like:
         * /C=US/ST=California/L=Saratoga/O=Zimbra/OU=Engineering/CN=user one/emailAddress=user1@u10
         * This method changes the separator to ",", trim the first "/" and make
         * "emailAddress" to "EMAILADDRESS"
         */
        private static String unifyDNFormat(String origDn) {
            String dn = origDn;
            if (dn.startsWith("/")) {
                dn = dn.substring(1); //trim the first "/"
            }

            dn = dn.replace("/", ",");
            dn = dn.replace("emailAddress", "EMAILADDRESS");
            return dn;
        }

        /**
         * Unescape all the '%xy' combinations in <code>src</code> to their
         * normal form, where 'xy' must be a valid hex value.
         * @param src the string to be unescaped
         * @return the escape result
         * @throws IllegalArgumentException throw when trailing escape (%)
         *         pattern is incomplete
         */
        private static String unescapeAuthUserAndPass(String src) {
            if (src == null) {
                return null;
            }

            int len = src.length();
            StringBuffer sb = new StringBuffer(src.length());
            int last = 0;
            int pos = src.indexOf("%");
            while (true) {
                if (pos == -1) {
                    sb.append(src.substring(last, len));
                    break;
                } else {
                    sb.append(src.substring(last, pos));
                    if (pos >= len - 2)
                        throw new IllegalArgumentException(
                            "Incomplete trailing escape (%) pattern");
                    char d1 = src.charAt(pos + 1);
                    char d2 = src.charAt(pos + 2);
                    //judge valid hex value
                    if (!(((d1 >= '0' && d1 <= '9') || (d1 >= 'A' && d1 <= 'F') || (d1 >= 'a' && d1 <= 'f')) &&
                          ((d2 >= '0' && d2 <= '9') || (d2 >= 'A' && d2 <= 'F') || (d2 >= 'a' && d2 <= 'f')))) {
                        throw new IllegalArgumentException(
                        "Incomplete trailing escape (%) pattern");
                    }

                    char r = (char)((d1 - '0') * 16 + (d2 - '0'));
                    sb.append(r);
                    last = pos + 3;
                    pos = src.indexOf("%", last);
                }
            }

            return sb.toString();
        }
//        comment out unused method
//        private String getPortAttribute(NginxLookupRequest req) throws NginxLookupException
//        {
//            String proto = req.proto;
//
//            if (IMAP.equalsIgnoreCase(proto))
//                return Provisioning.A_zimbraReverseProxyImapPortAttribute;
//            else if (IMAP_SSL.equalsIgnoreCase(proto))
//                return Provisioning.A_zimbraReverseProxyImapSSLPortAttribute;
//            else if (POP3.equalsIgnoreCase(proto))
//                return Provisioning.A_zimbraReverseProxyPop3PortAttribute;
//            else if (POP3_SSL.equalsIgnoreCase(proto))
//                return Provisioning.A_zimbraReverseProxyPop3SSLPortAttribute;
//            else if (HTTP.equalsIgnoreCase(proto)) {
//                if (req.isZimbraAdmin) {
//                    return Provisioning.A_zimbraReverseProxyAdminPortAttribute;
//                } else {
//                    return Provisioning.A_zimbraReverseProxyHttpPortAttribute;
//                }
//            }
//            else
//                throw new NginxLookupException("unsupported protocol: "+proto);
//        }

        /**
         * verify that the request is from the legitimate nginx admin
         * @throws NginxLookupException
         */
        private void verifyNginxAdmin(Config config, NginxLookupRequest req) throws ServiceException, NginxLookupException {
            Set<String> allowedServerIPs = config.getMultiAttrSet(Provisioning.A_zimbraReverseProxyAdminIPAddress);
            if (!allowedServerIPs.contains(req.serverIp))
                throw new NginxLookupException(SERVER_IP + " " + req.serverIp + " is not allowed");

            Account adminAcct = prov.get(AccountBy.appAdminName, req.adminUser);
            if (adminAcct == null)
                throw new NginxLookupException("admin account " + req.adminUser + " not found");

            // must be global admin
            boolean isAdmin= adminAcct.getBooleanAttr(Provisioning.A_zimbraIsAdminAccount, false);
            if (!isAdmin)
                throw new NginxLookupException(req.adminUser + " is not an admin account");

            Map<String, Object> authCtxt = new HashMap<String, Object>();
            authCtxt.put(AuthContext.AC_ORIGINATING_CLIENT_IP, req.clientIp);
            authCtxt.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, req.adminUser);
            AuthMechanism.doZimbraAuth(prov, null, adminAcct, req.adminPass, authCtxt);
        }

        /**
         * verify whether the account is an admin
         * account could be account name or account ID
         * @throws ServiceException
         */
        private void verifyAccountAdmin(String account, String authMethod)
                throws NginxLookupException, ServiceException {
            Account acct = null;
            if (authMethod.compareToIgnoreCase(AUTHMETH_ZIMBRAID) == 0) {
                acct = prov.get(AccountBy.id, account);
            } else {
                acct = prov.get(AccountBy.name, account);
            }

            if (acct == null) {
                throw new NginxLookupException("account " + account
                        + " not found");
            }

            boolean isAdmin = acct.getBooleanAttr(
                    Provisioning.A_zimbraIsAdminAccount, false);
            boolean isDelegatedAdmin = acct.getBooleanAttr(
                    Provisioning.A_zimbraIsDelegatedAdminAccount, false);
            if (!isAdmin && !isDelegatedAdmin) {
                throw new NginxLookupException("account " + account
                        + " is not admin or delegated admin");
            }
        }

        private String genAuthToken(Account authc, Config config, NginxLookupRequest req)
        throws ServiceException, NginxLookupException {
            verifyNginxAdmin(config, req);

            try {
                if (req.type == NginxLookupRequestType.admin) {
                    return AuthProvider.getAuthToken(authc, true).getEncoded();
                } else {
                    return AuthProvider.getAuthToken(authc).getEncoded();
                }
            } catch (AuthTokenException e) {
                throw new NginxLookupException("failed to generate auth token for " + authc.getName(), e);
            }
        }

        private String getDomainNameByServerIp(ILdapContext zlc, Config config, String serverIp, String unqualifiedName) {
            String domainName = null;

            DomainInfo domainInfo = sDomainNameByVirtualIpCache.get(serverIp);

            if (domainInfo == null) {
                try {
                    Map<String, Boolean> attrs = new HashMap<String, Boolean>();
                    attrs.put(Provisioning.A_zimbraReverseProxyDomainNameAttribute, true);

                    SearchDirResult sdr = helper.searchDirectory(
                            zlc,
                            getDomainSC(config),
                            config,
                            FilterId.NGINX_GET_DOMAIN_BY_SERVER_IP,
                            Provisioning.A_zimbraReverseProxyDomainNameQuery,
                            Provisioning.A_zimbraReverseProxyDomainNameSearchBase,
                            "IPADDR",
                            serverIp,
                            attrs,
                            null);

                    Map<String, String> vals = sdr.configuredAttrs;
                    domainName = vals.get(Provisioning.A_zimbraReverseProxyDomainNameAttribute);

                } catch (NginxLookupException e) {
                    ZimbraLog.nginxlookup.debug("domain not found for user " + unqualifiedName + ".  error: " + e.getMessage());
                }

                if (domainName != null)
                    sDomainNameByVirtualIpCache.put(new DomainInfo(serverIp, domainName));
            } else
                domainName = domainInfo.getDomainName();

            return domainName;
        }

        private DomainExternalRouteInfo getDomainExternalRouteInfoByDomainName(ILdapContext zlc, Config config,
                String domainName, String unqualifiedName) {
            DomainExternalRouteInfo domainExternalRouteInfo = sDomainExternalRouteByDomainNameCache.get(domainName);

            if (domainExternalRouteInfo == null) {
                try {
                    ZLdapFilter filter = ZLdapFilterFactory.getInstance().domainByName(domainName);
                    Map<String, Object> domainAttrs = helper.searchDir(zlc,
                            getDomainSC(config),
                            config,
                            filter,
                            Provisioning.A_zimbraReverseProxyDomainNameSearchBase);
                    String extRouteIncludeOrigAuthname = (String) domainAttrs.get(Provisioning.A_zimbraReverseProxyExternalRouteIncludeOriginalAuthusername);
                    if (extRouteIncludeOrigAuthname == null) {
                        extRouteIncludeOrigAuthname = config.getAttr(Provisioning.A_zimbraReverseProxyExternalRouteIncludeOriginalAuthusername, null);
                    }
                    domainExternalRouteInfo = new DomainExternalRouteInfo(domainName,
                            (String)domainAttrs.get(Provisioning.A_zimbraReverseProxyUseExternalRoute),
                            (String)domainAttrs.get(Provisioning.A_zimbraReverseProxyUseExternalRouteIfAccountNotExist),
                            extRouteIncludeOrigAuthname,
                            (String)domainAttrs.get(Provisioning.A_zimbraExternalPop3Port),
                            (String)domainAttrs.get(Provisioning.A_zimbraExternalPop3SSLPort),
                            (String)domainAttrs.get(Provisioning.A_zimbraExternalImapPort),
                            (String)domainAttrs.get(Provisioning.A_zimbraExternalImapSSLPort),
                            (String)domainAttrs.get(Provisioning.A_zimbraExternalPop3Hostname),
                            (String)domainAttrs.get(Provisioning.A_zimbraExternalPop3SSLHostname),
                            (String)domainAttrs.get(Provisioning.A_zimbraExternalImapHostname),
                            (String)domainAttrs.get(Provisioning.A_zimbraExternalImapSSLHostname));

                    sDomainExternalRouteByDomainNameCache.put(domainExternalRouteInfo);

                } catch (NginxLookupException e) {
                    ZimbraLog.nginxlookup.debug("domain not found for user while search doamin for external route:" +
                            "domain name =" + domainName + ", user name=" + unqualifiedName, e);
                }
            }

            return domainExternalRouteInfo;
        }

        private String getPort(Map<String, String> vals, String lookupAttr, Config config) {
            String port = vals.get(lookupAttr);
            if (port == null) {
                ZimbraLog.nginxlookup.debug("using port from globalConfig");
                String bindPortAttr = config.getAttr(lookupAttr);
                if (bindPortAttr == null)
                    ZimbraLog.nginxlookup.warn("missing config attr: "+lookupAttr);
                else {
                    port = config.getAttr(bindPortAttr);
                    if (port == null)
                        ZimbraLog.nginxlookup.warn("missing config attr: "+bindPortAttr);
                }
            }
            return port;
        }

        private String getPortByMailhostAndProto(ILdapContext zlc, Config config, NginxLookupRequest req, String mailhost)
        throws NginxLookupException {
            String port = null;

            ServerInfo serverInfo = sServerCache.get(mailhost);
            if (serverInfo == null) {
                // get all the ports and cache them
                Map<String, Boolean> attrs = new HashMap<String, Boolean>();
                attrs.put(Provisioning.A_zimbraReverseProxyHttpPortAttribute, false);
                attrs.put(Provisioning.A_zimbraReverseProxyHttpSSLPortAttribute, false);
                attrs.put(Provisioning.A_zimbraReverseProxyAdminPortAttribute, false);
                attrs.put(Provisioning.A_zimbraReverseProxyPop3PortAttribute, false);
                attrs.put(Provisioning.A_zimbraReverseProxyPop3SSLPortAttribute, false);
                attrs.put(Provisioning.A_zimbraReverseProxyImapPortAttribute, false);
                attrs.put(Provisioning.A_zimbraReverseProxyImapSSLPortAttribute, false);

                SearchDirResult sdr = helper.searchDirectory(
                        zlc,
                        getServerSC(config),
                        config,
                        FilterId.NGINX_GET_PORT_BY_MAILHOST,
                        Provisioning.A_zimbraReverseProxyPortQuery,
                        Provisioning.A_zimbraReverseProxyPortSearchBase,
                        "MAILHOST",
                        mailhost,
                        attrs,
                        null);

                Map<String, String> vals = sdr.configuredAttrs;
                serverInfo = new ServerInfo(mailhost);
                serverInfo.setHttpPort(getPort(vals, Provisioning.A_zimbraReverseProxyHttpPortAttribute, config));
                serverInfo.setHttpSSLPort(getPort(vals, Provisioning.A_zimbraReverseProxyHttpSSLPortAttribute, config));
                serverInfo.setHttpAdminPort(getPort(vals, Provisioning.A_zimbraReverseProxyAdminPortAttribute, config));
                serverInfo.setPop3Port(getPort(vals, Provisioning.A_zimbraReverseProxyPop3PortAttribute, config));
                serverInfo.setPop3SSLPort(getPort(vals, Provisioning.A_zimbraReverseProxyPop3SSLPortAttribute, config));
                serverInfo.setImapPort(getPort(vals, Provisioning.A_zimbraReverseProxyImapPortAttribute, config));
                serverInfo.setImapSSLPort(getPort(vals, Provisioning.A_zimbraReverseProxyImapSSLPortAttribute, config));

                sServerCache.put(serverInfo);
            }

            port = serverInfo.getPortForProto(req.proto, req.type);
            if (port == null)
                throw new NginxLookupException("missing port for protocol " + req.proto + " on server " + mailhost);

            return port;
        }

        private String qualifyUserName(ILdapContext zlc, Config config, NginxLookupRequest req, Provisioning prov, String unqualifiedName) {
            String domainName = null;

            if (HTTP.equalsIgnoreCase(req.proto)) {
                /* For HTTP, we need to qualify user based on virtual-host header */
                if (req.serverHost != null) {
                    ZimbraLog.nginxlookup.debug("looking up domain by virtualhost name");
                    Domain d = null;
                    try {
                        d = prov.get(Key.DomainBy.virtualHostname, req.serverHost);
                    } catch (ServiceException e) {
                    }
                    if (d != null) {
                        domainName = d.getName();
                        ZimbraLog.nginxlookup.debug("found domain:" + domainName + " for virtualhost:" + req.serverHost);
                    }
                }
            } else {
                /* For mail, we need to qualify user based on server-ip header */
                if (req.serverIp != null) {
                    domainName = getDomainNameByServerIp(zlc,config, req.serverIp, unqualifiedName);
                }
            }

            if (domainName == null) {
                domainName = config.getAttr(Provisioning.A_zimbraDefaultDomainName);
                ZimbraLog.nginxlookup.debug("domain not found for user " + unqualifiedName + ", using default domain: " + (domainName==null?"null":domainName));
            }

            String qualifiedName = unqualifiedName;
            if (domainName != null) {
                qualifiedName = unqualifiedName + "@" + domainName;
                ZimbraLog.nginxlookup.debug(AUTH_USER + " " + unqualifiedName + " is replaced by " + qualifiedName + " for mailhost lookup");
            } else {
                ZimbraLog.nginxlookup.warn("domain not found for user " + unqualifiedName);
            }

            return qualifiedName;
        }

        /** Qualifies the user-name, if necessary, by suffixing "@domain"
            The domain to be suffixed is the domain object whose zimbraVirtualIPAddress matches the
            IP address specified by req.serverIP (X-Proxy-IP request header)
            @return Fully qualified user name (or user-id), else the original user name
         */
        private String getQualifiedUsername(ILdapContext zlc, Config config, NginxLookupRequest req)
        throws ServiceException, NginxLookupException {
            String aUser = req.user;               /* AUTHZ (whose route is being discovered) */
            String cUser = req.cuser;              /* AUTHC (if GSSAPI) */
            String qUser = aUser;                  /* Qualified AUTHZ (defaults to AUTHZ) */

            Account gssapiAuthC = null;

            if (req.authMethod.equalsIgnoreCase(AUTHMETH_ZIMBRAID)) {
                /* For auth-token based routing, aUser contains the zimbraId of the user
                   No qualification is performed in this case, because the ldap query
                   can handle route lookup by ID also
                 */
                return qUser;
            } else if (req.authMethod.equalsIgnoreCase(AUTHMETH_GSSAPI)) {
                /* For GSSAPI, cUser specifies the authenticating kerberos principal
                   When no separate authorization ID was specified, then in this case,
                   aUser is equal to cUser, and therefore, by transition, aUser is also
                   interpreted as a kerberos principal

                   If a separate authorization ID has been specified, then in this case,
                   the authorization ID is treated in its own right as a fully qualified
                   or a partially qualified user name, and must be qualified according to
                   the regular qualification logic (See bug 24792)

                 */

                boolean authzIsPrincipal;

                authzIsPrincipal = aUser.equalsIgnoreCase(cUser);

                gssapiAuthC = prov.get(AccountBy.krb5Principal,cUser);
                if (gssapiAuthC == null) {
                    throw new NginxLookupException("No account was found which has kerberos principal " + cUser);
                }

                /* overwrite request::cuser (authenticating identity for gssapi) */
                req.cuser = gssapiAuthC.getAttr(Provisioning.A_zimbraMailDeliveryAddress);

                if (authzIsPrincipal) {
                    qUser = gssapiAuthC.getAttr(Provisioning.A_zimbraMailDeliveryAddress);
                }
            } else if (req.authMethod.equalsIgnoreCase(AUTHMETH_CERTAUTH)) {
                Account certAuthAcct = ClientCertAuthenticator.getAccountByX509SubjectDN(req.user);
                if (certAuthAcct == null) {
                    throw new NginxLookupException("account not found: " + req.user);
                }
                req.pass = genAuthToken(certAuthAcct, config, req);
                return certAuthAcct.getName();
            }

            /* At this point, qUser is may not be fully qualified, and so the domain must be looked up
               depending upon which protocol is being used

               For HTTP, the host header must be used in order to lookup the domain by zimbraVirtualHostname
               For MAIL, the proxy ip must be used in order to lookup the domain by zimbraVirtualIPAddress
            */
            if (qUser.indexOf('@') == -1)
                qUser = qualifyUserName(zlc, config, req, prov, aUser);

            if (req.authMethod.equalsIgnoreCase(AUTHMETH_GSSAPI)) {
                /* Now, qUser is as qualified as it is ever going to get.
                   Perform access checks to see whether req.cuser is allowed to act as qUser.
                 */
                Account gssapiAuthZ = prov.get(AccountBy.name, qUser);
                if (gssapiAuthZ == null)
                    throw new NginxLookupException("account not found: " + qUser);

                if (!gssapiAuthC.getId().equals(gssapiAuthZ.getId()) &&
                    !AccessManager.getInstance().canAccessAccount(gssapiAuthC, gssapiAuthZ, true))
                    throw new NginxLookupException("authorization failed for " + gssapiAuthZ.getName() + " (authenticated user " + gssapiAuthC.getName() + " has insufficient rights)");

                /*
                 * finally, all is well, send back an auth-token as a password
                 * req.pass = "0_7e6c9784e1e3d27c311282220c2bc61e4db1bd48_69643d33363a66653664656239372d303162362d346463362d623662312d3265393634333238383931623b6578703d31333a313231353335393937333231333b747970653d363a7a696d6272613b";
                 */
                req.pass = genAuthToken(gssapiAuthC, config, req);
            }
            return qUser;
        }

        private boolean isImapProtocol(String proto) {
            return (IMAP.equalsIgnoreCase(proto) || IMAP_SSL.equalsIgnoreCase(proto));
        }

        private boolean isPop3Protocol(String proto) {
            return (POP3.equalsIgnoreCase(proto) || POP3_SSL.equalsIgnoreCase(proto));
        }

        private boolean isMailProtocol(String proto) {
            return isImapProtocol(proto) || isPop3Protocol(proto);
        }

        private String getExternalHostnameOnAccount(String proto, Map<String, String> vals) {
            if (NginxLookupHandler.POP3.equalsIgnoreCase(proto))
                return vals.get(Provisioning.A_zimbraExternalPop3Hostname);
            else if (NginxLookupHandler.POP3_SSL.equalsIgnoreCase(proto))
                return vals.get(Provisioning.A_zimbraExternalPop3SSLHostname);
            else if (NginxLookupHandler.IMAP.equalsIgnoreCase(proto))
                return vals.get(Provisioning.A_zimbraExternalImapHostname);
            else if (NginxLookupHandler.IMAP_SSL.equalsIgnoreCase(proto))
                return vals.get(Provisioning.A_zimbraExternalImapSSLHostname);
            return null;
        }

        private String getExternalPortOnAccount(String proto, Map<String, String> vals) {
            if (NginxLookupHandler.POP3.equalsIgnoreCase(proto))
                return vals.get(Provisioning.A_zimbraExternalPop3Port);
            else if (NginxLookupHandler.POP3_SSL.equalsIgnoreCase(proto))
                return vals.get(Provisioning.A_zimbraExternalPop3SSLPort);
            else if (NginxLookupHandler.IMAP.equalsIgnoreCase(proto))
                return vals.get(Provisioning.A_zimbraExternalImapPort);
            else if (NginxLookupHandler.IMAP_SSL.equalsIgnoreCase(proto))
                return vals.get(Provisioning.A_zimbraExternalImapSSLPort);
            return null;
        }

        private DomainExternalRouteInfo getDomainExternalRouteInfo(ILdapContext zlc, Config config, String addr) {
            DomainExternalRouteInfo domain = null;
            String[] parts = addr.split("@");
            if (parts.length == 2) {
                String domainName = parts[1];
                domain = getDomainExternalRouteInfoByDomainName(zlc, config, domainName, addr);

            }
            if (domain == null)
                ZimbraLog.nginxlookup.debug("cannot find domain for external route info, user=" + addr);
            return domain;
        }

        /** Setup <b>conn</b> if there are reverse proxy upstream IMAP server configured */
        private void chooseUsingReverseProxyUpstreamImapServer(NginxLookupRequest req, ConnConfig conn)
                throws ServiceException {
            String[] imapServerAddrs = {};
            Account acct = prov.get(AccountBy.name, req.user);
            if (acct == null) {
                return;
            }
            Server server = acct.getServer();
            if (server == null) {
                return;
            }
            imapServerAddrs = server.getReverseProxyUpstreamImapServers();
            if (imapServerAddrs == null || imapServerAddrs.length == 0) {
                return;
            }

            List<Server> imapServers = new LinkedList<Server>();
            for (String host: imapServerAddrs) {
                try {
                    Server imapServer = prov.getServerByServiceHostname(host);
                    if (imapServer == null) {
                        ZimbraLog.nginxlookup.warn("cannot get imap server by service hostname for %s", host);
                        continue;
                    }
                    imapServers.add(imapServer);
                } catch (ServiceException e) {
                    ZimbraLog.nginxlookup.warn("cannot get imap server by service hostname - %s", host);
                }
            }
            if (imapServers.isEmpty()) {
                ZimbraLog.nginxlookup.trace("No available IMAP servers found for acct '%s'", req.user);
                return;
            }
            String accountID = acct.getId();
            ImapLoadBalancingMechanism LBMech = ImapLoadBalancingMechanism.newInstance();
            server = LBMech.getImapServerFromPool(req.httpReq, accountID, imapServers);
            ZimbraLog.nginxlookup.trace("Use IMAP daemon server '%s' for acct '%s' (%s)",
                    server, req.user, accountID);
            if (server != null) {
                conn.host = server.getServiceHostname();
                conn.port = getUpstreamIMAPPort(server, req.proto, true);
            }
        }

        private String getUpstreamIMAPPort(Server server, String proto, Boolean useRemoteImap) throws ServiceException {
            if (useRemoteImap){
                return proto.equals(IMAP) ? server.getRemoteImapBindPortAsString() : server.getRemoteImapSSLBindPortAsString();
            } else {
                return proto.equals(IMAP) ? server.getImapBindPortAsString() : server.getImapSSLBindPortAsString();
            }
        }

        public final static class ConnConfig {
            public String host = null;
            public String port = null;
            public boolean isConfigured() {
                return (host != null) && (port != null);
            }
        }

        private void search(NginxLookupRequest req) throws NginxLookupException {
            ILdapContext zlc = null;
            try {
                zlc = helper.getLdapContext();

                Config config = prov.getConfig();
                String authUser = getQualifiedUsername(zlc, config, req);

                // verify the account is an admin or delegated admin when request ask for admin
                // route, certauth, ...
                if(req.type == NginxLookupRequestType.admin) {
                    verifyAccountAdmin(authUser, req.authMethod);
                }

                // Check if the client IP matches one of the IPs in zimbraDomainAllowedIPs
                Account acct = null;
                if (req.authMethod.compareToIgnoreCase(AUTHMETH_ZIMBRAID) == 0) {
                    acct = prov.get(AccountBy.id, authUser);
                } else {
                    acct = prov.get(AccountBy.name, authUser);
                }
                if (acct != null) {
                    Domain userdomain = prov.getDomain(acct);
                    if (userdomain == null)
                        throw new EntryNotFoundException("domain not found for user:" + authUser);
                    String[] DomainAllowedIPs = userdomain
                            .getMultiAttr(Provisioning.A_zimbraDomainAllowedIPs);
                    if (ZimbraLog.nginxlookup.isDebugEnabled()) {
                        ZimbraLog.nginxlookup.debug("Domain name is %s & DomainAllowedIPs list is %s",
                                userdomain.getName(), Arrays.asList(DomainAllowedIPs));
                    }

                    int i = 0;
                    for (; i < DomainAllowedIPs.length; i++) {
                        // Check if each entry in DomainAllowedIPs is an IP subnet (in CIDR notation eg.x.x.x.y/24) or just a single IP (eg. x.x.x.y)
                        String ipaddr = DomainAllowedIPs[i];
                        if (ipaddr.indexOf("/") == -1) {
                            if (ipaddr.equals(req.clientIp))
                                break;
                        } else {
                            SubnetUtils utils = new SubnetUtils(ipaddr);
                            SubnetInfo info = utils.getInfo();
                            if (info.isInRange(req.clientIp))
                                break;
                        }
                    }
                    if (DomainAllowedIPs.length > 0 && i == DomainAllowedIPs.length)
                        throw new NginxLookupException(CLIENT_IP + " " + req.clientIp + " " + ACCESS_DENIED_ERRMSG);
                }

                if (req.authMethod.equalsIgnoreCase(AUTHMETH_CERTAUTH)) {
                    // for cert auth, no need to find the real route, just
                    // send back zm_auth_token or zm_admin_auth_token
                    sendResult(req, "127.0.0.1", "9999", authUser, false);
                    return;
                }

                SearchDirResult sdr = null;

                String authUserWithRealDomainName = authUser;
                sdr = searchForReverseProxyMailHostInfo(zlc, config, authUser);

                // not found.  Domain part of authUser could contain an alias domain name.
                // If so, try the search again with the domain part converted to the real domain name.
                if (sdr == null) {
                    //
                    // Note: do *not* replace the name to be returned to the client(nginx)
                    //       the name should not be rewritten when the input name is an
                    //       alias or a name with domain alias.
                    //
                    authUserWithRealDomainName = prov.getEmailAddrByDomainAlias(authUser);

                    if (authUserWithRealDomainName != null) {
                        ZimbraLog.nginxlookup.debug("retrying with resolved domain alias: %s",
                                authUserWithRealDomainName);
                        sdr = searchForReverseProxyMailHostInfo(zlc, config, authUserWithRealDomainName);
                    } else {
                        // no luck in alias domain lookup, set it back
                        authUserWithRealDomainName = authUser;
                    }
                }

                /* if still not found, see if we should use external route based on a domain setting
                 * Note that will do sendResult in the called method if not throwing an exception. */
                if (sdr == null) {
                    useDomainExternalRoutingWhenNoUserFound(
                            req, zlc, config, authUser, authUserWithRealDomainName);
                    return;
                }
                ConnConfig conn = new ConnConfig();

                Map<String, String> vals = sdr.configuredAttrs;
                String userName = vals.get(Provisioning.A_zimbraReverseProxyUserNameAttribute);
                if (userName != null) {
                    authUser = authUserWithRealDomainName = userName;
                }

                /* If we should use external route for the user, setup "conn" appropriately. */
                boolean externalRouteIncludeOriginalAuthusername =
                        setupExternalRoutingInfoForUser(conn, req, zlc, config, authUserWithRealDomainName,
                                sdr.extraAttrs);

                /* check to see if an IMAP request should be routed to an upstream IMAP server */
                if (!conn.isConfigured() && isImapProtocol(req.proto)) {
                    chooseUsingReverseProxyUpstreamImapServer(req, conn);
                }

                // use internal route
                if (conn.host == null) {
                    conn.host = vals.get(Provisioning.A_zimbraReverseProxyMailHostAttribute);
                }

                if (conn.host == null) {
                    throw new NginxLookupException("mailhost not found for user: "+req.user);
                }
                if (conn.port == null) {
                    conn.port = getPortByMailhostAndProto(zlc, config, req, conn.host);
                }
                sendResult(req, resolvedHostname(conn.host), conn.port, authUser,
                        externalRouteIncludeOriginalAuthusername);
            } catch (NginxLookupException e) {
                throw e;
            } catch (Exception e) {
                throw new NginxLookupException(e);
            } finally {
                helper.closeLdapContext(zlc);
            }
        }

        /**
         * bug 37266, support proxy-side dns lookup
         * @param hostname
         * @return either <b>hostname</b> or the result of a proxy-side DNS lookup if appropriate
         * @throws ServiceException
         * @throws UnknownHostException
         */
        private String resolvedHostname(String hostname) throws ServiceException, UnknownHostException {
            boolean doDnsLookup = true;
            Server server = prov.getLocalServer();
            if (server == null) {
                doDnsLookup = prov.getConfig().getBooleanAttr(
                        Provisioning.A_zimbraReverseProxyDnsLookupInServerEnabled, true);
            } else {
                doDnsLookup = server.getBooleanAttr(
                        Provisioning.A_zimbraReverseProxyDnsLookupInServerEnabled, true);
            }
            if (doDnsLookup) {
                return getIPByIPMode(hostname).getHostAddress();
            }
            return hostname;
        }

        private SearchDirResult searchForReverseProxyMailHostInfo(
                ILdapContext zlc, Config config, String authUser)
                        throws NginxLookupException {
            SearchDirResult sdr = null;
            try {
                sdr = helper.searchDirectory(zlc, getUserSC(config), config,
                        FilterId.NGINX_GET_MAILHOST,
                        Provisioning.A_zimbraReverseProxyMailHostQuery,
                        Provisioning.A_zimbraReverseProxyMailHostSearchBase,
                        "USER", authUser, REV_PROXY_MAILHOST_ATTRS, REV_PROXY_MAILHOST_EXTRA_ATTRS);
            } catch (EntryNotFoundException e) {
                ZimbraLog.nginxlookup.debug("user  not found", authUser, e);
            }
            return sdr;
        }

        /**
         * If domain external routing is configured, then use it and do {@link #sendResult}
         * otherwise throw an exception
         */
        private void useDomainExternalRoutingWhenNoUserFound(NginxLookupRequest req, ILdapContext zlc,
                Config config, String authUser, String authUserWithRealDomainName)
                        throws EntryNotFoundException, UnknownHostException, ServiceException {
            DomainExternalRouteInfo domain =
                    getDomainExternalRouteInfo(zlc, config, authUserWithRealDomainName);
            if (domain == null || !domain.useExternalRouteIfAccountNotExist()) {
                throw new EntryNotFoundException("user not found:" + authUserWithRealDomainName);
            }

            String mailhost = domain.getHostname(req.proto);
            String port = domain.getPort(req.proto);

            if (mailhost == null || port == null) {
                throw new EntryNotFoundException(String.format(
                        "User '%s' not found. No external route info on domain '%s' but '%s=TRUE'",
                        authUserWithRealDomainName, domain.getDomainName(),
                        Provisioning.A_zimbraReverseProxyUseExternalRouteIfAccountNotExist));
            }
            sendResult(req, resolvedHostname(mailhost), port, authUser, false);
        }

        /**
         * If should use external route, setup {@link ConnConfig} appropriately
         * @param conn If using external routing, set this up appropriately
         * @return whether should include the original authusername
         * @throws ServiceException
         */
        private boolean setupExternalRoutingInfoForUser(ConnConfig conn, NginxLookupRequest req,
                ILdapContext zlc, Config config, String authUserWithRealDomainName,
                Map<String, String> extraAttrsVals) throws ServiceException {

            // external route is only applicable to mail protocols
            if (!isMailProtocol(req.proto)) {
                return false;
            }
            DomainExternalRouteInfo domain = null;

            String useExtRouteOnAcct = extraAttrsVals.get(Provisioning.A_zimbraReverseProxyUseExternalRoute);
            if (useExtRouteOnAcct == null) {
                // check if it is set on domain
                domain = getDomainExternalRouteInfo(zlc, config, authUserWithRealDomainName);
                if (domain == null) {
                    // don't throw, just fallback to use internal route
                    ZimbraLog.nginxlookup.warn("cannot find domain for external route info, " +
                            "fallback to use internal route, user=%s", authUserWithRealDomainName);
                    return false;
                } else if (!domain.useExternalRoute()) {
                    return false;
                }
            } else {
                if (!ProvisioningConstants.TRUE.equals(useExtRouteOnAcct)) {
                    return false;
                }
            }

            ZimbraLog.nginxlookup.debug("fetching external route for user %s", authUserWithRealDomainName);

            // get whether you need to include domain name
            if (domain == null) {
                domain = getDomainExternalRouteInfo(zlc, config, authUserWithRealDomainName);
            }

            // get external host/port on account
            conn.host = getExternalHostnameOnAccount(req.proto, extraAttrsVals);
            conn.port = getExternalPortOnAccount(req.proto, extraAttrsVals);

            if (!conn.isConfigured()) {
                // not set or not set completely on account, try domain
                if (domain == null) {
                    ZimbraLog.nginxlookup.warn("cannot find domain for external route info, " +
                            "fallback to use internal route, user=%s", authUserWithRealDomainName);
                } else {
                    conn.host = domain.getHostname(req.proto);
                    conn.port = domain.getPort(req.proto);
                }
            }

            // external host/port not set or not set completely on account/domain, null both and
            // we will fallback to the internal route
            if (!conn.isConfigured()) {
                ZimbraLog.nginxlookup.info("account %s has %s set to TRUE but missing external " +
                        "route info, fallback to use internal route", authUserWithRealDomainName,
                        Provisioning.A_zimbraReverseProxyUseExternalRoute);
                conn.host = null;
                conn.port = null;
            } else {
                ZimbraLog.nginxlookup.debug("external route for user %s, host=%s, port=%s",
                        authUserWithRealDomainName, conn.host, conn.port);
            }
            return externalRouteIncludeOriginalAuthusername(domain);
        }

        private boolean externalRouteIncludeOriginalAuthusername(DomainExternalRouteInfo domain)
                throws ServiceException {
            return (domain == null) ?
                    prov.getDefaultDomain().isReverseProxyExternalRouteIncludeOriginalAuthusername() :
                    domain.externalRouteIncludeOriginalAuthusername();
        }

        /** get the IP address of the host name according to current IP mode
         *
         * for ipv4 mode, the first ipv4 address will be used.
         * for ipv6 mode, the first ipv6 address will be used.
         * for both mode, try to return the first available ipv4. If no ipv4 available,
         * use the first available ipv6
         *
         * @param hostname the host name to be resolved
         * @return the IP Address
         * @throws ServiceException
         * @throws UnknownHostException
         */
        public InetAddress getIPByIPMode(String hostname) throws ServiceException, UnknownHostException {
            String localhost = LC.get("zimbra_server_hostname");
            IPMode mode = Provisioning.getInstance().getServerByName(localhost).getIPMode();
            InetAddress[] ips = InetAddress.getAllByName(hostname);
            if (mode == IPMode.ipv4) {
                for (InetAddress ip: ips) {
                    if (ip instanceof Inet4Address) {
                        return ip;
                    }
                }
                throw ServiceException.FAILURE("Can't find available IPv4 address for upstream " + hostname + " whose IP mode is IPv4 only", null);
            } else if (mode == IPMode.ipv6) {
                for (InetAddress ip: ips) {
                    if (ip instanceof Inet6Address) {
                        return ip;
                    }
                }
                throw ServiceException.FAILURE("Can't find available IPv6 address for upstream " + hostname + " whose IP mode is IPv6 bonly", null);
            } else {
                for (InetAddress ip: ips) {
                    if (ip instanceof Inet4Address) {
                        return ip;
                    }
                }
                return ips[0];
            }
        }

        /**
         * Send the routing information HTTP response back to the NGINX IMAP proxy
         * @param req    The HTTP request object
         * @param addr
         * @param port        The requested mail server port
         * @param origAuthUser If not null, then this value is sent back to override the login
         *                     user name, (usually) with a domain suffix added
         * @param externalRouteIncludeOriginalAuthusername - include original username as requested
         */
        private void sendResult(NginxLookupRequest req, String addr, String port, String origAuthUser,
                boolean externalRouteIncludeOriginalAuthusername)
                        throws UnknownHostException {
            String authUser = origAuthUser;
            ZimbraLog.nginxlookup.debug("SendingResult mailhost=%s port=%s clientIp=%s",
                    addr, port, req.clientIp);

            HttpServletResponse resp = req.httpResp;
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.addHeader(AUTH_STATUS, "OK");
            resp.addHeader(AUTH_SERVER, addr);
            resp.addHeader(AUTH_PORT, port);
            try {
                if (StringUtil.equal(prov.getDomainByEmailAddr(authUser).getName(),
                        prov.getConfig().getDefaultDomainName())) {
                    resp.addHeader(AUTH_CACHE_ALIAS, "TRUE");
                } else {
                    resp.addHeader(AUTH_CACHE_ALIAS, "FALSE");
                }
            } catch (ServiceException e) {
                // turn off alias cache if authUser is empty or if any error
                resp.addHeader(AUTH_CACHE_ALIAS, "FALSE");
            }

            if (externalRouteIncludeOriginalAuthusername) {
                authUser = req.user;
            }

            if (authUser != null) {
                /* encode authUser, %-->%25 ' '-->%20 */
                String respAuthUser = authUser.replace(" ", "%20").replace("%", "%25");
                if (authUser.equals(respAuthUser)) {
                    ZimbraLog.nginxlookup.debug("Response Header - add %s: '%s'", AUTH_USER, authUser);
                } else {
                    ZimbraLog.nginxlookup.debug("Response Header - rewrite %s '%s' to: '%s'",
                            AUTH_USER, authUser, respAuthUser);
                }
                resp.addHeader(AUTH_USER, respAuthUser);
            }

            if (req.authMethod.equalsIgnoreCase(AUTHMETH_GSSAPI)) {
                // For GSSAPI, we also need to send back the overriden authenticating ID and the auth-token as password
                resp.addHeader(AUTH_ID, req.cuser);
                resp.addHeader(AUTH_PASS, req.pass);
            } else if (req.authMethod.equalsIgnoreCase(AUTHMETH_CERTAUTH)) {
                // For CERTAUTH, we also need to send back the auth-token as password
                resp.addHeader(AUTH_PASS, req.pass);
            }
        }

        /**
         * Indicate an error to the calling (NGINX) proxy
         * @param resp  The HTTP response object
         * @param msg   The error message (a generic error message is sent back to the caller, the original message is logged)
         */
        private void sendError(HttpServletResponse resp, String msg) {

            ZimbraLog.nginxlookup.debug(msg);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.addHeader(AUTH_STATUS, msg);

            String waitInterval = null;
            try {
                Config config = Provisioning.getInstance().getConfig();
                long wi = config.getTimeIntervalSecs(Provisioning.A_zimbraReverseProxyAuthWaitInterval, DEFAULT_WAIT_INTERVAL);
                waitInterval = "" + wi;
            } catch (ServiceException e) {
                ZimbraLog.nginxlookup.warn("cannot get config");
                waitInterval = "" + DEFAULT_WAIT_INTERVAL;
            }
            resp.addHeader(AUTH_WAIT, waitInterval);
        }
    }
}
