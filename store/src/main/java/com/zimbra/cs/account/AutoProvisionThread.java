// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.util.Zimbra;

public class AutoProvisionThread extends Thread implements Provisioning.EagerAutoProvisionScheduler{

    private static volatile AutoProvisionThread autoProvThread = null;
    private static Object THREAD_CONTROL_LOCK = new Object();
    private boolean shutdownRequested = false;

    @VisibleForTesting
    protected AutoProvisionThread() {
        setName("AutoProvision");
    }

    /**
     * Starts up the auto provision thread.
     */
    public synchronized static void startup() {
        synchronized (THREAD_CONTROL_LOCK) {
            if (isRunning()) {
                ZimbraLog.autoprov.warn("Cannot start a second auto provision thread while another one is running.");
                return;
            }

            if (getSleepInterval() == 0) {
                ZimbraLog.autoprov.info("Not starting auto provision thread because %s is 0.",
                    Provisioning.A_zimbraAutoProvPollingInterval);
                return;
            }

            // Log status
            try {
                String displayInterval = Provisioning.getInstance().getLocalServer().getAttr(
                    Provisioning.A_zimbraAutoProvPollingInterval, null);
                ZimbraLog.autoprov.info("Starting auto provision thread with sleep interval %s.", displayInterval);
            } catch (ServiceException e) {
                ZimbraLog.autoprov.warn("Unable to get %s.  Aborting thread startup.",
                    Provisioning.A_zimbraAutoProvPollingInterval, e);
                return;
            }

            // Start thread
            autoProvThread = new AutoProvisionThread();
            autoProvThread.start();
        }
    }

    /**
     * Returns <tt>true</tt> if the mailbox auto provision thread is currently running.
     */
    public synchronized static boolean isRunning() {
        synchronized (THREAD_CONTROL_LOCK) {
            if (autoProvThread != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Shuts down the auto provision thread.  Does nothing if it is not running.
     */
    public synchronized static void shutdown() {
        synchronized (THREAD_CONTROL_LOCK) {
            if (autoProvThread != null) {
                ZimbraLog.autoprov.info("Shutting down auto provision thread");
                autoProvThread.requestShutdown();
                autoProvThread.interrupt();
                autoProvThread = null;
            } else {
                ZimbraLog.autoprov.info("shutdown() called, but auto provision thread is not running.");
            }
        }
    }
    
    public synchronized static void switchAutoProvThreadIfNecessary() throws ServiceException {
        Server localServer = Provisioning.getInstance().getLocalServer();
        
        long interval = localServer.getTimeInterval(Provisioning.A_zimbraAutoProvPollingInterval, 0);
        
        Set<String> scheduledDomains = 
            localServer.getMultiAttrSet(Provisioning.A_zimbraAutoProvScheduledDomains);
        
        boolean needRunning = interval > 0 && !scheduledDomains.isEmpty();
        
        if (needRunning && !AutoProvisionThread.isRunning()) {
            AutoProvisionThread.startup();
        } else if (!needRunning && AutoProvisionThread.isRunning()) {
            AutoProvisionThread.shutdown();
        }
    }
    
    /**
     * Provision accounts for all domain scheduled for auto provision on this server, 
     * sleep for configured amount of time between iterations.
     * 
     * If an iteration takes longer than the sleep interval, the next iteration will start 
     * immediately.
     */
    @Override public void run() {
        // Sleep before doing work, to give the server time to warm up.  Also limits the amount
        // of random effect when determining the next mailbox id.
        long sleepTime = getInitialDelay();
        ZimbraLog.autoprov.info("Auto provision thread sleeping for %dms before doing work.", sleepTime);

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            ZimbraLog.autoprov.info("Shutting down auto provision thread.");
            autoProvThread = null;
            return;
        }

        Provisioning prov = Provisioning.getInstance();

        while (true) {
            if (isShutDownRequested()) {
                ZimbraLog.autoprov.info("Shutting down auto provision thread.");
                return;
            }
            
            long iterStartedAt = System.currentTimeMillis();
            
            try {
                prov.autoProvAccountEager(this);
            } catch (Throwable t) {
                if (t instanceof OutOfMemoryError) {
                    Zimbra.halt("Ran out of memory while auto provision accounts", t);
                } else {
                    ZimbraLog.autoprov.warn("Unable to auto provision accounts", t);
                }
            }
            
            long iterEndedAt = System.currentTimeMillis();
            long elapsedMillis = iterEndedAt - iterStartedAt;

            // If this iteration took longer than the sleep interval, 
            // go back to work immediately without sleeping
            long sleepInterval = getSleepInterval();
            if (sleepInterval == 0) {
                // a shutdown request was issued
            } else if (elapsedMillis < sleepInterval) {
                sleep();
            } else {
                ZimbraLog.autoprov.debug("Iteration took %d seconds, starting next iteration immediately without sleeping", 
                        elapsedMillis/Constants.MILLIS_PER_SECOND);   
            }
        }
    }

    @VisibleForTesting
    protected long getInitialDelay() {
        return LC.autoprov_initial_sleep_ms.longValue();
    }
    
    /**
     * Sleeps for the time interval specified by {@link Provisioning#A_zimbraAutoProvPollingInterval}.
     * If sleep is interrupted, sets {@link #mShutdownRequested} to <tt>true</tt>.
     */
    private void sleep() {
        long interval = getSleepInterval();
        ZimbraLog.autoprov.info("Sleeping for %d milliseconds.", interval);

        if (interval > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                ZimbraLog.autoprov.info("Auto provision thread was interrupted.");
                shutdownRequested = true;
            }
        } else {
            shutdownRequested = true;
        }
    }
    
    private void requestShutdown() {
        shutdownRequested = true;
    }
    
    @Override
    public boolean isShutDownRequested() {
        return shutdownRequested;
    }
    
    /**
     * Stores the sleep interval, so that the auto provision thread doesn't
     * die if there's a problem talking to LDAP.  See bug 32639.
     */
    private static long sleepInterval = 0;
    
    /**
     * Returns the current value of {@link Provisioning#A_zimbraAutoProvPollingInterval},
     * or <tt>0</tt> if it cannot be determined.
     */
    private static long getSleepInterval() {
        try {
            Server server = Provisioning.getInstance().getLocalServer();
            sleepInterval = server.getTimeInterval(Provisioning.A_zimbraAutoProvPollingInterval, 0);
        } catch (ServiceException e) {
            ZimbraLog.autoprov.warn("Unable to determine value of %s.  Using previous value: %d.",
                Provisioning.A_zimbraAutoProvPollingInterval, sleepInterval, e);
        }
        
        return sleepInterval;
    }


}
