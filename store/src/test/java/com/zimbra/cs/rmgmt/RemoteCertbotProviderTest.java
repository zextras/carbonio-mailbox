package com.zimbra.cs.rmgmt;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoteCertbotProviderTest extends MailboxTestSuite {

    @Test
    void handleShouldThrowServiceExceptionWhenNoServerWithProxyIsAvailable() throws Exception {
        RemoteCertbot.RemoteCertbotProvider remoteCertbotProvider = new RemoteCertbot
                .RemoteCertbotProvider(Provisioning.getInstance());

        final ServiceException exception =
                assertThrows(ServiceException.class, remoteCertbotProvider::getRemoteCertbot);
        assertEquals(
                "system failure: Issuing LetsEncrypt certificate command requires carbonio-proxy. Make sure"
                        + " carbonio-proxy is installed, up and running.",
                exception.getMessage());
    }

}