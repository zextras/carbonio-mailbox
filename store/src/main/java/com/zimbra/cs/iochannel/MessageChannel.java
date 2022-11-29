// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.iochannel;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.zimbra.common.iochannel.Client;
import com.zimbra.common.iochannel.Client.PeerServer;
import com.zimbra.common.iochannel.IOChannelException;
import com.zimbra.common.iochannel.Server;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;

/**
 * MessageChannel is a service in ZCS that allows a message to be sent
 * from one mailboxd to another.  There can be multiple apps registered
 * to share the single underlying iochannel without creating its own
 * server socket.  Each application can register its own type of Message
 * it wants to send and receive, and MessageChannel will alert the app
 * when a message for its type is received.
 *
 * @author jylee
 *
 */
public class MessageChannel {

    public static MessageChannel getInstance() {
        synchronized (MessageChannel.class) {
            if (instance == null) {
                instance = new MessageChannel();
            }
        }
        return instance;
    }

    public synchronized void startup() throws ServiceException, IOException {
        if (!running) {
            ZcsConfig config = new ZcsConfig();
            server = Server.start(config);
            client = Client.start(config);
            server.registerCallback(new MessageChannelCallback());
            running = true;
        }
    }

    public synchronized void shutdown() {
        server.shutdown();
        client.shutdown();
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Sends the message to peer server that hosts Account identified
     * in Message.getRecipientAccountId().
     */
    public void sendMessage(Message message) {
        String accountId = message.getRecipientAccountId();
        Provisioning prov = Provisioning.getInstance();
        try {
            Account targetAccount = prov.getAccountById(accountId);
            if (targetAccount == null) {
                log.error("account %s doesn't exist", accountId);
                return;
            }
            com.zimbra.cs.account.Server targetServer = targetAccount.getServer();
            sendMessage(targetServer, message);
        } catch (ServiceException e) {
            log.error("can't find server for account %s", accountId, e);
        }
    }

    public void sendMessage(com.zimbra.cs.account.Server server, Message message) {
        String peerHostname;
        PeerServer peer;
        try {
            if (server == null || client == null ||
                    (peerHostname = server.getServiceHostname()) == null ||
                    (peer = client.getPeer(peerHostname)) == null) {
                log.error("no client available for server %s", server.getServiceHostname());
                return;
            }
            peer.sendMessage(message.serialize());
        } catch (IOChannelException e) {
            log.warn("MessageChannel: " + e.getMessage());
        } catch (IOException e) {
            log.error("can't send notification", e);
        }
    }

    private static class MessageChannelCallback implements Server.NotifyCallback {

        @Override
        public void dataReceived(String header, ByteBuffer buffer) {
            try {
                Message m = Message.create(buffer);
                m.getHandler().handle(m, header);
            } catch (IOException e) {
                log.warn("can't create message", e);
            }
        }
    }

    private Server server;
    private Client client;
    private boolean running;

    private static Log log = LogFactory.getLog("iochannel");
    private static MessageChannel instance;
}
