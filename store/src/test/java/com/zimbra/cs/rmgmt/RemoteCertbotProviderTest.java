package com.zimbra.cs.rmgmt;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class RemoteCertbotProviderTest extends MailboxTestSuite {

    @Test
    void handleShouldThrowServiceExceptionWhenNoServerWithProxyIsAvailable() throws Exception {
        RemoteCertbot.RemoteCertbotProvider remoteCertbotProvider = new RemoteCertbot
                .RemoteCertbotProvider(Provisioning.getInstance(), (Server server) -> Mockito.mock(RemoteManager.class));

        final ServiceException exception =
                assertThrows(ServiceException.class, remoteCertbotProvider::getRemoteCertbot);
        assertEquals(
                "system failure: Issuing LetsEncrypt certificate command requires carbonio-proxy. Make sure"
                        + " carbonio-proxy is installed, up and running.",
                exception.getMessage());
    }

}