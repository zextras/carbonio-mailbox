// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Factory class for various ProxySelector types.
 */
public final class ProxySelectors {
    private static ProxySelector defaultProxySelector;

    private static ProxySelector create() {
        var proxySelector = ProxySelector.getDefault();
        String className = LC.zimbra_class_customproxyselector.value();
        if (!className.isEmpty()) {
            try {
							proxySelector = (ProxySelector) Class.forName(className).newInstance();
            } catch (Exception e) {
                ZimbraLog.net.error("could not instantiate ConditionalProxySelector interface of class '" + className + "'; defaulting to system proxy settings", e);
            }
        }
        return proxySelector;
    }

    /**
     * On supported systems returns the native ProxySelector otherwise
     * returns the system default.
     *
     * @return the default ProxySelector
     */
    public static synchronized ProxySelector defaultProxySelector() {
        if (Objects.isNull(defaultProxySelector)) {
             defaultProxySelector =  create();
        }
        return defaultProxySelector;
    }

    /*
     * Custom proxy selector that replaces system default if no native proxy
     * is available. This proxy selector will delegate to the system default
     * for HTTP as well as SOCKS if SOCKS support is enabled. Also excludes
     * invalid proxy results (i.e. invalid port) which works around issues
     * on Linux hosts with incorrect settings.
     */
//    public static class CustomProxySelector extends ProxySelector {
//        protected ProxySelector ps;
//
//        protected CustomProxySelector(ProxySelector ps) {
//            this.ps = ps;
//        }
//
//        protected void setDefaultProxySelector(ProxySelector ps) {
//            this.ps = ps;
//        }
//
//        public List<Proxy> select(URI uri) {
//            List<Proxy> proxies = ps.select(uri);
//            for (Iterator<Proxy> it = proxies.iterator(); it.hasNext(); ) {
//                if (!isValidProxy(it.next())) {
//                    it.remove();
//                }
//            }
//            if (proxies.isEmpty()) {
//                proxies.add(Proxy.NO_PROXY);
//            }
//            return proxies;
//        }
//
//        public void connectFailed(URI uri, SocketAddress sa, IOException e) {
//            ps.connectFailed(uri, sa, e);
//        }
//    }

    private static boolean isValidProxy(Proxy proxy) {
        InetSocketAddress addr = (InetSocketAddress) proxy.address();
        switch (proxy.type()) {
        case SOCKS:
            return NetConfig.getInstance().isSocksEnabled() && addr.getPort() > 0;
        case HTTP:
            return addr.getPort() > 0;
        default:
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.useSystemProxies", "true");
        String url = args.length > 0 ? args[0] : "http://www.news.com";
        System.out.printf("Proxy information for %s :\n", url);
        List<Proxy> proxies = defaultProxySelector().select(new URI(url));
        for (int i = 0; i < proxies.size(); i++) {
            System.out.printf("proxy[%d] = %s\n", i, proxies.get(i));
        }
    }
}
