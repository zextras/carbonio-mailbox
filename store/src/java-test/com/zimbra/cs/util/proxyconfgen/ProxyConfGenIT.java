package com.zimbra.cs.util.proxyconfgen;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class ProxyConfGenIT {
  @Rule
  public GenericContainer redis =
      new GenericContainer(DockerImageName.parse("carbonio/ce-ldap-u20:latest"))
          .withCreateContainerCmdModifier(it -> it.withHostName("ldap.mail.local"))
          .withExposedPorts(389)
          .waitingFor(new HostPortWaitStrategy())
          .withExtraHost("mail.local", "127.0.0.1");

  @Test
  public void shouldDownloadCertificatesFromLDAP() throws Exception {
    ProxyConfGen.main(new String[] {});
  }
}
