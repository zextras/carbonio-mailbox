package com.zimbra.cs.clamscanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.clamscanner.client.ClamAVClient;
import org.junit.jupiter.api.Test;

public class ClamAVClientTest {

  @Test
  public void testIsCleanReply_WhenCleanReply_ReturnsTrue() {
    byte[] reply = "OK".getBytes();
    boolean result = ClamAVClient.replyOk(reply);
    assertTrue(result);
  }

  @Test
  public void testIsCleanReply_WhenInfectedReply_ReturnsFalse() {
    byte[] reply = "FOUND".getBytes();
    boolean result = ClamAVClient.replyOk(reply);
    assertFalse(result);
  }
}
