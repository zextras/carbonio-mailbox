package com.zimbra.cs.util;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.cli.CommandExitException;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    final var prov = Provisioning.getInstance();
    final var localServer = prov.getLocalServer();
    prov.modifyAttrs(localServer, new HashMap<>(Map.of("+" + ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_PROXY)));
    try {
      final var servers = ProxyPurgeUtil.getCacheServers(
          ProxyPurgeUtil.parseCommandLine(new String[] {"-a", "user@test.com"}), prov);
      Assertions.assertEquals(List.of("127.78.0.1:20014"), servers);
    } finally {
      prov.modifyAttrs(localServer, new HashMap<>(Map.of("-" + ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_PROXY)));
    }
  }

  @Test
  void shouldUseMailboxUpstreamOnMailboxNode() throws Exception {
    final var prov = Provisioning.getInstance();
    final var localServer = prov.getLocalServer();
    prov.modifyAttrs(localServer, new HashMap<>(Map.of("+" + ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_MAILBOX)));
    try {
      final var servers = ProxyPurgeUtil.getCacheServers(
          ProxyPurgeUtil.parseCommandLine(new String[] {"-a", "user@test.com"}), prov);
      Assertions.assertEquals(List.of("127.78.0.7:20006"), servers);
    } finally {
      prov.modifyAttrs(localServer, new HashMap<>(Map.of("-" + ZAttrProvisioning.A_zimbraServiceEnabled, Provisioning.SERVICE_MAILBOX)));
    }
  }

  @Test
  void shouldFailWithoutServersOnNodeWithoutProxyOrMailbox() throws Exception {
    final var commandLine = ProxyPurgeUtil.parseCommandLine(new String[] {"-a", "user@test.com"});
    final var prov = Provisioning.getInstance();
    Assertions.assertThrows(CommandExitException.class, () -> ProxyPurgeUtil.getCacheServers(commandLine, prov));
  }

  @Test
  void shouldPreferServersFromCommandLine() throws Exception {
    final var servers = ProxyPurgeUtil.getCacheServers(
        ProxyPurgeUtil.parseCommandLine(new String[] {"-a", "user@test.com", "cache1:11211"}), Provisioning.getInstance());
    Assertions.assertEquals(List.of("cache1:11211"), servers);
  }
}
