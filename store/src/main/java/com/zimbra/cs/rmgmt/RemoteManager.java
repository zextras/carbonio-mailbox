// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.rmgmt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.security.SecurityUtils;

import com.zimbra.common.account.Key;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.util.Zimbra;



public class RemoteManager {

    private static final int DEFAULT_REMOTE_MANAGEMENT_PORT = 22;
    private static final String DEFAULT_REMOTE_MANAGEMENT_USER = "zextras";
    private static final String DEFAULT_REMOTE_MANAGEMENT_COMMAND = "/opt/zextras/libexec/zmrcd";

    private final File mPrivateKey;

    private final String mUser;
    private final String mHost;
    private final int mPort;
    private final String mShimCommand;
    private final String mDescription;

    private RemoteManager(Server remote) throws ServiceException {
        mHost = remote.getAttr(Provisioning.A_zimbraServiceHostname, null);
        if (mHost == null) throw ServiceException.FAILURE("server " + remote.getName() + " does not have a service host name", null);

        mPort = remote.getIntAttr(Provisioning.A_zimbraRemoteManagementPort, DEFAULT_REMOTE_MANAGEMENT_PORT);
        if (mPort < 0) throw ServiceException.FAILURE("server " + remote.getName() + " has invalid " + Provisioning.A_zimbraRemoteManagementPort, null);

        mUser = remote.getAttr(Provisioning.A_zimbraRemoteManagementUser, DEFAULT_REMOTE_MANAGEMENT_USER);
        if (mUser == null) throw ServiceException.FAILURE("server " + remote.getName() + " has no " + Provisioning.A_zimbraRemoteManagementUser, null);

        mShimCommand = remote.getAttr(Provisioning.A_zimbraRemoteManagementCommand, DEFAULT_REMOTE_MANAGEMENT_COMMAND);
        if (mShimCommand == null) throw ServiceException.FAILURE("server " + remote.getName() + " has no " + Provisioning.A_zimbraRemoteManagementCommand, null);

        Server local = Provisioning.getInstance().getLocalServer();
        String localName = local.getName();
        String privateKey = local.getAttr(Provisioning.A_zimbraRemoteManagementPrivateKeyPath, null);
        if (privateKey == null) {
            throw ServiceException.FAILURE("server " + localName + " has no " + Provisioning.A_zimbraRemoteManagementPrivateKeyPath, null);
        }

        File key = new File(privateKey);
        if (!key.exists()) {
            throw ServiceException.FAILURE("server " + localName + " " + Provisioning.A_zimbraRemoteManagementPrivateKeyPath + " (" + key + ") does not exist", null);
        }
        if (!key.canRead()) {
            throw ServiceException.FAILURE("server " + localName + " " + Provisioning.A_zimbraRemoteManagementPrivateKeyPath + " (" + key + ") is not readable", null);
        }
        mPrivateKey = key;

        mDescription = "{RemoteManager: " + localName + "->" + mUser + "@" + mHost + ":" + mPort + "}";
    }

    public String getPrivateKeyPath() {
        return mPrivateKey.getAbsolutePath();
    }

    @Override
    public String toString() {
        return mDescription;
    }

    public Integer getPort() {
	return mPort;
    }

    private synchronized void executeBackground0(String command, RemoteBackgroundHandler handler) {
        RemoteResult result = new RemoteResult();
        try {
            result = executeRemoteCommand(mUser,mHost,mPort,mPrivateKey,mShimCommand,command);
                try {
                    ZimbraLog.rmgmt.trace("stdout content for cmd:\n%s", new String(result.mStdout, "UTF-8"));
                    ZimbraLog.rmgmt.trace("stderr content for cmd:\n%s", new String(result.mStderr, "UTF-8"));
                } catch (Exception ex) {
                    ZimbraLog.rmgmt.trace("Problem logging stdout or stderr for cmd - probably not UTF-8");
                }
            InputStream stdout = new ByteArrayInputStream(result.mStdout);
            InputStream stderr = new ByteArrayInputStream(result.mStderr);
            handler.read(stdout,stderr);
        }
        catch (OutOfMemoryError e) {
            Zimbra.halt("out of memory", e);
        } catch (Throwable t) {
            handler.error(t);
        }
    }

