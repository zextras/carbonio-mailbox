package com.zimbra.cs.signature;

import com.zimbra.cs.pgp.PgpHandler;
import com.zimbra.cs.smime.SmimeHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class SignatureHandlerFactoryTest {

    @BeforeEach
    void setUp() {
        SmimeHandler.registerHandler(null);
        PgpHandler.registerHandler(null);
    }

    @Test
    void test_getHandler_when_handlers_are_null_then_return_empty() {
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(null).isEmpty());
    }

    @Test
    void test_getHandler_when_content_is_null_then_return_empty() {
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        MimeMessage mimeMessage = Mockito.mock();
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_content_is_not_Multipart_then_return_empty() throws MessagingException, IOException {
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        MimeMessage mimeMessage = Mockito.mock();
        Mockito.when(mimeMessage.getContent()).thenReturn("content");
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_but_count_is_0_then_return_empty() throws MessagingException, IOException {
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(0);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_and_count_is_1_but_last_part_null_then_return_empty() throws MessagingException, IOException {
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(1);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        Mockito.when(multipart.getBodyPart(0)).thenReturn(null);
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_and_count_is_1_but_last_part_content_is_null_then_return_empty() throws MessagingException, IOException {
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(1);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        BodyPart lastPart = Mockito.mock();
        Mockito.when(multipart.getBodyPart(0)).thenReturn(lastPart);
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_and_last_part_content_is_pkcs7signed_but_smimeHandler_is_null_then_return_empty() throws MessagingException, IOException {
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(1);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        BodyPart lastPart = Mockito.mock();
        Mockito.when(multipart.getBodyPart(0)).thenReturn(lastPart);
        Mockito.when(lastPart.getContentType()).thenReturn("application/pkcs7-mime");
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_and_last_part_content_is_pkcs7signed_but_smimeHandler_is_not_null_then_return_handler() throws MessagingException, IOException {
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(1);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        BodyPart lastPart = Mockito.mock();
        Mockito.when(multipart.getBodyPart(0)).thenReturn(lastPart);
        Mockito.when(lastPart.getContentType()).thenReturn("application/pkcs7-mime");
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        Assertions.assertEquals(smimeHandler, SignatureHandlerFactory.getHandler(mimeMessage).get());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_and_last_part_content_is_pkcs7signature_but_smimeHandler_is_not_null_then_return_handler() throws MessagingException, IOException {
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(1);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        BodyPart lastPart = Mockito.mock();
        Mockito.when(multipart.getBodyPart(0)).thenReturn(lastPart);
        Mockito.when(lastPart.getContentType()).thenReturn("application/pkcs7-signature");
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        Assertions.assertEquals(smimeHandler, SignatureHandlerFactory.getHandler(mimeMessage).get());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_and_last_part_content_is_old_pkcs7signed_but_smimeHandler_is_not_null_then_return_handler() throws MessagingException, IOException {
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(1);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        BodyPart lastPart = Mockito.mock();
        Mockito.when(multipart.getBodyPart(0)).thenReturn(lastPart);
        Mockito.when(lastPart.getContentType()).thenReturn("application/x-pkcs7-mime");
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        Assertions.assertEquals(smimeHandler, SignatureHandlerFactory.getHandler(mimeMessage).get());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_and_last_part_content_is_pgpsigned_but_smimeHandler_is_null_then_return_empty() throws MessagingException, IOException {
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(1);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        BodyPart lastPart = Mockito.mock();
        Mockito.when(multipart.getBodyPart(0)).thenReturn(lastPart);
        Mockito.when(lastPart.getContentType()).thenReturn("application/pgp-signature");
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(mimeMessage).isEmpty());
    }

    @Test
    void test_getHandler_when_content_is_Multipart_and_last_part_content_is_pgpsigned_but_smimeHandler_is_not_null_then_return_handler() throws MessagingException, IOException {
        MimeMessage mimeMessage = Mockito.mock();
        Multipart multipart = Mockito.mock();
        Mockito.when(multipart.getCount()).thenReturn(1);
        Mockito.when(mimeMessage.getContent()).thenReturn(multipart);
        BodyPart lastPart = Mockito.mock();
        Mockito.when(multipart.getBodyPart(0)).thenReturn(lastPart);
        Mockito.when(lastPart.getContentType()).thenReturn("application/pgp-signature");
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        Assertions.assertEquals(handler, SignatureHandlerFactory.getHandler(mimeMessage).get());
    }

    @Test
    void test_getHandler_when_content_throws_exception_then_return_empty() throws MessagingException, IOException {
        MimeMessage mimeMessage = Mockito.mock();
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        Mockito.when(mimeMessage.getContent()).thenThrow(new RuntimeException());
        Assertions.assertTrue(SignatureHandlerFactory.getHandler(mimeMessage).isEmpty());    }

    @Test
    void test_getHandler_when_smime_then_return_smimeHandler() throws IOException, MessagingException {
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        try (InputStream emlFile = new FileInputStream(new File("src/test/resources/com/zimbra/cs/signature/smime.eml"))) {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage message = new MimeMessage(session, emlFile);
            Assertions.assertEquals(smimeHandler, SignatureHandlerFactory.getHandler(message).get());
        }
    }

        // Create a session


    @Test
    void test_getHandler_when_pgp_then_return_pgpHandler() throws IOException, MessagingException  {
        PgpHandler handler = Mockito.mock();
        PgpHandler.registerHandler(handler);
        SmimeHandler smimeHandler = Mockito.mock();
        SmimeHandler.registerHandler(smimeHandler);
        try (InputStream emlFile = new FileInputStream(new File("src/test/resources/com/zimbra/cs/signature/pgp.eml"))) {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage message = new MimeMessage(session, emlFile);
            Assertions.assertEquals(handler, SignatureHandlerFactory.getHandler(message).get());
        }
    }

}
