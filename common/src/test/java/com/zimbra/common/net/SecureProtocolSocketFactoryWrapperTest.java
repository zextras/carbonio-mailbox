package com.zimbra.common.net;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.net.Socket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SecureProtocolSocketFactoryWrapperTest {

  @AfterEach
  void resetSocks() {
    NetConfig.getInstance().setSocksEnabled(false);
  }

  @Test
  void createsSocksAwareSocketWhenSocksEnabled() throws Exception {
    NetConfig.getInstance().setSocksEnabled(true);
    var wrapper = new SecureProtocolSocketFactoryWrapper(SocketFactories.dummySSLSocketFactory());

    try (Socket socket = wrapper.createSocket(null)) {
      assertInstanceOf(ProxySelectorSocket.class, socket);
    }
  }
}
