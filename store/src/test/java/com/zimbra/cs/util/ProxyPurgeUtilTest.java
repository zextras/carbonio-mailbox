package com.zimbra.cs.util;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.cli.CommandExitException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProxyPurgeUtilTest extends MailboxTestSuite {

  @Test
  void shouldLogInfoByDefault()  {
    Assertions.assertThrows(CommandExitException.class, () -> ProxyPurgeUtil.run(new String[] {""}));
    final Logger rootLogger = org.apache.logging.log4j.LogManager.getRootLogger();
    Assertions.assertEquals(Level.INFO, rootLogger.getLevel());
  }

  @Test
  void shouldLogDebugWhenVerbose() {
    Assertions.assertThrows(CommandExitException.class, () -> ProxyPurgeUtil.run(new String[] {"-v"}));
    final Logger rootLogger = org.apache.logging.log4j.LogManager.getRootLogger();
    Assertions.assertEquals(Level.DEBUG, rootLogger.getLevel());
  }

  @Test
  void shouldUseProxyUpstreamOnProxyNode() throws Exception {
    final var servers = ProxyPurgeUtil.getCacheServers(
        ProxyPurgeUtil.parseCommandLine(new String[] {"-a", "user@test.com"}), createProxyServer());
    Assertions.assertEquals(List.of("127.78.0.1:20014"), servers);
  }

  @Test
  void shouldUseMailboxUpstreamOnMailboxNode() throws Exception {
    final var servers = ProxyPurgeUtil.getCacheServers(
        ProxyPurgeUtil.parseCommandLine(new String[] {"-a", "user@test.com"}), createMailboxServer());
    Assertions.assertEquals(List.of("127.78.0.7:20006"), servers);
  }

  @Test
  void shouldUseMemcachedEndpointAttributeOnMailboxNode() throws Exception {
    final var prov = Provisioning.getInstance();
    final var config = prov.getConfig();
    prov.modifyAttrs(config, new HashMap<>(Map.of(ZAttrProvisioning.A_carbonioMemcachedEndpoint, "memcached:11211")));
    try {
      final var servers = ProxyPurgeUtil.getCacheServers(
          ProxyPurgeUtil.parseCommandLine(new String[] {"-a", "user@test.com"}), createMailboxServer());
      Assertions.assertEquals(List.of("memcached:11211"), servers);
    } finally {
      prov.modifyAttrs(config, new HashMap<>(Map.of(ZAttrProvisioning.A_carbonioMemcachedEndpoint, "")));
    }
  }

  @Test
  void shouldPreferServersFromCommandLine() throws Exception {
    final var servers = ProxyPurgeUtil.getCacheServers(
        ProxyPurgeUtil.parseCommandLine(new String[] {"-a", "user@test.com", "cache1:11211"}), createProxyServer());
    Assertions.assertEquals(List.of("cache1:11211"), servers);
  }

  private static Server createMailboxServer() throws Exception {
    final String serverName = UUID.randomUUID() + ".com";
    return Provisioning.getInstance().createServer(serverName, new HashMap<>(Map.of(
        ZAttrProvisioning.A_cn, serverName,
        ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_MAILBOX)));
  }
}