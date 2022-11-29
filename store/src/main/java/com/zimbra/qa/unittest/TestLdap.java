// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.IOException;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.google.common.collect.Lists;
import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.localconfig.KnownKey;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.util.Log;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.ldap.LdapDIT;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapConstants;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.ldap.ZSearchControls;
import com.zimbra.cs.ldap.ZSearchResultEntry;
import com.zimbra.cs.ldap.ZSearchResultEnumeration;
import com.zimbra.cs.ldap.ZSearchScope;
import com.zimbra.cs.ldap.unboundid.InMemoryLdapServer;
import com.zimbra.qa.unittest.prov.ldap.TestAccountLockout;
import com.zimbra.qa.unittest.prov.ldap.TestAutoProvision;
import com.zimbra.qa.unittest.prov.ldap.TestLdapBinary;
import com.zimbra.qa.unittest.prov.ldap.TestLdapConnection;
import com.zimbra.qa.unittest.prov.ldap.TestLdapHelper;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvAccount;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvAlias;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvAttrCallback;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvCos;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvDIT;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvDataSource;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvDistributionList;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvDomain;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvDynamicGroup;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvEntry;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvExternalLdapAuth;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvGal;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvGlobalConfig;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvGlobalGrant;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvIdentity;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvMimeType;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvMisc;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvModifyAttrs;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvRenameDomain;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvSearchDirectory;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvServer;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvSignature;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvXMPPComponent;
import com.zimbra.qa.unittest.prov.ldap.TestLdapProvZimlet;
import com.zimbra.qa.unittest.prov.ldap.TestLdapUpgrade;
import com.zimbra.qa.unittest.prov.ldap.TestLdapUtil;
import com.zimbra.qa.unittest.prov.ldap.TestLdapZLdapContext;
import com.zimbra.qa.unittest.prov.ldap.TestLdapZLdapFilter;
import com.zimbra.qa.unittest.prov.ldap.TestLdapZMutableEntry;
import com.zimbra.qa.unittest.prov.ldap.TestProvAlias;
import com.zimbra.qa.unittest.prov.ldap.TestProvAttr;
import com.zimbra.qa.unittest.prov.ldap.TestProvCallbackAvailableZimlets;
import com.zimbra.qa.unittest.prov.ldap.TestProvIDN;
import com.zimbra.qa.unittest.prov.ldap.TestProvValidator;
import com.zimbra.qa.unittest.prov.ldap.TestProvZimbraId;
import com.zimbra.qa.unittest.prov.soap.TestACLUserRights;
import com.zimbra.qa.unittest.prov.soap.TestDelegatedDL;
import com.zimbra.qa.unittest.prov.soap.TestSearchGal;

public class TestLdap {
    private static final String TEST_LDAP_BASE_DOMAIN = InMemoryLdapServer.UNITTEST_BASE_DOMAIN_SEGMENT; // "testldap";
    
    // variable guarding initTest() enter only once per JVM
    // if test is triggered from ant test-ldap(-inmem), number of JVM's
    // to fork is controlled by forkmode attr in the <junit> ant element
    private static boolean perJVMInited = false;
    
    // - handy to set it to "true"/"false" when invoking a single test from inside Eclipse
    // - make sure it is always set to null in p4. 
    private static String useInMemoryLdapServerOverride = null; // "true";
    
    // ensure assertion is enabled
    static {
        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!

        if (!assertsEnabled) {
            throw new RuntimeException("Asserts must be enabled!!!");
        }

    } 
    
