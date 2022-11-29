// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPConnectionPoolStatistics;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.CliUtil;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZAttributes;
import com.zimbra.cs.ldap.unboundid.UBIDLdapContext;
import com.zimbra.qa.unittest.prov.LocalconfigTestUtil;


// Note: do not extend LdapTest, because LdapClient will be initialized in LdapTest.init(),
// which we don't want.   In this class, each test needs to init and shutdown LdapClient.
public class TestLdapConnectivity {

    /*
     * ensure assertion is disabled, otherwise will choke on:
     * UBIDLdapFilterFactory.initialize
     * ZLdapFilterFactory.setInstance
     */
    static {
        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!

        if (assertsEnabled) {
            throw new RuntimeException("Asserts must NOT be enabled!!!");
        }
    }

    /*
     * To run this test:
       1. To enable TLS, modifying /opt/zextras/conf/slapd.conf to uncomment the three lines:
          TLSCertificateFile /opt/zextras/conf/slapd.crt
          TLSCertificateKeyFile /opt/zextras/conf/slapd.key
          TLSVerifyClient never

       2. To listen to ldaps
          edit /opt/zextras/bin/ldap
          modify the line:

          sudo /opt/zextras/libexec/zmslapd -l LOCAL0 -4 -u `whoami` -h "ldap://:389/" \
                        -f /opt/zextras/conf/slapd.conf

          to:

          sudo /opt/zextras/libexec/zmslapd -l LOCAL0 -4 -u `whoami` -h "ldap://:389/ ldaps://:636/" \
                        -f /opt/zextras/conf/slapd.conf

        StartTLS and ldaps cannot co-exist in production, because if ldap url contains ldaps,
        then startTLS will never be used regardless of the LC keys.

        We can run all protocols(ldap, ldaps, startTLS) in one VM in this class because
        LdapClient.initialize()/LdapClient.shutdown() is called before/after each test.

     *
     * Note: ssl_allow_mismatched_certs behavior is different in ZimbraLdapContext(JNDI)
     *       and unboundid.
     *
     *       (ssl_allow_mismatched_certs only applies to STRATTLS, *not* ldaps.)
     *
     *       Legacy ZimbraLdapContext allows it only when ssl_allow_mismatched_certs
     *       is true.  If ssl_allow_mismatched_certs is false, JNDI throws:
     *       Caused by: java.security.cert.CertificateException: No name matching localhost found
     *   at sun.security.util.HostnameChecker.matchDNS(HostnameChecker.java:210)
     *   at sun.security.util.HostnameChecker.match(HostnameChecker.java:77)
     *   at com.sun.jndi.ldap.ext.StartTlsResponseImpl.verify(StartTlsResponseImpl.java:416)
     *
     *       because JNDI's StartTlsResponseImpl always verifies the hostname in the ldap server
     *       certificate against the ldap URL after the SSL handshake is done.
     *       (http://download.oracle.com/javase/jndi/tutorial/ldap/ext/starttls.html)
     *       If we don't set a custom HostnameVerifier, StartTlsResponseImpl.verify will fail.
     *
     *       Unboundid LDAP SDK does not do this checking after sslSocket.handshake().
     *       The behavior is that mismatched cert is always allowed, as long as the SSL handshake
     *       went through.
     *
     *       It can be done by override the SSLSocket.startHandshake() method (see CustomSSLSocket).
     *
     *       But Unboundid StartTLSPostConnectProcessor only takes a SSLContext, not a SSLSocketFactory.
     *       We can only provide the TrustManager to SSLContext, not a SSLSocketFactory.  Thus,
     *       we cannot make StartTLSPostConnectProcessor to create a SSLSocket that we subclass.
     *
     *       TODO, look into StartTLSRequestHandler in unboundid.  It takes a SSLSocketFactory.
     *
     *       This issue should not be that bad because ssl_allow_mismatched_certs is default to true.
     *
     */
    // private static ConnectionConfig connConfig = ConnectionConfig.LDAP;

    /*
     * for testConnectivity
     */
    private static enum ConnectionConfig {
        LDAP("ldap://localhost:389", "ldap://localhost:389", "0", "false", "false"),

        LDAPI("ldapi://", "ldapi://", "0", "false", "false"),

        LDAPS_T_UNTRUSTED_T_MISMATCHED("ldaps://localhost:636", "ldaps://localhost:636", "0", "true", "true"),
        // JNDI: OK

