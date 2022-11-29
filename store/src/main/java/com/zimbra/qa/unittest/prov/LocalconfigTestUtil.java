// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.zimbra.common.localconfig.KnownKey;

public class LocalconfigTestUtil {
    
    public static void modifyLocalConfig(KnownKey key, String value) throws Exception {
        String keyName = key.key();
        
        Process process = null;
        try {
            String command = "/opt/zextras/bin/zmlocalconfig -e " + keyName + "=" + value;
            System.out.println(command);
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } 
        
        int exitCode;
        try {
            exitCode = process.waitFor();
            assertEquals(0, exitCode);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        } 
    }
    
    public static void modifyLocalConfigTransient(KnownKey key, String value) {
        key.setDefault(value);
    }
}
