package com.zextras.mailbox.encryption;

import com.zimbra.cs.pgp.PgpHandler;
import com.zimbra.cs.smime.SmimeHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class EncryptionHandlerFactoryTest {

    @BeforeEach
    void setUp() {
        SmimeHandler.registerHandler(null);
        PgpHandler.registerHandler(null);
    }

    @AfterEach
    void tearDown() {
        SmimeHandler.registerHandler(null);
        PgpHandler.registerHandler(null);
    }

    @Test
    void test_getHandler_when_no_handlers() {
        Assertions.assertTrue(EncryptionHandlerFactory.getHandler(null).isEmpty());
    }

    @Test
    void test_getHandler_when_pgpHandler_is_not_null_and_smime_handler_is_null_and_content_type_is_smime_then_return_smime()
            throws MessagingException {
        MimeMessage mimeMessage = Mockito.mock();
        PgpHandler.registerHandler(Mockito.mock());
        Mockito.when(mimeMessage.getContentType()).thenReturn("application/pkcs7-mime; smime-type=enveloped-data; name=\"smime.p7m\"");
        EncryptionHandler actual = EncryptionHandlerFactory.getHandler(mimeMessage).get();
        Assertions.assertEquals(SmimeHandler.getHandler(), actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pkcs7-mime; smime-type=enveloped-data; name=\"smime.p7m\"",
            "application/pkcs7-mime; smime-type=enveloped-data; name=\"smime.p7m\"; charset=\"UTF-8\""})
    void test_getHandler_when_pgpHandler_is_not_null_and_smime_handler_is_not_null_and_content_type_is_smime_then_return_smimeHandler(String contentType)
            throws MessagingException {
        MimeMessage mimeMessage = Mockito.mock();
        PgpHandler.registerHandler(Mockito.mock());
        SmimeHandler mock = Mockito.mock();
        SmimeHandler.registerHandler(mock);
        Mockito.when(mimeMessage.getContentType()).thenReturn(contentType);
        Assertions.assertEquals(mock, EncryptionHandlerFactory.getHandler(mimeMessage).get());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "application/pkcs7-mime; smime-type=enveloped-data; ",
            "application/pkcs7-mime;  name=\"smime.p7m\"",
            " smime-type=enveloped-data; name=\"smime.p7m\"",
            " abc",
    })
    void test_getHandler_when_pgpHandler_is_not_null_and_smime_handler_is_not_null_and_content_type_is_not_smime_then_return_empty(String contentType)
            throws MessagingException {
        MimeMessage mimeMessage = Mockito.mock();
        PgpHandler.registerHandler(Mockito.mock());
        SmimeHandler mock = Mockito.mock();
        SmimeHandler.registerHandler(mock);
        Mockito.when(mimeMessage.getContentType()).thenReturn(contentType);
        Assertions.assertTrue(EncryptionHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_pgpHandler_is_not_null_and_smime_handler_is_not_null_and_content_is_null_then_return_empty() {
        SmimeHandler.registerHandler(Mockito.mock());
        PgpHandler.registerHandler(Mockito.mock());
        Assertions.assertTrue(EncryptionHandlerFactory.getHandler(Mockito.mock()).isEmpty());
    }

    @Test
    void test_getHandler_when_smime_email_then_return_smimeHandler() throws Exception {
        Path filePath = Paths.get(
                ClassLoader.getSystemResource("com/zextras/mailbox/encryption/smime_encrypted.eml").toURI()
        );

        MimeMessage mimeMessage = new MimeMessage(null, Files.newInputStream(filePath));
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        PgpHandler pgpHandler = Mockito.mock();
        PgpHandler.registerHandler(pgpHandler);
        Assertions.assertEquals(smimeHandler, EncryptionHandlerFactory.getHandler(mimeMessage).get());

    }

    @Test
    void test_getHandler_when_pgpHandler_is_null_and_smime_handler_is_not_null_and_content_type_is_pgp_then_return_empty()
            throws MessagingException {
        MimeMessage mimeMessage = Mockito.mock();
        SmimeHandler mock = Mockito.mock();
        SmimeHandler.registerHandler(mock);
        Mockito.when(mimeMessage.getContentType()).thenReturn("");
        Assertions.assertTrue(EncryptionHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_pgp_with_subject_then_return_pgpHandler() throws Exception {
        Path filePath = Paths.get(
                ClassLoader.getSystemResource("com/zextras/mailbox/encryption/pgp_encrypted.eml").toURI()
        );

        MimeMessage mimeMessage = new MimeMessage(null, Files.newInputStream(filePath));
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        PgpHandler pgpHandler = Mockito.mock();
        PgpHandler.registerHandler(pgpHandler);
        Assertions.assertEquals(pgpHandler, EncryptionHandlerFactory.getHandler(mimeMessage).get());
    }

    @Test
    void test_getHandler_when_pgp_without_subject_then_return_pgpHandler() throws Exception {
        Path filePath = Paths.get(
                ClassLoader.getSystemResource("com/zextras/mailbox/encryption/pgp_encrypted_without_subject.eml").toURI()
        );

        MimeMessage mimeMessage = new MimeMessage(null, Files.newInputStream(filePath));
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        PgpHandler pgpHandler = Mockito.mock();
        PgpHandler.registerHandler(pgpHandler);
        Assertions.assertEquals(pgpHandler, EncryptionHandlerFactory.getHandler(mimeMessage).get());
    }
}
