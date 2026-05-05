// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.Provisioning;

/**
 * Base class for TCP servers using thread per connection model.
 */
public abstract class TcpServer implements Runnable, Server {
    private Log log;
    private ExecutorService pooledExecutor;
    private final AtomicInteger activeThreadCount = new AtomicInteger(0);
    private ServerSocket serverSocket;
    private List<ProtocolHandler> activeHandlers;
    private boolean sslEnabled;
    private final ServerConfig config;
    private volatile boolean shutdownRequested;

    public TcpServer(ServerConfig config) throws ServiceException {
        this.config = config;
        this.sslEnabled = config.isSslEnabled();
        init(config.getMaxThreads(), config.getServerSocket());
    }

    public TcpServer(int maxThreads, ServerSocket serverSocket) {
        config = null;
        init(maxThreads, serverSocket);
    }

    private void init(int maxThreads, ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        log = LogFactory.getLog(TcpServer.class.getName() + "/" + serverSocket.getLocalPort());

        if (maxThreads <= 0) {
            log.warn("max handler threads " + maxThreads + " invalid; will use 10 threads instead");
            maxThreads = 10;
        }

        // Virtual threads: one cheap virtual thread per connection; no pool ceiling needed.
        // Each connection blocks on I/O and is exactly the pattern Loom was designed for.
        ThreadFactory vtf = Thread.ofVirtual()
            .name(getName() + "-vt-", 0)
            .factory();
        pooledExecutor = Executors.newThreadPerTaskExecutor(vtf);

        // TODO a linked list is probably the wrong datastructure here
        // TODO write tests with multiple concurrent client
        // TODO write some tests for shutdown/startup
        activeHandlers = new LinkedList<>();
    }

    @Override
    public ServerConfig getConfig() {
        return config;
    }

    public int getConfigMaxIdleMilliSeconds() {
        if (config != null) {
            int secs = config.getMaxIdleTime();
            if (secs >= 0) {
                return secs * 1000;
            }
        }
        return -1;
    }

    protected void setSslEnabled(boolean ssl) {
        sslEnabled = ssl;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void addActiveHandler(ProtocolHandler handler) {
        synchronized (activeHandlers) {
            activeHandlers.add(handler);
        }
    }

    public void removeActiveHandler(ProtocolHandler handler) {
        synchronized (activeHandlers) {
            activeHandlers.remove(handler);
        }
    }

    protected int numActiveHandlers() {
        synchronized (activeHandlers) {
            return activeHandlers.size();
        }
    }

    public int numThreads() {
        return activeThreadCount.get();
    }

    private void shutdownActiveHandlers(boolean graceful) {
        synchronized (activeHandlers) {
            for (ProtocolHandler handler : activeHandlers) {
                if (graceful) {
                    handler.gracefulShutdown("graceful shutdown requested");
                } else {
                    handler.hardShutdown("hard shutdown requested");
                }
            }
        }
    }

    @Override
    public void start() {
        Thread thread = new Thread(this);
        thread.setName(getName());
        thread.start();
    }

    @Override
    public void stop() {
        stop(config.getShutdownTimeout());
    }

    @Override
    public void stop(int forceShutdownAfterSeconds) {
        log.info(getName() + " initiating shutdown");
        shutdownRequested = true;

        try {
            serverSocket.close();
            Thread.yield();
        } catch (IOException ioe) {
            log.warn(getName() + " error closing server socket", ioe);
        }

        pooledExecutor.shutdown();

        shutdownActiveHandlers(true);

        if (numActiveHandlers() == 0) {
            log.info(getName() + " shutting down idle thread pool");
            pooledExecutor.shutdownNow();
            return;
        }

        log.info(getName() + " waiting " + forceShutdownAfterSeconds + " seconds for thread pool shutdown");
        try {
            pooledExecutor.awaitTermination(forceShutdownAfterSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            log.warn(getName() + " interrupted while waiting for graceful shutdown", ie);
        }

        if (numActiveHandlers() == 0) {
            log.info(getName() + " thread pool terminated");
            return;
        }

        shutdownActiveHandlers(false);

        log.info(getName() + " shutdown complete");
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getName());

        log.info("Starting accept loop with virtual threads.");

        while (!shutdownRequested) {
            try {
                Socket connection = serverSocket.accept();
                ProtocolHandler handler = newProtocolHandler();
                handler.setConnection(connection);
                pooledExecutor.execute(() -> {
                    activeThreadCount.incrementAndGet();
                    try {
                        handler.run();
                    } finally {
                        activeThreadCount.decrementAndGet();
                    }
                });
            } catch (Throwable e) {
                if (e instanceof SocketException && shutdownRequested) {
                    break; // ignore SocketException: Socket closed
                }
                log.error("accept loop failed", e);
                try {
                    Thread.sleep(1000); // pause for 1 second
                } catch (InterruptedException ignore) {
                }
            }
        }

        log.info("finished accept loop");
    }

    // warnIfNecessary removed: virtual threads have no fixed pool ceiling,
    // so utilization percentage is not meaningful. Active connection count
    // is tracked via activeThreadCount and exposed through numThreads().

    protected abstract ProtocolHandler newProtocolHandler();

    protected Set<String> getThrottleSafeHosts() throws ServiceException {

        Set<String> safeHosts = new HashSet<>();
        for (com.zimbra.cs.account.Server server : Provisioning.getInstance().getAllServers()) {
            safeHosts.add(server.getServiceHostname());
        }
      safeHosts.addAll(Arrays.asList(config.getThottleIgnoredHosts()));
        return safeHosts;
    }

    protected Set<String> getThrottleWhitelist() throws ServiceException {

        Set<String> safeHosts = new HashSet<>();
      safeHosts.addAll(Arrays.asList(config.getThrottleWhitelist()));
        return safeHosts;
    }
}
