// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.stats.StatsDumperDataSource;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;

/**
 * Returns thread count statistics.  Threads whose names start with
 * known prefixes are counted together.  All other threads are grouped
 * in the "other" category.
 */
public class ThreadStats
implements StatsDumperDataSource
{
    private String mFilename;

    /**
     * Creates a new <tt>ThreadStats</tt> object.
     *  
     * @param filename CSV file that stats are written to
     */
    public ThreadStats(String filename) {
        mFilename = filename;
    }
    
    public String getFilename() {
        return mFilename; 
    }
    
    public String getHeader() {
        return StringUtil.join(",", getThreadNamePrefixes()) + ",other,total";
    }

    /**
     * Returns a single line of data.  Each column corresponds to a known
     * thread name prefix.  The last two columns are "other" and "total".
     */
    public Collection<String> getDataLines() {
        // Tally threads by name
        String[] threadNamePrefixes = getThreadNamePrefixes();
        Map<String, Integer> threadCount = new LinkedHashMap<String, Integer>();
        for (String prefix : threadNamePrefixes) {
            threadCount.put(prefix, 0);
        }
        threadCount.put("other", 0);
        
        // Find the root thread group
        ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
        while (root.getParent() != null) {
            root = root.getParent();
        }
        
        // Iterate all threads and increment counters
        Thread[] threads = new Thread[root.activeCount() * 2];
        int numThreads = root.enumerate(threads, true);
        for (int i = 0; i < numThreads; i++) {
            Thread thread = threads[i];
            String threadName = thread.getName();
            boolean found = false;
            for (String prefix : threadNamePrefixes) {
                // Increment count for the prefix if found
                if (threadName != null && threadName.startsWith(prefix)) {
                    threadCount.put(prefix, threadCount.get(prefix) + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                threadCount.put("other", threadCount.get("other") + 1);
            }
        }
        
        StringBuilder buf = new StringBuilder();
        for (String prefix : threadCount.keySet()) {
            buf.append(threadCount.get(prefix));
            buf.append(',');
        }
        buf.append(numThreads);
        
        // Return value
        List<String> retVal = new ArrayList<String>(1);
        retVal.add(buf.toString());
        return retVal;
    }

    public boolean hasTimestampColumn() {
        return true;
    }
    
    private String[] getThreadNamePrefixes() {
        try {
            return Provisioning.getInstance().getLocalServer().getStatThreadNamePrefix();
        } catch (ServiceException e) {
            ZimbraLog.perf.warn("Unable to determine thread name prefixes.", e);
            return new String[0];
        }
    }
}