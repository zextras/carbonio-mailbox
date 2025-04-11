package com.zimbra.cs.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class HttpProxyUtilTest {

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
  }

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @Test
  void testConstructor() throws NoSuchMethodException {
    Constructor<HttpProxyUtil> constructor = HttpProxyUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
      try {
        constructor.newInstance();
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
    });

    assertEquals("Utility class", thrown.getMessage());
  }

  @Test
  void testConfigureProxy_noOrNullProxyUrl_doesNothing() throws ServiceException {
    Provisioning.getInstance().getLocalServer().setHttpProxyURL(null);

    HttpClientBuilder httpClientBuilderSpy = Mockito.spy(HttpClientBuilder.create());
    HttpProxyUtil.configureProxy(httpClientBuilderSpy);

    verify(httpClientBuilderSpy, never()).setDefaultRequestConfig(Mockito.any(RequestConfig.class));
    verify(httpClientBuilderSpy, never()).setDefaultCredentialsProvider(Mockito.any(CredentialsProvider.class));
  }

  @Test
  void testConfigureProxy_withProxyUrl_setsProxy() throws Exception {
    Provisioning.getInstance().getLocalServer().setHttpProxyURL("http://user:pass@proxyhost:8080");

    HttpClientBuilder httpClientBuilderSpy = Mockito.spy(HttpClientBuilder.create());
    HttpProxyUtil.configureProxy(httpClientBuilderSpy);

    ArgumentCaptor<RequestConfig> configCaptor = ArgumentCaptor.forClass(RequestConfig.class);
    verify(httpClientBuilderSpy).setDefaultRequestConfig(configCaptor.capture());
    RequestConfig config = configCaptor.getValue();

    HttpHost proxy = config.getProxy();
    Assertions.assertNotNull(proxy);
    assertEquals("proxyhost", proxy.getHostName());
    assertEquals(8080, proxy.getPort());

    ArgumentCaptor<CredentialsProvider> credentialsCaptor = ArgumentCaptor.forClass(CredentialsProvider.class);
    verify(httpClientBuilderSpy).setDefaultCredentialsProvider(credentialsCaptor.capture());
    CredentialsProvider credentialsProvider = credentialsCaptor.getValue();

    UsernamePasswordCredentials creds = (UsernamePasswordCredentials) credentialsProvider.getCredentials(AuthScope.ANY);
    Assertions.assertNotNull(creds);
    assertEquals("user", creds.getUserName());
    assertEquals("pass", creds.getPassword());
  }

  @Test
  void testConfigureProxy_withProxyUrlWithoutCredentials_setsProxy() throws Exception {
    Provisioning.getInstance().getLocalServer().setHttpProxyURL("http://proxyhost:8080");

    HttpClientBuilder httpClientBuilderSpy = Mockito.spy(HttpClientBuilder.create());
    HttpProxyUtil.configureProxy(httpClientBuilderSpy);

    ArgumentCaptor<RequestConfig> configCaptor = ArgumentCaptor.forClass(RequestConfig.class);
    verify(httpClientBuilderSpy).setDefaultRequestConfig(configCaptor.capture());
    RequestConfig config = configCaptor.getValue();

    HttpHost proxy = config.getProxy();
    Assertions.assertNotNull(proxy);
    assertEquals("proxyhost", proxy.getHostName());
    assertEquals(8080, proxy.getPort());
  }

  @Test
  void testConfigureProxy_malformedUrl_doesNothing() throws ServiceException {
    Provisioning.getInstance().getLocalServer().setHttpProxyURL("proxyhost/8080");

    HttpClientBuilder httpClientBuilderSpy = Mockito.spy(HttpClientBuilder.create());
    HttpProxyUtil.configureProxy(httpClientBuilderSpy);

    verify(httpClientBuilderSpy, never()).setDefaultRequestConfig(Mockito.any(RequestConfig.class));
    verify(httpClientBuilderSpy, never()).setDefaultCredentialsProvider(Mockito.any(CredentialsProvider.class));
  }

  @Test
  void testConfigureProxy_emptyProxyUrl_doesNothing() throws ServiceException {
    Provisioning.getInstance().getLocalServer().setHttpProxyURL("");

    HttpClientBuilder httpClientBuilderSpy = Mockito.spy(HttpClientBuilder.create());
    HttpProxyUtil.configureProxy(httpClientBuilderSpy);

    verify(httpClientBuilderSpy, never()).setDefaultRequestConfig(Mockito.any(RequestConfig.class));
    verify(httpClientBuilderSpy, never()).setDefaultCredentialsProvider(Mockito.any(CredentialsProvider.class));
  }
}