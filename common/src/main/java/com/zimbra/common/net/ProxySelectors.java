// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;

/**
 * Factory class for various ProxySelector types.
 */
public final class ProxySelectors {
    private static final ProxySelector systemProxySelector;
    private static ProxySelector defaultProxySelector;

    static {
        systemProxySelector = ProxySelector.getDefault();
        defaultProxySelector = new CustomProxySelector(systemProxySelector);
        String className = LC.zimbra_class_customproxyselector.value();
        if (className != null && !className.equals("")) {
            try {
                CustomProxySelector selector = (CustomProxySelector) Class.forName(className).newInstance();
                selector.setDefaultProxySelector(defaultProxySelector);
                defaultProxySelector = selector;
            } catch (Exception e) {
                ZimbraLog.net.error("could not instantiate ConditionalProxySelector interface of class '" + className + "'; defaulting to system proxy settings", e);
            }
        }
    }

    /**
     * On supported systems returns the native ProxySelector otherwise
     * returns the system default.
     *
     * @return the default ProxySelector
     */
    public static ProxySelector defaultProxySelector() {
        return defaultProxySelector;
    }

    /**
     * Returns the original system default ProxySelector.
     * @return the system default ProxySelector
     */
    public static ProxySelector systemProxySelector() {
        return systemProxySelector;
    }

    /**
     * Returns a "dummy" ProxySelector whose select method always returns
     * a DIRECT connection. Used for testing.
     *
     * @return the dummy ProxySelector
     */
    public static ProxySelector dummyProxySelector() {
        return new ProxySelector() {
            public List<Proxy> select(URI uri) {
                return Arrays.asList(Proxy.NO_PROXY);
            }

            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                // Do nothing...
            }
        };
    }

    /*
     * Custom proxy selector that replaces system default if no native proxy
     * is available. This proxy selector will delegate to the system default
     * for HTTP as well as SOCKS if SOCKS support is enabled. Also excludes
     * invalid proxy results (i.e. invalid port) which works around issues
     * on Linux hosts with incorrect settings.
     */
    public static class CustomProxySelector extends ProxySelector {
        protected ProxySelector ps;

        protected CustomProxySelector(ProxySelector ps) {
            this.ps = ps;
        }
        
        protected void setDefaultProxySelector(ProxySelector ps) {
            this.ps = ps;
        }

        public List<Proxy> select(URI uri) {
            List<Proxy> proxies = ps.select(uri);
            for (Iterator<Proxy> it = proxies.iterator(); it.hasNext(); ) {
                if (!isValidProxy(it.next())) {
                    it.remove();
                }
            }
            if (proxies.isEmpty()) {
                proxies.add(Proxy.NO_PROXY);
            }
            return proxies;
        }

        public void connectFailed(URI uri, SocketAddress sa, IOException e) {
            ps.connectFailed(uri, sa, e);
        }
    }

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

    private static SocketAddress saddr(String host, int port) {
        return new InetSocketAddress(host, port);
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
