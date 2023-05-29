// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.perf.chart;

public interface SummaryConstants {
    
    public static final String SUMMARY_TXT = "summary.txt";
    public static final String SUMMARY_CSV = "summary.csv";
    
    public static final String ADD_COUNT = "mbox_add_msg_count";

    public static final String ADD_LATENCY = "mbox_add_msg_ms_avg";

    public static final String GET_LATENCY = "mbox_get_ms_avg";

    public static final String SOAP_RESPONSE = "soap_ms_avg";

    public static final String POP_RESPONSE = "pop_ms_avg";

    public static final String IMAP_RESPONSE = "imap_ms_avg";
    
   //    server  MEM gc time
    public static final String GC_IN = "gc.csv";
    
    public static final String SERVER_IN = "zimbrastats.csv";
    
    public static final String CPU_IN = "proc.csv";
    
    public static final String IO_IN = "io-x.csv";    

    public static final String F_GC = "FULLGC%";

    public static final String Y_GC = "YGC%";    

   // server CPU
    
    public static final String USER = "user";

    public static final String SYS = "sys";

    public static final String IDLE = "idle";

    public static final String IOWAIT = "iowait";

   // Server IO     

    public static final String KEY_TO_DISK_UTIL = "disk_util.png";

    public static final String W_THROUGHPUT = "wkB/s";

    public static final String R_THROUGHPUT = "rkB/s";

    public static final String R_IOPS = "r/s";

    public static final String W_IOPS = "w/s";
    
    public static final String IO_UTIL = "%util";
}
