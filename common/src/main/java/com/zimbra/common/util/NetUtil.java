// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.zimbra.common.service.ServiceException;

public class NetUtil {

    public static ServerSocket getTcpServerSocket(String address, int port) throws ServiceException {
        return getServerSocket(address, port, false, false, null, null, null);
    }

    public static ServerSocket getSslTcpServerSocket(String address, int port, String[] excludeCiphers, String[] includeCiphers) throws ServiceException {
        return getServerSocket(address, port, true, /* doesn't matter, but keep it false always */ false, excludeCiphers, includeCiphers, null);
    }

    public static ServerSocket getSslTcpServerSocket(String address, int port, String[] excludeCiphers, String[] includeCiphers, String[] sslProtocols) throws ServiceException {
        return getServerSocket(address, port, true, /* doesn't matter, but keep it false always */ false, excludeCiphers, includeCiphers, sslProtocols);
    }

    public static ServerSocket getNioServerSocket(String address, int port) throws ServiceException {
        return getServerSocket(address, port, false, true, null, null, null);
    }

    public static void bindTcpServerSocket(String address, int port) throws IOException {
        bindServerSocket(address, port, false, false, null, null, null);
    }

    public static void bindSslTcpServerSocket(String address, int port, String[] excludeCiphers, String[] includeCiphers) throws IOException {
        bindServerSocket(address, port, true, /* doesn't matter, but it false always */ false, excludeCiphers, includeCiphers, null);
    }

    public static void bindSslTcpServerSocket(String address, int port, String[] excludeCiphers, String[] includeCiphers, String[] sslProtocols) throws IOException {
        bindServerSocket(address, port, true, /* doesn't matter, but it false always */ false, excludeCiphers, includeCiphers, sslProtocols);
    }

    public static void bindNioServerSocket(String address, int port) throws IOException {
        bindServerSocket(address, port, false, true, null, null, null);
    }

    public static synchronized ServerSocket getServerSocket(String address, int port, boolean ssl, boolean useChannels, String[] excludeCiphers, String[] includeCiphers) throws ServiceException {
        return getServerSocket(address, port, true, /* doesn't matter, but keep it false always */ false, excludeCiphers, includeCiphers, null);
    }

    public static synchronized ServerSocket getServerSocket(String address, int port, boolean ssl, boolean useChannels, String[] excludeCiphers, String[] includeCiphers, String[] sslProtocols) throws ServiceException {
        ServerSocket serverSocket = getAlreadyBoundServerSocket(address, port, ssl, useChannels);
        if (serverSocket != null) {
            return serverSocket;
        }
        try {
            serverSocket = newBoundServerSocket(address, port, ssl, useChannels, excludeCiphers, includeCiphers, sslProtocols);
        } catch (IOException ioe) {
            throw ServiceException.FAILURE("Could not bind to port=" + port + " bindaddr=" + address + " ssl=" + ssl + " useChannels=" + useChannels, ioe);
        }
        if (serverSocket == null) {
            throw ServiceException.FAILURE("Server socket null after bind to port=" + port + " bindaddr=" + address + " ssl=" + ssl + " useChannels=" + useChannels, null);
        }
        return serverSocket;
    }

    private static ServerSocket newBoundServerSocket(String address, int port, boolean ssl, boolean useChannels,
            String[] excludeCiphers, String[] includeCiphers, String[] sslProtocols) throws IOException {
        ServerSocket serverSocket = null;
        InetAddress bindAddress = null;
        if (address != null && address.length() > 0) {
            bindAddress = InetAddress.getByName(address);
        }

        if (useChannels) {
            //for SSL channels, it's up to the selector to configure SSL stuff
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false); //I believe the only time we use channels is in NIO
            serverSocket = serverSocketChannel.socket();
        } else {
            if (ssl) {
                SSLServerSocketFactory fact = (SSLServerSocketFactory)
                SSLServerSocketFactory.getDefault();
                serverSocket = fact.createServerSocket();
                setSSLProtocols((SSLServerSocket)serverSocket, sslProtocols);
                setSSLEnabledCipherSuites((SSLServerSocket)serverSocket, excludeCiphers, includeCiphers);
            } else {
                serverSocket = new ServerSocket();
            }
        }

