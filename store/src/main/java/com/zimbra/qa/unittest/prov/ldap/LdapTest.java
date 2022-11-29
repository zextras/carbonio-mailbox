// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import static org.junit.Assert.*;

import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.localconfig.KnownKey;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.ldap.unboundid.InMemoryLdapServer;
import com.zimbra.qa.unittest.prov.ProvTest;

public class LdapTest extends ProvTest {
    private static final String LDAP_TEST_BASE_DOMAIN = "ldaptest";

    // variable guarding initTest() enter only once per JVM
    // if test is triggered from ant test-ldap(-inmem), number of JVM's
    // to fork is controlled by forkmode attr in the <junit> ant element
    private static boolean perJVMInited = false;

    // - handy to set it to "true"/"false" when invoking a single test from inside Eclipse
    // - make sure it is always set to null in p4.
    private static String useInMemoryLdapServerOverride = "true"; // null;  // "true";

    // ensure assertion is enabled
    static {
        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!

        if (!assertsEnabled) {
            throw new RuntimeException("Asserts must be enabled!!!");
        }
    }

    @BeforeClass  // invoked once per class loaded
    public static void beforeClass() throws Exception {
        initPerJVM();
    }

    static String baseDomainName() {
        StackTraceElement [] s = new RuntimeException().getStackTrace();
        return s[1].getClassName().toLowerCase() + "." +
                LDAP_TEST_BASE_DOMAIN + "." + InMemoryLdapServer.UNITTEST_BASE_DOMAIN_SEGMENT;
    }

    public static String genTestId() {
        Date date = new Date();
        SimpleDateFormat fmt =  new SimpleDateFormat("yyyyMMdd-HHmmss");
        return fmt.format(date);
    }

    // invoked once per JVM
    private static synchronized void initPerJVM() throws Exception {
        if (perJVMInited) {
            return;
        }
        perJVMInited = true;

        CliUtil.toolSetup(Log.Level.error.name());
        ZimbraLog.test.setLevel(Log.Level.info);
        // ZimbraLog.acl.setLevel(Log.Level.debug);
        // ZimbraLog.autoprov.setLevel(Log.Level.debug);
        // ZimbraLog.account.setLevel(Log.Level.debug);
        // ZimbraLog.ldap.setLevel(Log.Level.debug);
        // ZimbraLog.soap.setLevel(Log.Level.trace);

        if (useInMemoryLdapServerOverride != null) {
            boolean useInMemoryLdapServer =
                    Boolean.parseBoolean(useInMemoryLdapServerOverride);

            KnownKey key = new KnownKey("debug_use_in_memory_ldap_server",
                    useInMemoryLdapServerOverride);
            if (DebugConfig.useInMemoryLdapServer != useInMemoryLdapServer) {
                System.out.println("useInMemoryLdapServerOverride is " + useInMemoryLdapServerOverride +
                        " but LC key debug_use_in_memory_ldap_server is " + key.value() +
                        ".  Remove the value from LC key.");
                fail();
            }
        }
        ZimbraLog.test.info("useInMemoryLdapServer = " + InMemoryLdapServer.isOn());

        RightManager.getInstance(true);

        Cleanup.deleteAll();
    }

}
