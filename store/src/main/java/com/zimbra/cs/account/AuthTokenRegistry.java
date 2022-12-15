// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.util.Zimbra;
import com.zimbra.soap.admin.message.RefreshRegisteredAuthTokensRequest;

/**
 *
 * @author gsolovyev
 * a registry of authtokens that have been deregistered (logged out) and need to be broadcasted to other servers.
 * This class implements storage of tokens as a queue limited in size by {@link LC#zimbra_deregistered_authtoken_queue_size}
 * and a {@link TimerTask} task responsible for broadcasting the queue to other servers.
 * TODO: switch to using a global shared registry such as Redis or a subscription based notification mechanism such as RabbitMQ
 */
public final class AuthTokenRegistry {
    private static final Log mLOG = LogFactory.getLog(AuthTokenRegistry.class);
    //queue of deregistered authtokens
    final private static ConcurrentLinkedQueue<AuthToken> deregisteredOutAuthTokens = new ConcurrentLinkedQueue<AuthToken>();

    /**
     * adds a token to the queue of deregistered tokens
     * @param token
     */
    final public static void addTokenToQueue(AuthToken token) {
        while(deregisteredOutAuthTokens.size() > LC.zimbra_deregistered_authtoken_queue_size.intValue() && !deregisteredOutAuthTokens.isEmpty()) {
            //throw out oldest tokens to make space
            deregisteredOutAuthTokens.remove();
        }
        deregisteredOutAuthTokens.add(token);
    }

    /**
     * starts up the {@link TimerTask} for broadcasting the queue to other servers
     * @param interval
     */
    public static void startup(long interval) {
        Zimbra.sTimer.schedule(new SendTokensTimerTask(), interval, interval);
    }

    private static final class SendTokensTimerTask extends TimerTask {

        SendTokensTimerTask()  { }

        @Override
        public void run() {

            mLOG.debug("Broadcasting dergistered authtokens");
            try {
                //get a snapshot of the queue so that we are not holding logins and logouts
                Object[] copy = deregisteredOutAuthTokens.toArray();

                //if we have any tokens to report send them over to all servers
                if(copy.length > 0) {
                    mLOG.debug("Found some dergistered authtokens to broadcast");

                    /* Remove the snapshot from the queue.
                    *  Since this block is not synchronized new tokens may have been added to the queue and some may have been pushed out due to size limitation
                    */
                    deregisteredOutAuthTokens.removeAll(Arrays.asList(copy));

                    //send the snapshot to other servers
                    try {
                        List<Server> mailServers = Provisioning.getInstance().getAllMailClientServers();
                        SoapProvisioning soapProv = SoapProvisioning.getAdminInstance();
                        soapProv.soapZimbraAdminAuthenticate();
                        RefreshRegisteredAuthTokensRequest req = new RefreshRegisteredAuthTokensRequest();
                        for(Object ztoken : copy) {
                            if(!((AuthToken)ztoken).isExpired()) {//no need to broadcast expired tokens, since they will be cleaned up automatically
                                req.addToken(((AuthToken)ztoken).getEncoded());
                            }
                        }
                        for(Server server : mailServers) {
                            if(server.isLocalServer()) {
                                continue;
                            }
                            soapProv.invokeJaxb(req, server.getName());
                        }
                    } catch (ServiceException | AuthTokenException e) {
                        mLOG.error("Failed to broadcast deregistered authtokens", e);
                    }
                }
        } catch (Throwable t) {
            mLOG.info("Caught exception in SendTokens timer", t);
        }
    }
    }
}
