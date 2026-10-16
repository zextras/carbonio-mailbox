// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.client;

import com.zimbra.common.httpclient.HttpClientUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.auth.BasicSchemeFactory;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.soap.W3cDomUtil;
import com.zimbra.common.soap.XmlParseException;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.dav.DavContext.Depth;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.httpclient.HttpProxyUtil;
import com.zimbra.cs.service.UserServlet.HttpInputStream;
import com.zimbra.cs.util.BuildInfo;

public class WebDavClient {

    public WebDavClient(String baseUrl) {
        this(baseUrl, "ZCS");
    }

    public WebDavClient(String baseUrl, String app) {
        mBaseUrl = baseUrl;
        mClient = ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();
        HttpProxyUtil.configureProxy(mClient);
        setAppName(app);
    }

    public Collection<DavObject> listObjects(String path, Collection<QName> extraProps) throws IOException, DavException {
        DavRequest propfind = DavRequest.PROPFIND(path);
        propfind.setDepth(Depth.one);
        if (extraProps == null) {
            propfind.addRequestProp(DavElements.E_DISPLAYNAME);
            propfind.addRequestProp(DavElements.E_RESOURCETYPE);
            propfind.addRequestProp(DavElements.E_CREATIONDATE);
            propfind.addRequestProp(DavElements.E_GETCONTENTLENGTH);
            propfind.addRequestProp(DavElements.E_GETCONTENTLANGUAGE);
            propfind.addRequestProp(DavElements.E_GETCONTENTTYPE);
            propfind.addRequestProp(DavElements.E_GETETAG);
            propfind.addRequestProp(DavElements.E_GETLASTMODIFIED);
        } else {
            for (QName p : extraProps)
                propfind.addRequestProp(p);
        }
        return sendMultiResponseRequest(propfind);
    }

    public Collection<DavObject> sendMultiResponseRequest(DavRequest req) throws IOException, DavException {
        ArrayList<DavObject> ret = new ArrayList<>();

        ClassicHttpResponse response = null;
        try {
            response = executeFollowRedirect(req);
            int status = response.getCode();
            if (status >= 400) {
                throw new DavException("DAV server returned an error: "+status, status);
            }

            Document doc = W3cDomUtil.parseXMLToDom4jDocUsingSecureProcessing(response.getEntity().getContent());
            Element top = doc.getRootElement();
            for (Object obj : top.elements(DavElements.E_RESPONSE)) {
                if (obj instanceof Element) {
                    ret.add(new DavObject((Element)obj));
                }
            }
        } catch (XmlParseException e) {
            throw new DavException("can't parse response", e);
        } finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
        return ret;
    }

    public ClassicHttpResponse  sendCustomRequest(DavRequest req) throws IOException, DavException {
        ClassicHttpResponse response = executeFollowRedirect(req);
        return response;
    }

    public HttpInputStream sendRequest(DavRequest req) throws IOException, DavException {
        ClassicHttpResponse response = executeFollowRedirect(req);
        return new HttpInputStream(response);
    }

    public HttpInputStream sendGet(String href) throws IOException {
        HttpGet get = new HttpGet(mBaseUrl + href);
        ClassicHttpResponse response = executeMethod(get, Depth.zero);
        return new HttpInputStream(response);
    }

    public HttpInputStream sendPut(String href, byte[] buf, String contentType, String etag, Collection<Pair<String,String>> headers) throws IOException {
        boolean done = false;
        ClassicHttpResponse response = null;
        while (!done) {
            HttpPut put = new HttpPut(mBaseUrl + href);
            put.setEntity(new ByteArrayEntity(buf, ContentType.create(contentType)));
            if (mDebugEnabled && contentType.startsWith("text"))
                ZimbraLog.dav.debug("PUT payload: \n"+new String(buf, StandardCharsets.UTF_8));
            if (etag != null)
                put.addHeader(DavProtocol.HEADER_IF_MATCH, etag);
            if (headers != null)
                for (Pair<String,String> h : headers)
                    put.addHeader(h.getFirst(), h.getSecond());
            response = executeMethod(put, Depth.zero);
            int ret = response.getCode();
            if (ret == HttpStatus.SC_MOVED_PERMANENTLY || ret == HttpStatus.SC_MOVED_TEMPORARILY) {
                Header newLocation = response.getFirstHeader("Location");
                if (newLocation != null) {
                    href = newLocation.getValue();
                    ZimbraLog.dav.debug("redirect to new url = "+href);
                    put.reset();
                    continue;
                }
            }
            done = true;
        }
        return new HttpInputStream(response);
    }

    protected ClassicHttpResponse executeFollowRedirect(DavRequest req) throws IOException {
        ClassicHttpResponse response = null;
        boolean done = false;
        while (!done) {
            response = execute(req);
            int ret = response.getCode();
            if (ret == HttpStatus.SC_MOVED_PERMANENTLY || ret == HttpStatus.SC_MOVED_TEMPORARILY) {
                Header newLocation = response.getFirstHeader("Location");
                if (newLocation != null) {
                    String uri = newLocation.getValue();
                    ZimbraLog.dav.debug("redirect to new url = "+uri);
                    EntityUtils.consume(response.getEntity());
                    req.setRedirectUrl(uri);
                    continue;
                }
            }
            done = true;
        }
        return response;
    }