    public void executeBackground(final String command, final RemoteBackgroundHandler handler) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                executeBackground0(command, handler);
            }
        };

        Thread t = new Thread(r);
        t.setName(this + "-" + command);
        t.setDaemon(true);
        t.start();
    }

    public synchronized RemoteResult execute(String command) throws ServiceException{
        RemoteResult result;
        try {
            result = executeRemoteCommand(mUser, mHost, mPort, mPrivateKey, mShimCommand, command);
            try {
                ZimbraLog.rmgmt
                    .trace("stdout content for cmd:\n%s", new String(result.mStdout,
                        StandardCharsets.UTF_8));
                ZimbraLog.rmgmt
                    .trace("stderr content for cmd:\n%s", new String(result.mStderr,
                        StandardCharsets.UTF_8));
            } catch (Exception ex) {
                ZimbraLog.rmgmt
                    .trace("Problem logging stdout or stderr for cmd - probably not UTF-8");
            }
        } catch (Exception e) {
             throw ServiceException.FAILURE(
                 "exception executing command " + command + " with " + this + " " + e);
        }
        return result;
    }

    public static RemoteResult executeRemoteCommand(String username, String host, int port, File privateKey,
            String mShimCommand, String command) throws Exception {
        long defaultTimeoutSeconds = 100l;
        String send = "HOST:" + host + " " + command;
        InputStream inputStream = new ByteArrayInputStream(send.getBytes());
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        ConnectFuture cf = client.connect(username, host, port);
        try (ClientSession session = cf.verify().getSession()) {
            session.addPublicKeyIdentity(loadKeypair(privateKey.getAbsolutePath()));
            session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
            ZimbraLog.rmgmt.debug("executing shim command '%s'", mShimCommand);
            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                    ByteArrayOutputStream errorResponseStream = new ByteArrayOutputStream();
                    ChannelExec channel = session.createExecChannel(mShimCommand)) {
                ZimbraLog.rmgmt.debug("sending mgmt command '%s'", send);
                channel.setIn(inputStream);
                channel.setOut(responseStream);
                channel.setErr(errorResponseStream);
                channel.open().await();
                try {
                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                            TimeUnit.MINUTES.toMillis(LC.zimbra_remote_cmd_channel_timeout_min.intValue()));
                    session.close(false);
                    RemoteResult result = new RemoteResult();
                    InputStream stdout = new ByteArrayInputStream(responseStream.toByteArray());
                    InputStream stderr = new ByteArrayInputStream(errorResponseStream.toByteArray());
                    result.mStdout = ByteUtil.getContent(stdout, -1);
                    result.mStderr = ByteUtil.getContent(stderr, -1);
                    result.mExitStatus = channel.getExitStatus();
                    if (result.mExitStatus != 0) {
                        throw new IOException("FAILURE: exit status=" + result.mExitStatus + "\n"
                            + "STDOUT=" + new String(result.mStdout) + "\n"
                            + "STDERR=" + new String(result.mStderr));
                    }
                    result.mExitSignal = channel.getExitSignal();
                    return result;
                } finally {
                    channel.close(false);
                    session.close();
                }
            }
        } finally {
            client.stop();
        }
    }

	public static KeyPair loadKeypair(String privateKeyPath) throws IOException, GeneralSecurityException {
        try (InputStream privateKeyStream = new FileInputStream(privateKeyPath)) {
            Iterable<KeyPair> keyPairIterable =
                    SecurityUtils.loadKeyPairIdentities(null, null, privateKeyStream, null);
            KeyPair keyPair = keyPairIterable.iterator().next();
            return keyPair;
        }
	}

    public static RemoteManager getRemoteManager(Server server) throws ServiceException {
        return new RemoteManager(server);
    }

    public static void main(String[] args) throws Exception {
        int iterations = Integer.parseInt(args[0]);
        String serverName = args[1];
        String command = args[2];

        CliUtil.toolSetup("DEBUG");
        Provisioning prov = Provisioning.getInstance();
        Server remote = prov.get(Key.ServerBy.name, serverName);

        for (int i = 0; i < iterations; i++) {
            RemoteManager rm = RemoteManager.getRemoteManager(remote);
            RemoteResult rr = rm.execute(command);
            Map<String,String> m = RemoteResultParser.parseSingleMap(rr);
            if (m == null) {
                System.out.println("NO RESULT RETURNED");
            } else {
                for (String k : m.keySet()) {
                    System.out.println(k + "=" + m.get(k));
                }
            }
        }
    }
}
