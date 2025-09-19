package com.zimbra.cs.util.calltohome;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxTestSuite;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CallToHomeRunnerTest extends MailboxTestSuite {

  private static CallToHomeRunner callToHomeRunner;

  @BeforeAll
  public static void setUp() {
    callToHomeRunner = CallToHomeRunner.getInstance();
  }

  @AfterAll
  public static void tearDown(){
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