    protected void logRequestInfo(HttpUriRequestBase method, String body) throws IOException {
        if (!mDebugEnabled) {
            return;
        }
        StringBuilder reqLog = new StringBuilder();
        reqLog.append("WebDAV request:\n").append(method.getMethod()).append(" ").append(HttpClientUtil.requestUri(method).toString());
        reqLog.append('\n');
        Header[] headers = method.getHeaders();
        if (headers != null && headers.length > 0) {
            for (Header hdr : headers) {
                String hdrName = hdr.getName();
                reqLog.append(hdrName).append('=');
                if (hdrName.contains("Auth")  || (hdrName.contains(HttpHeaders.COOKIE))) {
                    reqLog.append("*** REPLACED ***\n");
                } else {
                    reqLog.append(hdr.getValue()).append('\n');
                }
            }
        }
        if (Strings.isNullOrEmpty(body) || !ZimbraLog.dav.isTraceEnabled()) {
            ZimbraLog.dav.debug(reqLog.toString());
        } else {
            ZimbraLog.dav.debug("%s\n%s", reqLog.toString(), body);
        }
    }

    protected void logResponseInfo(ClassicHttpResponse response) throws IOException {
        if (!mDebugEnabled) {
            return;
        }
        StringBuilder responseLog = new StringBuilder();
        responseLog.append("WebDAV response:\n").append(new StatusLine(response)).append('\n');
        Header[] headers = response.getHeaders();
        if (headers != null && headers.length > 0) {
            for (Header hdr : headers) {
                String hdrName = hdr.getName();
                responseLog.append(hdrName).append('=');
                if (hdrName.contains("Auth")  || (hdrName.contains(HttpHeaders.COOKIE))) {
                    responseLog.append("*** REPLACED ***\n");
                } else {
                    responseLog.append(hdr.getValue()).append('\n');
                }
            }
        }
        if (response.getEntity() == null || !ZimbraLog.dav.isTraceEnabled()) {
            ZimbraLog.dav.debug(responseLog.toString());
        } else {
            ZimbraLog.dav.debug("%s\n%s", responseLog.toString(), new String(EntityUtils.toByteArray(response.getEntity()),
                StandardCharsets.UTF_8));
        }
    }

    protected ClassicHttpResponse execute(DavRequest req) throws IOException {
        HttpUriRequestBase m = req.getHttpMethod(mBaseUrl);
        for (Pair<String,String> header : req.getRequestHeaders()) {
            m.addHeader(header.getFirst(), header.getSecond());
        }
        return executeMethod(m, req.getDepth(), req.getRequestMessageString());
    }

    protected ClassicHttpResponse executeMethod(HttpUriRequestBase m, Depth d, String bodyForLogging) throws IOException {
        
          
        Registry<AuthSchemeFactory> authSchemeRegistry = RegistryBuilder.<AuthSchemeFactory>create()
            .register(StandardAuthScheme.BASIC, new BasicSchemeFactory()).build();
       mClient.setDefaultAuthSchemeRegistry(authSchemeRegistry);
       HttpClient client = mClient.build();
        m.addHeader("User-Agent", mUserAgent);
        String depth = "0";
        switch (d) {
        case one:
            depth = "1";
            break;
        case infinity:
            depth = "infinity";
            break;
          default:
            break;
        }
        m.addHeader("Depth", depth);
        logRequestInfo(m, bodyForLogging);
        ClassicHttpResponse response = client.executeOpen(null, m, context);

        logResponseInfo(response);
        return response;
    }

    protected ClassicHttpResponse executeMethod(HttpUriRequestBase m, Depth d) throws IOException {
        return executeMethod(m, d, null);
    }

    public void setCredential(String user, String pass, String targetUrl) {
        mUsername = user;
        mPassword = pass;
        Credentials cred = new UsernamePasswordCredentials(mUsername, mPassword.toCharArray());
        CredentialsStore provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(null, -1), cred);
        
        HttpHost targetHost = new HttpHost(targetUrl);
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());
         
        // Add AuthCache to the execution context
        context = HttpClientContext.create();
        context.setCredentialsProvider(provider);
        context.setAuthCache(authCache);
        
    }

    public void setAuthCookie(ZAuthToken auth) {
        Map<String, String> cookieMap = auth.cookieMap(false);
        if (cookieMap != null) {
            String host = null;
            try {
                host = new URL(mBaseUrl).getHost();
            } catch (Exception e) {
            }
            BasicCookieStore cookieStore = new BasicCookieStore();
            for (Map.Entry<String, String> ck : cookieMap.entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(ck.getKey(), ck.getValue());
                cookie.setDomain(host);
                cookie.setPath("/");
                cookie.setSecure(false);
                cookieStore.addCookie(cookie);
                
            }
            mClient.setDefaultCookieStore(cookieStore);
            RequestConfig reqConfig = RequestConfig.copy(
                ZimbraHttpConnectionManager.getInternalHttpConnMgr().getZimbraConnMgrParams().getReqConfig())
                .setCookieSpec(StandardCookieSpec.RELAXED).build();

            mClient.setDefaultRequestConfig(reqConfig);
        }
    }

    public String getUsername() {
        return mUsername;
    }
    public String getPassword() {
        return mPassword;
    }

    public void setDebugEnabled(boolean b) {
        mDebugEnabled = b;
    }

    public void setUserAgent(String ua) {
        mUserAgent = ua;
    }

    public void setAppName(String app) {
        mUserAgent = "Zimbra " + app + "/" + BuildInfo.VERSION + " (" + BuildInfo.DATE + ")";
    }

    protected String mUserAgent;
    private final String mBaseUrl;
    private String mUsername;
    private String mPassword;
    protected final HttpClientBuilder  mClient;
    private boolean mDebugEnabled = false;
    private  HttpClientContext context;
}