        LDAPS_T_UNTRUSTED_F_MISMATCHED("ldaps://localhost:636", "ldaps://localhost:636", "0", "true", "false"),
        // JNDI: OK

        LDAPS_F_UNTRUSTED_T_MISMATCHED("ldaps://localhost:636", "ldaps://localhost:636", "0", "false", "true"),
        // JNDI: OK

        LDAPS_F_UNTRUSTED_F_MISMATCHED("ldaps://localhost:636", "ldaps://localhost:636", "0", "false", "false"),
        // JNDI: OK

        STARTTLS_T_UNTRUSTED_T_MISMATCHED("ldap://localhost:389", "ldap://localhost:389", "1", "true", "true"),
        // JNDI: OK

        STARTTLS_T_UNTRUSTED_F_MISMATCHED("ldap://localhost:389", "ldap://localhost:389", "1", "true", "false"),
        // JNDI: ERROR: service.FAILURE (system failure: ZimbraLdapContext) (cause: javax.net.ssl.SSLPeerUnverifiedException hostname of the server 'localhost' does not match the hostname in the server's certificate.)
        /*
        Caused by: java.security.cert.CertificateException: No name matching localhost found
        at sun.security.util.HostnameChecker.matchDNS(HostnameChecker.java:210)
        at sun.security.util.HostnameChecker.match(HostnameChecker.java:77)
        at com.sun.jndi.ldap.ext.StartTlsResponseImpl.verify(StartTlsResponseImpl.java:416)
         */

        STARTTLS_F_UNTRUSTED_T_MISMATCHED("ldap://localhost:389", "ldap://localhost:389", "1", "false", "true"),
        // JNDI: OK

        STARTTLS_F_UNTRUSTED_F_MISMATCHED("ldap://localhost:389", "ldap://localhost:389", "1", "false", "false");
        // JNDI: ERROR: service.FAILURE (system failure: ZimbraLdapContext) (cause: javax.net.ssl.SSLPeerUnverifiedException hostname of the server 'localhost' does not match the hostname in the server's certificate.)


        private String ldap_url;
        private String ldap_master_url;
        private String ldap_starttls_supported;
        // private String ldap_starttls_required;   default(true) is OK
        // private String zimbra_require_interprocess_security;  default(1) is OK
        private String ssl_allow_untrusted_certs;
        private String ssl_allow_mismatched_certs;

        ConnectionConfig(String ldap_url,
                String ldap_master_url,
                String ldap_starttls_supported,
                String ssl_allow_untrusted_certs,
                String ssl_allow_mismatched_certs) {
            this.ldap_url = ldap_url;
            this.ldap_master_url = ldap_master_url;
            this.ldap_starttls_supported = ldap_starttls_supported;
            this.ssl_allow_untrusted_certs = ssl_allow_untrusted_certs;
            this.ssl_allow_mismatched_certs = ssl_allow_mismatched_certs;
        }

        void setLocalConfig() throws Exception {
            LocalconfigTestUtil.modifyLocalConfigTransient(LC.ldap_url, ldap_url);
            LocalconfigTestUtil.modifyLocalConfigTransient(LC.ldap_master_url, ldap_master_url);
            LocalconfigTestUtil.modifyLocalConfigTransient(LC.ldap_starttls_supported, ldap_starttls_supported);
            LocalconfigTestUtil.modifyLocalConfigTransient(LC.ssl_allow_untrusted_certs, ssl_allow_untrusted_certs);
            LocalconfigTestUtil.modifyLocalConfigTransient(LC.ssl_allow_mismatched_certs, ssl_allow_mismatched_certs);
            LC.reload();
        }
    }


    @BeforeClass
    public static void init() throws Exception {

        // these two lines are only needed for ldaps when not running inside the server,
        // because CustomTrustManager is not used when running in CLI
        //
        // these two lines are not needed for starttls
        System.setProperty("javax.net.ssl.trustStore", LC.mailboxd_truststore.value());
        System.setProperty("javax.net.ssl.trustStorePassword", LC.mailboxd_truststore_password.value());

        CliUtil.toolSetup();

        // connConfig.setLocalConfig();
        // TestLdap.TestConfig.useConfig(TestLdap.TestConfig.UBID);
        // TestLdap.TestConfig.useConfig(TestLdap.TestConfig.LEGACY);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        // LdapClient.shutdown();
    }

    private UBIDLdapContext getContext() throws Exception {
        return (UBIDLdapContext) LdapClient.getContext(LdapUsage.UNITTEST);
    }