    static void modifyLocalConfig(String key, String value) throws Exception {
        Process process = null;
        try {
            String command = "/opt/zextras/bin/zmlocalconfig -e " + key + "=" + value;
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

    enum TestConfig {
        UBID(com.zimbra.cs.ldap.unboundid.UBIDLdapClient.class, com.zimbra.cs.account.ldap.LdapProvisioning.class);
        
        static private TestConfig currentTestConfig = null;
        
        private Class ldapClientClass;
        private Class ldapProvClass;
        
        private TestConfig(Class ldapClientClass, Class ldapProvClass) {
            this.ldapClientClass = ldapClientClass;
            this.ldapProvClass = ldapProvClass;
        }
        
        static synchronized void useConfig(TestConfig config) throws Exception {
            if (currentTestConfig != null) {
                fail("TestConfig.useConfig can only be called once per JVM");
            }
            
            currentTestConfig = config;
            
            // support UBID only for now
            // assert(TestConfig.UBID == config);

            if (config.ldapClientClass != null) {
                modifyLocalConfig(LC.zimbra_class_ldap_client.key(), config.ldapClientClass.getCanonicalName());
            } else {
                // remove the key
                modifyLocalConfig(LC.zimbra_class_ldap_client.key(), "");
            }
            modifyLocalConfig(LC.zimbra_class_provisioning.key(), config.ldapProvClass.getCanonicalName());
            LC.reload();
        }
        
        static synchronized TestConfig getCurrentTestConfig() {
            if (currentTestConfig == null) {
                fail("TestConfig has not been initialized");
            }
            
            return currentTestConfig;
        }
        
    }

    
    /*
     * given a domain name like test.com, delete the entire tree under 
     * dc=com in LDAP
     */
    public static void deleteEntireBranch(String domainName) throws Exception {
        String parts[] = domainName.split("\\.");
        String[] dns = ((LdapProv) Provisioning.getInstance()).getDIT().domainToDNs(parts);
        String topMostRDN = dns[dns.length-1];
        TestLdap.deleteEntireBranchByDN(topMostRDN);
        
        cleanupAll();
    }
    
    private static void cleanupAll() throws Exception {
        deleteAllNonDefaultCoses();
        deleteAllNonDefaultServers();
        deleteAllXMPPComponents();
    }
    
    private static void deleteAllNonDefaultCoses() throws Exception {
        LdapDIT dit = ((LdapProv) Provisioning.getInstance()).getDIT();
        String cosBaseDN = dit.cosBaseDN();
        
        Set<String> defaultCosDN = new HashSet<String>();
        defaultCosDN.add(dit.cosNametoDN(Provisioning.DEFAULT_COS_NAME));
        defaultCosDN.add(dit.cosNametoDN(Provisioning.DEFAULT_EXTERNAL_COS_NAME));

        deleteAllChildrenUnderDN(cosBaseDN, defaultCosDN);
    }
    
    private static void deleteAllNonDefaultServers() throws Exception {
        LdapProv ldapProv = LdapProv.getInst();
        LdapDIT dit = ldapProv.getDIT();
        String serverBaseDN = dit.serverBaseDN();
        
        Set<String> defaultServerDN = new HashSet<String>();
        defaultServerDN.add(dit.serverNameToDN(ldapProv.getLocalServer().getName()));
        
        deleteAllChildrenUnderDN(serverBaseDN, defaultServerDN);
    }
    
    private static void deleteAllXMPPComponents() throws Exception {
        String xmppBaseDN = ((LdapProv) Provisioning.getInstance()).getDIT().xmppcomponentBaseDN();
        deleteAllChildrenUnderDN(xmppBaseDN, null);
    }

    
    // dn itself will also be deleted
    private static void deleteEntireBranchByDN(String dn) throws Exception {
        ZLdapContext zlc = null;
        
        try {
            zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UNITTEST);
            deleteEntireBranch(zlc, dn);
        } finally {
            LdapClient.closeContext(zlc);
        }
    }
    
    // dn itself will not be deleted
    private static void deleteAllChildrenUnderDN(String dn, Set<String> ignoreDNs) throws Exception {
        
        ZLdapContext zlc = null;
        try {
            zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UNITTEST);
            List<String> childrenDNs = getDirectChildrenDNs(zlc, dn);
            for (String childDN : childrenDNs) {
                if (ignoreDNs == null || !ignoreDNs.contains(childDN)) {
                    deleteEntireBranch(zlc, childDN);
                }
            }
        } finally {
            LdapClient.closeContext(zlc);
        }
        
    }
    
    private static void deleteEntireBranch(ZLdapContext zlc, String dn) throws Exception {
        
        if (isLeaf(zlc, dn)) {
            deleteEntry(dn);
            return;
        }
        
        List<String> childrenDNs = getDirectChildrenDNs(zlc, dn);
        for (String childDN : childrenDNs) {
            deleteEntireBranch(zlc, childDN);
        }
        deleteEntry(dn);
    }
    
