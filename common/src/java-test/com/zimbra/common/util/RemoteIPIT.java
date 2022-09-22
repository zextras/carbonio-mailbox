package com.zimbra.common.util;

import static com.zimbra.common.util.RemoteIP.X_ORIGINATING_IP_HEADER;
import static com.zimbra.common.util.RemoteIP.X_ORIGINATING_PORT_HEADER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zimbra.common.util.RemoteIP.TrustedIPs;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RemoteIPIT {

  @Before
  public void clearLogContext() {
    ZimbraLog.clearContext();
  }

  @Test
  public void shouldAddClientIpAndPortToLogWhenNotLocalhost() {
    final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    final String remoteAddr = "192.168.10.7";
    final int remotePort = 8080;
    when(httpServletRequest.getRemoteAddr()).thenReturn(remoteAddr);
    when(httpServletRequest.getRemotePort()).thenReturn(remotePort);
    new RemoteIP(httpServletRequest, new TrustedIPs(new String[] {})).addToLoggingContext();
    final String contextString = ZimbraLog.getContextString();
    Assert.assertEquals("ip=" + remoteAddr + ";port=" + remotePort + ";", contextString);
  }

  @Test
  public void shouldNotAddClientIpAndClientPortWhenRemoteAddressLocalhost() {
    final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    final String remoteAddr = "127.0.0.1";
    final int remotePort = 8080;
    when(httpServletRequest.getRemoteAddr()).thenReturn(remoteAddr);
    when(httpServletRequest.getRemotePort()).thenReturn(remotePort);
    new RemoteIP(httpServletRequest, new TrustedIPs(new String[] {})).addToLoggingContext();
    final String contextString = ZimbraLog.getContextString();
    Assert.assertNull(contextString);
  }

  @Test
  public void shouldAddOrigIpAndOrigPortWhenRemoteAddressTrusted() {
    final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    final String remoteAddr = "172.18.18.20";
    final int remotePort = 8080;
    final String origIp = "192.168.10.7";
    final int origPort = 9090;

    when(httpServletRequest.getRemoteAddr()).thenReturn(remoteAddr);
    when(httpServletRequest.getRemotePort()).thenReturn(remotePort);
    when(httpServletRequest.getHeader(X_ORIGINATING_IP_HEADER)).thenReturn(origIp);
    when(httpServletRequest.getHeader(X_ORIGINATING_PORT_HEADER))
        .thenReturn(Integer.toString(origPort));
    new RemoteIP(httpServletRequest, new TrustedIPs(new String[] {remoteAddr}))
        .addToLoggingContext();
    final String contextString = ZimbraLog.getContextString();
    // Logging context is unordered, so no way to assert equals
    Assert.assertTrue(contextString.contains("ip=" + remoteAddr));
    Assert.assertTrue(contextString.contains("port=" + remotePort));
    Assert.assertTrue(contextString.contains("oport=" + origPort));
    Assert.assertTrue(contextString.contains("oip=" + origIp));
  }
}