    private void closeContext(UBIDLdapContext zlc) {
        LdapClient.closeContext(zlc);
    }

    private void dumpUBIDSDKOject(String desc, Object obj) {

        System.out.println("\n--- " + desc);

        Pattern sPattern = Pattern.compile("([^(]*)\\((.*)");
        String asString = obj.toString();

        System.out.println(asString);

        Matcher matcher = sPattern.matcher(asString);
        if (matcher.matches()) {
            String className = matcher.group(1);
            String fields = matcher.group(2);

            if (fields.charAt(fields.length()-1) == ')') {
                fields = fields.substring(0, fields.length() -1);
            }

            String[] fieldArray = fields.split(",");

            System.out.println(className);
            for (String var : fieldArray) {
                System.out.println("  " + var.trim());
            }
        }
    }

    private void dumpConnPool(LDAPConnectionPool connPool) {
        dumpUBIDSDKOject("connPool", connPool);

        LDAPConnectionPoolStatistics poolStats = connPool.getConnectionPoolStatistics();
        dumpUBIDSDKOject("poolStats", poolStats);
    }

    /*
     * Important: Each invocation of testConnectivity must be run in its own VM
     */
    public void testConnectivity(ConnectionConfig connConfig) throws Exception {
        connConfig.setLocalConfig();

        try {
            LdapClient.initialize();

            int expectedPort;

            if (connConfig == ConnectionConfig.LDAPI) {
                expectedPort = 1;  // LdapServerPool.DUMMY_LDAPI_PORT
            } else if (connConfig == ConnectionConfig.LDAP ||
                    connConfig == ConnectionConfig.STARTTLS_T_UNTRUSTED_T_MISMATCHED ||
                    connConfig == ConnectionConfig.STARTTLS_T_UNTRUSTED_F_MISMATCHED ||
                    connConfig == ConnectionConfig.STARTTLS_F_UNTRUSTED_T_MISMATCHED ||
                    connConfig == ConnectionConfig.STARTTLS_F_UNTRUSTED_F_MISMATCHED) {
                expectedPort = 389;
            } else {
                expectedPort = 636;
            }

            UBIDLdapContext zlc1 = getContext();
            assertEquals(expectedPort, zlc1.getNative().getConnectedPort());

            ZAttributes attrs = zlc1.getAttributes("cn=zimbra", null);
            assertEquals("Zimbra Systems Application Data", attrs.getAttrString("description"));

            UBIDLdapContext zlc2 = getContext();
            assertEquals(expectedPort, zlc2.getNative().getConnectedPort());

            closeContext(zlc1);
            closeContext(zlc2);
        } finally {
            // so next test can re-initialized UBIDLdapContext and run
            LdapClient.shutdown();
        }
    }

    @Test
    public void LDAP() throws Exception {
        testConnectivity(ConnectionConfig.LDAP);
    }

    @Test
    public void LDAPI() throws Exception {
        testConnectivity(ConnectionConfig.LDAPI);
    }

    @Test
    public void LDAPS_T_UNTRUSTED_T_MISMATCHED() throws Exception {
        testConnectivity(ConnectionConfig.LDAPS_T_UNTRUSTED_T_MISMATCHED);
    }

    @Test
    public void LDAPS_T_UNTRUSTED_F_MISMATCHED() throws Exception {
        testConnectivity(ConnectionConfig.LDAPS_T_UNTRUSTED_F_MISMATCHED);
    }

    @Test
    public void LDAPS_F_UNTRUSTED_T_MISMATCHED() throws Exception {
        testConnectivity(ConnectionConfig.LDAPS_F_UNTRUSTED_T_MISMATCHED);
    }

    @Test
    public void LDAPS_F_UNTRUSTED_F_MISMATCHED() throws Exception {
        testConnectivity(ConnectionConfig.LDAPS_F_UNTRUSTED_F_MISMATCHED);
    }

    @Test
    public void STARTTLS_T_UNTRUSTED_T_MISMATCHED() throws Exception {
        testConnectivity(ConnectionConfig.STARTTLS_T_UNTRUSTED_T_MISMATCHED);
    }

    @Test
    public void STARTTLS_T_UNTRUSTED_F_MISMATCHED() throws Exception {
        testConnectivity(ConnectionConfig.STARTTLS_T_UNTRUSTED_F_MISMATCHED);
    }

    @Test
    public void STARTTLS_F_UNTRUSTED_T_MISMATCHED() throws Exception {
        testConnectivity(ConnectionConfig.STARTTLS_F_UNTRUSTED_T_MISMATCHED);
    }

