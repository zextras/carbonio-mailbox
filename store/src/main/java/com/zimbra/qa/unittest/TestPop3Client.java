// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

import com.zimbra.common.util.Log;
import com.zimbra.cs.mailclient.pop3.Pop3Capabilities;
import com.zimbra.cs.mailclient.pop3.Pop3Config;
import com.zimbra.cs.mailclient.pop3.Pop3Connection;
import com.zimbra.cs.mailclient.util.SSLUtil;
import com.zimbra.cs.mailclient.MailConfig;

public class TestPop3Client extends TestCase {
    private Pop3Config config;
    private Pop3Connection connection;

    private static final String HOST = "localhost";
    private static final int PORT = 7110;
    private static final int SSL_PORT = 7995;
    private static final String USER = "user1";
    private static final String PASS = "test123";

    static {
        BasicConfigurator.configure();
    }

    public void testDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        config = null;
        connection = null;
    }

    public void testLogin() throws Exception {
        login();
    }

    public void testPlainAuth() throws Exception {
        connect();
        connection.authenticate(PASS);
    }

    public void testTls() throws Exception {
        config = getConfig(false);
        config.setSecurity(MailConfig.Security.TLS);
        connect();
        login();
    }

    public void testCapabilities() throws Exception {
        connect();
        Pop3Capabilities caps = connection.getCapabilities();
        assertNotNull(caps);
        assertTrue(caps.hasCapability(Pop3Capabilities.STLS));
        assertTrue(caps.hasCapability("StLs"));
        assertTrue(caps.hasCapability(Pop3Capabilities.IMPLEMENTATION, "ZimbraInc"));
        assertFalse(caps.hasCapability(Pop3Capabilities.EXPIRE, "NEVER"));
        //connection.getLogger().debug("Capabilities BEFORE = " + caps);
        login();
        caps = connection.getCapabilities();
        //connection.getLogger().debug("Capabilities AFTER = " + caps);
        assertTrue(caps.hasCapability(Pop3Capabilities.EXPIRE, "NEVER"));
    }

    public void testGetMessageSizes() throws Exception {
        login();
        Integer[] sizes = connection.getMessageSizes();
        assertNotNull(sizes);
        assertEquals(connection.getMessageCount(), sizes.length);
        for (int i = 0; i < sizes.length; i++) {
            assertEquals(sizes[i], (Integer)connection.getMessageSize(i + 1));
        }
    }

    public void testGetMessageUids() throws Exception {
        login();
        String[] uids = connection.getMessageUids();
        assertNotNull(uids);
        assertEquals(connection.getMessageCount(), uids.length);
        for (int i = 0; i < uids.length; i++) {
            assertEquals(uids[i], connection.getMessageUid(i + 1));
        }
    }

    public void testGetMessage() throws Exception {
        login();
        for (int msgno = 5; msgno < 20; msgno++) {
            int size = connection.getMessageSize(msgno);
            assertTrue(size > 0);
            InputStream is = connection.getMessage(msgno);
            assertNotNull(is);
            int count = countBytes(is);
            assertEquals(size, count);
            is.close();
        }
    }

    public void testDeleteMessage() throws Exception {
        login();
        boolean deleted = connection.deleteMessage(1);
        assertTrue(deleted);
        int count = connection.getMessageCount() - 1;
        connection.quit();
        login();
        int newCount = connection.getMessageCount();
        assertEquals(count, newCount);
    }

    private static int countBytes(InputStream is) throws IOException {
        int count = 0;
        while (is.read() != -1) {
            count++;
        }
        return count;
    }

    private void login() throws IOException {
        connect();
        connection.login(PASS);
    }

    private void connect() throws IOException {
        connect(false);
    }

    private void connect(boolean ssl) throws IOException {
        if (config == null) {
            config = getConfig(ssl);
        }
        System.out.println("---------");
        connection = new Pop3Connection(config);
        connection.connect();
    }

    private static Pop3Config getConfig(boolean ssl) {
        Pop3Config config = new Pop3Config(HOST);
        if (ssl) config.setSecurity(MailConfig.Security.SSL);
        config.setPort(ssl ? SSL_PORT : PORT);
        config.setSSLSocketFactory(SSLUtil.getDummySSLContext().getSocketFactory());
        config.getLogger().setLevel(Log.Level.trace);
        config.setMechanism("PLAIN");
        config.setAuthenticationId(USER);
        return config;
    }


}
