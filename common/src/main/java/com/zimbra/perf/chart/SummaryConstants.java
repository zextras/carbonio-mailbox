// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.perf.chart;

public interface SummaryConstants {
    
    String SUMMARY_TXT = "summary.txt";
    String SUMMARY_CSV = "summary.csv";
    
    String ADD_COUNT = "mbox_add_msg_count";

    String ADD_LATENCY = "mbox_add_msg_ms_avg";

    String GET_LATENCY = "mbox_get_ms_avg";

    String SOAP_RESPONSE = "soap_ms_avg";

    String POP_RESPONSE = "pop_ms_avg";

    String IMAP_RESPONSE = "imap_ms_avg";
    
   //    server  MEM gc time
   String GC_IN = "gc.csv";
    
    String SERVER_IN = "zimbrastats.csv";
    
    String CPU_IN = "proc.csv";
    
    String IO_IN = "io-x.csv";

    String F_GC = "FULLGC%";

    String Y_GC = "YGC%";

   // server CPU
    
    String USER = "user";

    String SYS = "sys";

    String IDLE = "idle";

    String IOWAIT = "iowait";

   // Server IO     

    String KEY_TO_DISK_UTIL = "disk_util.png";

    String W_THROUGHPUT = "wkB/s";

    String R_THROUGHPUT = "rkB/s";

    String R_IOPS = "r/s";

    String W_IOPS = "w/s";
    
    String IO_UTIL = "%util";
}