    @Test
    public void STARTTLS_F_UNTRUSTED_F_MISMATCHED() throws Exception {
        testConnectivity(ConnectionConfig.STARTTLS_F_UNTRUSTED_F_MISMATCHED);
    }


    /*
     * junixsocket test
     */

    /**
     * Needs -Djava.library.path=/opt/zextras/lib (where the native lib is) when running in Eclipse
     * A simple demo server.
     *
     * @author Christian Kohlschütter
     * @see SimpleTestClient
     */
    public final class SimpleTestServer {
        private static final int MAX_NUMBER = 5;

        private SimpleTestServer() {
            throw new UnsupportedOperationException("No instances");
        }

        public void main(String[] args) throws IOException {
            final File socketFile = new File(new File(System.getProperty("java.io.tmpdir")),
                    "junixsocket-test.sock");
            System.out.println(socketFile);

            try (AFUNIXServerSocket server = AFUNIXServerSocket.newInstance()) {
                // server.setReuseAddress(false);
                server.bind(AFUNIXSocketAddress.of(socketFile));
                System.out.println("server: " + server);

                while (!Thread.interrupted()) {
                    System.out.println("Waiting for connection...");
                    try (Socket sock = server.accept()) {
                        System.out.println("Connected: " + sock);

                        try (InputStream is = sock.getInputStream(); //
                             OutputStream os = sock.getOutputStream()) {
                            System.out.println("Saying hello to client " + os);
                            os.write("Hello, dear Client".getBytes("UTF-8"));
                            os.flush();

                            byte[] buf = new byte[128];
                            int read = is.read(buf);
                            System.out.println("Client's response: " + new String(buf, 0, read, "UTF-8"));

                            System.out.println("Now counting to 5...");
                            DataOutputStream dout = new DataOutputStream(os);
                            DataInputStream din = new DataInputStream(is);
                            int number = 0;
                            while (!Thread.interrupted()) {
                                number++;
                                System.out.println("write " + number);
                                dout.writeInt(number);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    break;
                                }
                                if (number > MAX_NUMBER) {
                                    System.out.println("write -123 (end of numbers)");
                                    dout.writeInt(-123); // in this demo, -123 is our magic number to indicate the end
                                    break;
                                }

                                // verify the number from the client
                                // in the demo, the client just sends 2 * our number
                                int theirNumber = din.readInt();
                                System.out.println("received " + theirNumber);
                                if (theirNumber != (number * 2)) {
                                    throw new IllegalStateException("Received the wrong number: " + theirNumber);
                                }
                            }
                        }
                    } catch (IOException e) {
                        if (server.isClosed()) {
                            throw e;
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Needs -Djava.library.path=/opt/zextras/lib (where the native lib is) when running in Eclipse
     * A simple demo client.
     *
     * @author Christian Kohlschütter
     * @see SimpleTestServer
     */
    public class SimpleTestClient {
        public void main(String[] args) throws IOException {
            final File socketFile = new File(new File(System.getProperty("java.io.tmpdir")),
                    "junixsocket-test.sock");

            try (AFUNIXSocket sock = AFUNIXSocket.newInstance()) {
                try {
                    sock.connect(AFUNIXSocketAddress.of(socketFile));
                } catch (SocketException e) {
                    System.out.println("Cannot connect to server. Have you started it?");
                    System.out.println();
                    throw e;
                }
                System.out.println("Connected");

                try (InputStream is = sock.getInputStream(); //
                     OutputStream os = sock.getOutputStream();) {

                    byte[] buf = new byte[128];

                    int read = is.read(buf);
                    System.out.println("Server says: " + new String(buf, 0, read, "UTF-8"));

                    System.out.println("Replying to server...");
                    os.write("Hello Server".getBytes("UTF-8"));
                    os.flush();

                    System.out.println("Now reading numbers from the server...");
                    DataInputStream din = new DataInputStream(is);
                    DataOutputStream dout = new DataOutputStream(os);
                    while (!Thread.interrupted()) {
                        int number = din.readInt();
                        if (number == -123) {
                            // by convention of this demo, if the number is -123, we stop.
                            // If we don't do this, we'll get an EOFException upon the next unsuccessful read.
                            break;
                        }
                        System.out.println(number);

                        int ourNumber = number * 2;

                        System.out.println("Sending back " + ourNumber);
                        dout.writeInt(ourNumber);
                    }
                }
            }

            System.out.println("End of communication.");
        }
    }
}
