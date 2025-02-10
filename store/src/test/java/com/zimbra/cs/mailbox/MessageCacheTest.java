package com.zimbra.cs.mailbox;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.pgp.PgpHandler;
import com.zimbra.cs.smime.SmimeHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.mail.internet.MimeMessage;

class MessageCacheTest {

    @BeforeEach
    public void setUp() throws Exception {
        Provisioning provisioning = Mockito.mock();
        Server server = Mockito.mock();
        Mockito.when(provisioning.getLocalServer()).thenReturn(server);
        Mockito.when(server.getMessageCacheSize()).thenReturn(0);
        Provisioning.setInstance(provisioning);
        SmimeHandler.registerHandler(null);
        PgpHandler.registerHandler(null);
    }

    @AfterEach
    public void tearDown() {
        Provisioning.setInstance(null);
        SmimeHandler.registerHandler(null);
        PgpHandler.registerHandler(null);
    }

    @Test
    void test_doDecryption_when_encryption_handler_is_null_then_return_null() {
        MessageCache.CacheNode cache = new MessageCache.CacheNode();
        Assertions.assertNull(MessageCache.doDecryption(null, cache, 0));
        Assertions.assertTrue(cache.smimeAccessInfo.isEmpty());
    }

    @Test
    void test_doDecryption_when_encryption_handler_is_not_null_but_can_not_decrypt_then_return_null() throws Exception {
        MessageCache.CacheNode cache = new MessageCache.CacheNode();
        cache.message = Mockito.mock();
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        Mockito.when(cache.message.getContentType()).thenReturn("application/pkcs7-mime; smime-type=enveloped-data; name=\"smime.p7m\"");
        MailItem item = Mockito.mock();
        Mailbox mailbox = Mockito.mock();
        Mockito.when(item.getMailbox()).thenReturn(mailbox);
        Mockito.when(smimeHandler.decryptMessage(mailbox, cache.message, 0)).thenReturn(null);
        Assertions.assertNull(MessageCache.doDecryption(item, cache, 0));
        Assertions.assertEquals(MimeConstants.ERR_DECRYPTION_FAILED, cache.smimeAccessInfo.get(0));
    }

    @Test
    void test_doDecryption_when_encryption_handler_is_not_null_but_can_decrypt_then_return_mimeMessage() throws Exception {
        MessageCache.CacheNode cache = new MessageCache.CacheNode();
        cache.message = Mockito.mock();
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        Mockito.when(cache.message.getContentType()).thenReturn("application/pkcs7-mime; smime-type=enveloped-data; name=\"smime.p7m\"");
        MailItem item = Mockito.mock();
        Mailbox mailbox = Mockito.mock();
        Mockito.when(item.getMailbox()).thenReturn(mailbox);
        MimeMessage mimeMessage = Mockito.mock();
        Mockito.when(smimeHandler.decryptMessage(mailbox, cache.message, 0)).thenReturn(mimeMessage);
        Assertions.assertEquals(mimeMessage, MessageCache.doDecryption(item, cache, 0));
        Assertions.assertFalse(cache.smimeAccessInfo.isEmpty());
    }
}
