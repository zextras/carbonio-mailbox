package com.zimbra.cs.util.calltohome;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CallToHomeRunnerTest {

  private static CallToHomeRunner callToHomeRunner;

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    callToHomeRunner = CallToHomeRunner.getInstance();
  }

  @AfterAll
  public static void tearDown() throws ServiceException {
    MailboxTestUtil.tearDown();
    callToHomeRunner.stop();
  }

  @AfterEach
   void stop() {
     callToHomeRunner.stop();
  }

  @Test
  void testInitAndStop() throws InterruptedException {
    assertFalse(callToHomeRunner.isStarted(), "Initially should not be started");

    callToHomeRunner.init(TimeUnit.SECONDS.toMillis(1));
    TimeUnit.SECONDS.sleep(2);

    assertTrue(callToHomeRunner.isStarted(), "After init, should be started");

    callToHomeRunner.stop();
    assertFalse(callToHomeRunner.isStarted(), "After stop, should not be started");
  }

  @Test
  void testInitTwice() throws InterruptedException {
    assertFalse(callToHomeRunner.isStarted(), "Initially should not be started");

    callToHomeRunner.init(TimeUnit.SECONDS.toMillis(1));
    TimeUnit.SECONDS.sleep(2);

    assertTrue(callToHomeRunner.isStarted(), "After first init, should be started");

    callToHomeRunner.init(TimeUnit.SECONDS.toMillis(1));
    assertTrue(callToHomeRunner.isStarted(), "After second init, should still be started");
  }

  @Test
  void testStopWithoutInit() {
    assertFalse(callToHomeRunner.isStarted(), "Initially should not be started");

    callToHomeRunner.stop();
    assertFalse(callToHomeRunner.isStarted(), "After stop without init, should still not be started");
  }

  @Test
  void testInitAndStopDelay() throws InterruptedException {
    assertFalse(callToHomeRunner.isStarted(), "Initially should not be started");

    callToHomeRunner.init(TimeUnit.SECONDS.toMillis(1));
    TimeUnit.SECONDS.sleep(2);

    assertTrue(callToHomeRunner.isStarted(), "After init, should be started");

    callToHomeRunner.stop();
    assertFalse(callToHomeRunner.isStarted(), "After stop, should not be started");
  }
}