        serverSocket.setReuseAddress(true);
        InetSocketAddress isa = new InetSocketAddress(bindAddress, port);
        serverSocket.bind(isa, 1024);
        return serverSocket;
    }

    private static void setSSLProtocols(SSLServerSocket socket, String[] sslProtocols) {
        if (sslProtocols != null && sslProtocols.length > 0) {
            socket.setEnabledProtocols(sslProtocols);
        }
    }

    public static void setSSLProtocols(SSLSocket socket, String[] sslProtocols) {
        if (sslProtocols != null && sslProtocols.length > 0) {
            socket.setEnabledProtocols(sslProtocols);
        }
    }

    /**
     *
     * @param enabledCiphers
     * @param excludeCiphers
     * @return Array of enabled cipher after excluding the unwanted ones
     *         null if either enabledCiphers or excludeCiphers are null or empty.  Callers should not
     *         alter the default ciphers on the SSL socket/engine if computeEnabledCipherSuites returns null.
     */
    public static String[] computeEnabledCipherSuites(String[] enabledCiphers, String[] excludeCiphers) {
        if (enabledCiphers == null || enabledCiphers.length == 0 ||
            excludeCiphers == null || excludeCiphers.length == 0)
            return null;

        List<String> excludedCSList = new ArrayList<>(Arrays.asList(excludeCiphers));
        List<String> enabledCSList = new ArrayList<>(Arrays.asList(enabledCiphers));

        for (String cipher : excludedCSList) {
          enabledCSList.remove(cipher);
        }

        return enabledCSList.toArray(new String[0]);
    }

    private static void setSSLEnabledCipherSuites(SSLServerSocket socket, String[] excludeCiphers, String[] includeCiphers) {
        String[] enabledCiphers = computeEnabledCipherSuites(includeCiphers != null && includeCiphers.length > 0 ? includeCiphers : socket.getEnabledCipherSuites(), excludeCiphers);
        if (enabledCiphers != null)
            socket.setEnabledCipherSuites(enabledCiphers);
    }

    public static void setSSLEnabledCipherSuites(SSLSocket socket, String[] excludeCiphers, String[] includeCiphers) {
        String[] enabledCiphers = computeEnabledCipherSuites(includeCiphers != null && includeCiphers.length > 0 ? includeCiphers : socket.getEnabledCipherSuites(), excludeCiphers);
        if (enabledCiphers != null)
            socket.setEnabledCipherSuites(enabledCiphers);
    }

    private static Map<String, ServerSocket> mBoundSockets = new HashMap<>();

    private static String makeKey(String address, int port, boolean ssl, boolean useChannels) {
        return "[ssl=" + ssl + ";addr=" + address + ";port=" + port + ";useChannels=" + useChannels + "]";
    }

    public static void dumpMap() {
      for (Map.Entry<String, ServerSocket> entry : mBoundSockets.entrySet()) {
        System.err.println(entry.getKey() + " => " + entry.getValue());
      }
    }

    public static synchronized void bindServerSocket(String address, int port, boolean ssl, boolean useChannels, String[] excludeCiphers, String[] includeCiphers) throws IOException {
        bindServerSocket(address, port, ssl, useChannels, excludeCiphers, includeCiphers, null );
    }

    public static synchronized void bindServerSocket(String address, int port, boolean ssl, boolean useChannels, String[] excludeCiphers, String[] includeCiphers, String[] sslProtocols) throws IOException {
        // Don't use log4j - when this code is called, log4j might not have been initialized
        // and we do not want to initialize log4j at this time because we are likely still
        // running as root.
        System.err.println("Zimbra server reserving server socket port=" + port + " bindaddr=" + address + " ssl=" + ssl);
        String key = makeKey(address, port, ssl, useChannels);
        ServerSocket serverSocket = NetUtil.newBoundServerSocket(address, port, ssl, useChannels, excludeCiphers, includeCiphers, sslProtocols);
        //System.err.println("put table=" + mBoundSockets.hashCode() + " key=" + key + " sock=" + serverSocket);
        mBoundSockets.put(key, serverSocket);
        //dumpMap();
    }

    private static ServerSocket getAlreadyBoundServerSocket(String address, int port, boolean ssl, boolean useChannels) {
        //dumpMap();
        String key = makeKey(address, port, ssl, useChannels);
        ServerSocket serverSocket = mBoundSockets.get(key);
        //System.err.println("get table=" + mBoundSockets.hashCode() + " key=" + key + " sock=" + serverSocket);
        return serverSocket;
    }

    public static void main(String[] args) {
        SSLServerSocketFactory sf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        String[] supportedCipherSuites = sf.getSupportedCipherSuites();
        System.out.println("\nsupported cipher suites:\n");
        for (String c : supportedCipherSuites)
            System.out.println(c);
    }


}
