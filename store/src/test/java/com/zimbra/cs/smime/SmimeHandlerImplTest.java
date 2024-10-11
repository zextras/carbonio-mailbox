package com.zimbra.cs.smime;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.localconfig.LocalConfig;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.security.auth.x500.X500Principal;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmimeHandlerImplTest {

    @BeforeEach
    void setUp() throws Exception {
        Field trustStore = SmimeHandlerImpl.class.getDeclaredField("trustStore");
        trustStore.setAccessible(true);
        trustStore.set(null, null);
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.remove(LC.mailboxd_truststore.key());
        localConfig.save();

        LC.get(LC.mailboxd_truststore.key());
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
    }

    @AfterEach
    void tearDown() throws Exception {
        setUp();
    }

    @ParameterizedTest
    @MethodSource("provideDataExtractCN")
    void test_extractCN(String name, String output) {
        SmimeHandlerImpl smimeHandler = new SmimeHandlerImpl();
        X500Principal principal = Mockito.mock();
        Mockito.when(principal.getName()).thenReturn(name);
        String result = smimeHandler.extractCN(principal);
        assertEquals(output, result);

    }

    static Stream<Arguments> provideDataExtractCN() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("1", ""),
                org.junit.jupiter.params.provider.Arguments.of("CN", ""),
                org.junit.jupiter.params.provider.Arguments.of("CN=", ""),
                org.junit.jupiter.params.provider.Arguments.of("cn=", ""),
                org.junit.jupiter.params.provider.Arguments.of("Cn=test", "test"),
                org.junit.jupiter.params.provider.Arguments.of("CN=test2", "test2"),
                org.junit.jupiter.params.provider.Arguments.of("cn=test3", "test3"),
                org.junit.jupiter.params.provider.Arguments.of("cn=test3,cn=test4", "test3"),
                org.junit.jupiter.params.provider.Arguments.of("abc=xyz,cn=test3,cn=test4", "test3")
        );
    }

    @Test
    void test_getKeyStore_when_store_is_not_valid_then_throws_FileNotFoundException() throws Exception {
        Assertions.assertThrowsExactly(FileNotFoundException.class, SmimeHandlerImpl::getKeyStore);
    }

    @Test
    void test_getKeyStore_when_store_is_valid_then_returns_keyStore() throws Exception {
        String javaHome = System.getProperty("java.home");
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.set(LC.mailboxd_truststore.key() ,javaHome + "/lib/security/cacerts");
        localConfig.save();
        LC.get(LC.mailboxd_truststore.key());
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Assertions.assertNotNull(SmimeHandlerImpl.getKeyStore());
    }

    @Test
    void test_getKeyStore_when_store_is_valid_and_cache_is_not_invalidated_then_returns_keyStore() throws Exception {
        String javaHome = System.getProperty("java.home");
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.set(LC.mailboxd_truststore.key() ,javaHome + "/lib/security/cacerts");
        localConfig.save();
        LC.get(LC.mailboxd_truststore.key());
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Assertions.assertEquals(SmimeHandlerImpl.getKeyStore(), SmimeHandlerImpl.getKeyStore());
    }

    @Test
    void test_getKeyStore_when_store_is_valid_and_cache_is_invalidated_then_returns_keyStore() throws Exception {
        String javaHome = System.getProperty("java.home");
        Method method = LocalConfig.class.getDeclaredMethod("getInstance");
        method.setAccessible(true);
        LocalConfig localConfig = (LocalConfig) method.invoke(null);
        localConfig.set(LC.mailboxd_truststore.key() ,javaHome + "/lib/security/cacerts");
        localConfig.save();
        LC.get(LC.mailboxd_truststore.key());
        Method load = LocalConfig.class.getDeclaredMethod("load", String.class);
        load.setAccessible(true);
        load.invoke(localConfig, localConfig.getConfigFile());
        Field trustStoreRefreshTime = SmimeHandlerImpl.class.getDeclaredField("trustStoreRefreshTime");
        trustStoreRefreshTime.setAccessible(true);
        KeyStore keyStore = SmimeHandlerImpl.getKeyStore();
        trustStoreRefreshTime.set(null, 0);
        Assertions.assertNotEquals(keyStore, SmimeHandlerImpl.getKeyStore());
    }

    @Test
    void test_getX509TrustManager_when_holders_is_empty_then_returns_empty_list() throws Exception {
        SMIMESigned signed = Mockito.mock();
        Store<X509CertificateHolder> holders = Mockito.mock();
        Mockito.when(signed.getCertificates()).thenReturn(holders);
        Mockito.when(holders.getMatches(null)).thenReturn(List.of());
        Assertions.assertTrue(SmimeHandlerImpl.getX509Certificates(signed).isEmpty());
    }

    @Test
    void test_getX509TrustManager_when_holders_is_not_empty_then_returns_list() throws Exception {
        SMIMESigned signed = Mockito.mock();
        Store<X509CertificateHolder> holders = Mockito.mock();
        Mockito.when(signed.getCertificates()).thenReturn(holders);
        X509CertificateHolder x509CertificateHolder = Mockito.mock();
        Mockito.when(x509CertificateHolder.getEncoded()).thenReturn("test".getBytes());
        Mockito.when(holders.getMatches(null)).thenReturn(List.of(x509CertificateHolder));
        Assertions.assertEquals(1, SmimeHandlerImpl.getX509Certificates(signed).size());
    }
}