    private static void deleteEntry(String dn) throws Exception {
        ZLdapContext zlc = null;
        try {
            zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UNITTEST);
            zlc.deleteEntry(dn);
        } finally {
            LdapClient.closeContext(zlc);
        }
    }
    
    private static boolean isLeaf(ZLdapContext zlc, String dn) throws Exception {
        return getDirectChildrenDNs(zlc, dn).size() == 0;
    }
    
    private static List<String> getDirectChildrenDNs(ZLdapContext zlc, String dn) throws Exception {
        final List<String> childrenDNs = new ArrayList<String>();

        ZLdapFilter filter = ZLdapFilterFactory.getInstance().anyEntry();
        
        ZSearchControls searchControls = ZSearchControls.createSearchControls(
                ZSearchScope.SEARCH_SCOPE_ONELEVEL, 
                ZSearchControls.SIZE_UNLIMITED, new String[]{"objectClass"});
        
        ZSearchResultEnumeration sr = zlc.searchDir(dn, filter, searchControls);
        while (sr.hasMore()) {
            ZSearchResultEntry entry = sr.next();
            childrenDNs.add(entry.getDN());
        }
        sr.close();
        
        return childrenDNs;
    }
    
    /**
     * Given a name (which is to be turn into a DN), mix in chars 
     * defined in rfc2253.txt that need to be escaped in RDN value.
     * 
     * http://www.ietf.org/rfc/rfc2253.txt?number=2253
     * 
     * - a space or "#" character occurring at the beginning of the
     *   string
     *
     * - a space character occurring at the end of the string
     *
     * - one of the characters ",", "+", """, "\", "<", ">" or ";"
     * 
     * Implementations MAY escape other characters.
     *
     * If a character to be escaped is one of the list shown above, then it
     * is prefixed by a backslash ('\' ASCII 92).
     *
     * Otherwise the character to be escaped is replaced by a backslash and
     * two hex digits, which form a single byte in the code of the
     * character.
     * 
     * @param name
     * @return
     */    
    private static String makeRFC2253Name(String name, boolean wantTrailingBlank) {
        String LEADING_CHARS = "#";
        String TRAILING_CHARS = " ";
        String BACKSLASH_ESCAPED_CHARS = "# ,+\"\\<>;";
        String UNICODE_CHARS = "\u4e2d\u6587";
        
        if (wantTrailingBlank) {
            return LEADING_CHARS + BACKSLASH_ESCAPED_CHARS + DOT_ATOM_CHARS + UNICODE_CHARS + "---" + name + TRAILING_CHARS;
        } else {
            return LEADING_CHARS + BACKSLASH_ESCAPED_CHARS + DOT_ATOM_CHARS + UNICODE_CHARS + "---" + name;
        }
    }
    
    // RFC 2822
    private static final String ATOM_CHARS = "!#$%&'*+-/=?^_`{|}~";   
    private static final String DOT_ATOM_CHARS = "." + ATOM_CHARS;
    
    private static String makeRFC2253NameEmailLocalPart(String name) {
        String LEADING_CHAR = "#";
        return LEADING_CHAR + DOT_ATOM_CHARS + "---" + name;
    }
    
    private static String makeRFC2253NameDomainName(String name) {
        String UNICODE_CHARS = "\u4e2d\u6587";
        
        // hmm, javamail does not like any of the ATOM_CHARS 
        return /* ATOM_CHARS + */ UNICODE_CHARS + "---" + name;
    }

    static String makeAccountNameLocalPart(String localPart) {
        return makeRFC2253NameEmailLocalPart(localPart);
    }

    static String makeAliasNameLocalPart(String localPart) {
        return makeRFC2253NameEmailLocalPart(localPart);
    }

    static String makeCosName(String name) {
        return makeRFC2253Name(name, false);
    }

    static String makeDataSourceName(String name) {
        // historically we allow trailing blank in data source name
        // should probably make it consistent across the board.
        return makeRFC2253Name(name, true);
    }
    
    static String makeDLNameLocalPart(String localPart) {
        return makeRFC2253NameEmailLocalPart(localPart);
    }
    
    static String makeDomainName(String name) {
        return TestLdap.makeRFC2253NameDomainName(name);
    }
    
    static String makeIdentityName(String name) {
        // historically we allow trailing blank in identity name
        // should probably make it consistent across the board.
        return makeRFC2253Name(name, true);
    }
    
    static String makeServerName(String name) {
        return makeRFC2253Name(name, false);
    }
    
    static String makeSignatureName(String name) {
        return makeRFC2253Name(name, false);
    }
    
    static String makeXMPPName(String name) {
        return makeRFC2253Name(name, false);
    }
    
    static String makeZimletName(String name) {
        return makeRFC2253Name(name, false);
    }
    

    private static boolean batchMode = false;
    
    //
    // TODO: merge with LdapSuite
    //
    private static void runTests(JUnitCore junit, TestConfig testConfig) throws Exception {
        batchMode = true;
         
        initPerJVM(testConfig);
        
        Date startTime = new Date();
        SimpleDateFormat dateFmt = new SimpleDateFormat("HH:mm:ss");
        System.out.println("TestLdap started at: " + dateFmt.format(startTime));
        
        if (testConfig == TestConfig.UBID) {
            // junit.run(TestLdapSDK.class);
        }
        
        // List<Class<? extends TestLdap>> classes = Lists.newArrayList();
        List<Class> classes = Lists.newArrayList();
        
        /*
         * Tests not running inside the server
         */
        classes.add(TestLdapHelper.class);
        classes.add(TestLdapConnection.class);
        classes.add(TestLdapProvAccount.class);
        classes.add(TestLdapProvAlias.class);
        classes.add(TestLdapProvAttrCallback.class);
        classes.add(TestAutoProvision.class);
        classes.add(TestLdapProvCos.class);
        classes.add(TestLdapProvDataSource.class);
        classes.add(TestLdapProvDistributionList.class);
        classes.add(TestLdapProvDIT.class);
        classes.add(TestLdapProvDomain.class);
        classes.add(TestLdapProvDynamicGroup.class);
        classes.add(TestLdapProvEntry.class);
        classes.add(TestLdapProvExternalLdapAuth.class);
        classes.add(TestLdapProvGal.class);
        classes.add(TestLdapProvGlobalConfig.class);
        classes.add(TestLdapProvGlobalGrant.class);
        classes.add(TestLdapProvIdentity.class);
        classes.add(TestLdapProvMimeType.class);
        classes.add(TestLdapProvMisc.class);
        classes.add(TestLdapProvModifyAttrs.class);
        classes.add(TestLdapProvRenameDomain.class);
        classes.add(TestLdapProvServer.class);
        classes.add(TestLdapProvSignature.class);
        classes.add(TestLdapProvSearchDirectory.class);
        classes.add(TestLdapProvXMPPComponent.class);
        classes.add(TestLdapProvZimlet.class);
        classes.add(TestLdapUtil.class);
        classes.add(TestLdapUpgrade.class);
        classes.add(TestLdapZLdapContext.class);
        classes.add(TestLdapZLdapFilter.class);
        classes.add(TestLdapZMutableEntry.class);
        
        
        // Tests need server running
        classes.add(TestDelegatedDL.class);
        
        // old tests, TODO: convert them 
        classes.add(TestAccountLockout.class);
        // classes.add(TestACPermissionCache.class);
        classes.add(TestACLUserRights.class);
        classes.add(TestBuildInfo.class);
        classes.add(TestLdapBinary.class);
        classes.add(TestLdapUtil.class);
        classes.add(TestProvAlias.class);
        classes.add(TestProvAttr.class);
        classes.add(TestProvCallbackAvailableZimlets.class);
        classes.add(TestProvIDN.class);
        classes.add(TestProvValidator.class);
        classes.add(TestProvZimbraId.class);
        classes.add(TestSearchGal.class);
        
        /*
         * tests in extensions - don't forget to run them:
         * 
         * NginxLookupExtensionTest
         * TestGetSMIMEPublicCerts
         * TestLdapSMIMELookup
         */
        
        /*
         * ACL tests
         * 
         * TestACBasic
         * TestACEffectiveRights
         * TestACLAttrRight
         * TestACLGrant
         * TestACLGrantee
         * TestACLPrecedence
         * TestACLRight
         * TestACLTarget
         * TestACPermissionCache
         * TestACUserRights
         * 
         */
        /*  // tests need fixing
        junit.run(TestCreateAccount.class);
        junit.run(TestProvDomainStatus.class);
        */
        
        Class[] classArray = classes.toArray(new Class[classes.size()]);
        Result result = junit.run(classArray);
        
        System.out.println("===== Number of Failures: " + result.getFailureCount());
        List<Failure> failures = result.getFailures();
        for (Failure failure : failures) {
            System.out.println(failure.toString());
            System.out.println();
        }
        System.out.println();

        
        Date endTime = new Date();
        System.out.println("TestLdap ended at:   " + dateFmt.format(endTime));
        
        long elapsedMills = endTime.getTime() - startTime.getTime();
        long minutes = elapsedMills / Constants.MILLIS_PER_MINUTE;
        long seconds = elapsedMills / Constants.MILLIS_PER_SECOND;
        long secs = seconds - (minutes * Constants.SECONDS_PER_MINUTE);
        System.out.println();
        System.out.println("Elapsed time: " + minutes + " mins " + secs + " secs");
    }
    
    // invoked once per JVM
    private static void initPerJVM(TestConfig testConfig) throws Exception {
        if (perJVMInited) {
            return;
        }
        perJVMInited = true;
        
        CliUtil.toolSetup(Log.Level.error.name());
        ZimbraLog.test.setLevel(Log.Level.info);
        ZimbraLog.autoprov.setLevel(Log.Level.debug);
        // ZimbraLog.account.setLevel(Log.Level.debug);
        // ZimbraLog.ldap.setLevel(Log.Level.debug);
        // ZimbraLog.soap.setLevel(Log.Level.trace);

        /*
        if (useInMemoryLdapServerProperty == null) {
            useInMemoryLdapServerProperty = 
                System.getProperty("use_in_memory_ldap_server", "false");
        }
        
        boolean useInMemoryLdapServer = 
            Boolean.parseBoolean(useInMemoryLdapServerProperty);
        
        KnownKey key = new KnownKey("debug_use_in_memory_ldap_server", 
                useInMemoryLdapServerProperty);
        assert(DebugConfig.useInMemoryLdapServer == useInMemoryLdapServer);
        useInMemoryLdapServer = InMemoryLdapServer.isOn();
        
        ZimbraLog.test.info("useInMemoryLdapServer = " + useInMemoryLdapServer);
        
        if (useInMemoryLdapServer) {
            try {
                InMemoryLdapServer.start(InMemoryLdapServer.ZIMBRA_LDAP_SERVER, 
                        new InMemoryLdapServer.ServerConfig(
                        Lists.newArrayList(LdapConstants.ATTR_DC + "=" + TEST_LDAP_BASE_DOMAIN)));
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        */
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
        TestConfig.useConfig(testConfig);
        
        cleanupAll();

    }
    
    @BeforeClass  // invoked once per class loaded
    public static void beforeClass() throws Exception {
        if (batchMode) {
            return;
        }
        
        TestConfig testConfig = TestConfig.UBID;
        
        initPerJVM(testConfig);
    }
    
    static String baseDomainName(Class<? extends TestLdap> klass) {
        return klass.getName().toLowerCase() + "." + TEST_LDAP_BASE_DOMAIN;
    }
    
    static TestConfig getCurrentTestConfig() {
        return TestConfig.getCurrentTestConfig();
    }
    
    /*
     * zmjava -ea com.zimbra.qa.unittest.TestLdap > ~/temp/out.txt
     * 
     * cp -f /Users/pshao/p4/main/ZimbraServer/data/unittest/ldap/attrs-unittest.xml /Users/pshao/p4/main/ZimbraServer/conf/attrs
     * ant refresh-ldap-schema
     * 
     * cp -f /Users/pshao/p4/main/ZimbraServer/data/unittest/ldap/rights-unittest.xml /opt/zextras/conf/rights
     * 
     */
    public static void main(String[] args) throws Exception {
        JUnitCore junit = new JUnitCore();
        // junit.addListener(new ConsoleListener());
        junit.addListener(new TestLogger());
        
        TestConfig.useConfig(TestConfig.UBID);
        
        // runTests(junit, TestConfig.UBID);
        
        System.out.println();
        System.out.println("=== Finished ===");
    }

}
