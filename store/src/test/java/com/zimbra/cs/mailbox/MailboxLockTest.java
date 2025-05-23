// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.client.ZMailboxLock;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox.FolderNode;
import com.zimbra.cs.mailbox.MailboxLock.LockFailedException;
import com.zimbra.cs.service.util.ItemId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MailboxLockTest {
 private static Account account;
    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        account = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @BeforeEach
    public void setup() throws Exception {
        MailboxTestUtil.clearData();
        MailboxManager.getInstance().getMailboxByAccountId(account.getId());
    }

 @Test
 void badWriteWhileHoldingRead() throws ServiceException {
  boolean check = false;
  assert (check = true);
  if (check) {
   Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
   mbox.lock.lock(false);
   assertFalse(mbox.lock.isUnlocked());
   assertFalse(mbox.lock.isWriteLockedByCurrentThread());
   boolean good = true;
   try {
    mbox.lock.lock(true);
    good = false;
   } catch (AssertionError e) {
    //expected
   }
   assertTrue(good);
  } else {
   ZimbraLog.test.debug("skipped testWriteWhileHoldingRead since asserts are not enabled");
   //without this the test times out eventually, but we want tests to be fast so skip this one
  }
 }

 @Test
 void nestedWrite() throws ServiceException {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  int holdCount = 0;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.lock(true);
  holdCount++;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  assertFalse(mbox.lock.isUnlocked());
  assertTrue(mbox.lock.isWriteLockedByCurrentThread());
  mbox.lock.lock(false);
  holdCount++;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.lock(true);
  holdCount++;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.lock(false);
  holdCount++;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.lock(true);
  holdCount++;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.lock(true);
  holdCount++;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.lock(true);
  holdCount++;
  assertEquals(holdCount, mbox.lock.getHoldCount());

  mbox.lock.release();
  holdCount--;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.release();
  holdCount--;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.release();
  holdCount--;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.release();
  holdCount--;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.release();
  holdCount--;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.release();
  holdCount--;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  mbox.lock.release();
  holdCount--;
  assertEquals(holdCount, mbox.lock.getHoldCount());
  assertEquals(0, holdCount);
 }

 @Test
 void multiAccess() throws ServiceException {
  final Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());

  //just do some read/write in different threads to see if we trigger any deadlocks or other badness
  int numThreads = 5;
  final int loopCount = 10;
  final long sleepTime = 10;
  int joinTimeout = 10000;

  List<Thread> threads = new ArrayList<Thread>(numThreads * 2);
  for (int i = 0; i < numThreads; i++) {
   String threadName = "MailboxLockTest-MultiReader-" + i;
   Thread reader = new Thread(threadName) {
    @Override
    public void run() {
     for (int i = 0; i < loopCount; i++) {
      mbox.lock.lock(false);
      try {
       ItemId iid = new ItemId(mbox, Mailbox.ID_FOLDER_USER_ROOT);
       FolderNode node = mbox.getFolderTree(null, iid, true);
      } catch (ServiceException e) {
       e.printStackTrace();
       fail("ServiceException");
      }
      try {
       Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
      }
      mbox.lock.release();
     }
    }
   };
   threads.add(reader);

   threadName = "MailboxLockTest-MultiWriter-" + i;
   Thread writer = new Thread(threadName) {
    @Override
    public void run() {
     for (int i = 0; i < loopCount; i++) {
      mbox.lock.lock(true);
      try {
       mbox.createFolder(null, "foo-" + Thread.currentThread().getName() + "-" + i, new Folder.FolderOptions().setDefaultView(MailItem.Type.MESSAGE));
      } catch (ServiceException e) {
       e.printStackTrace();
       fail("ServiceException");
      }
      mbox.lock.release();
      try {
       Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
      }
     }
    }
   };
   threads.add(writer);
//            writer.start();
//            reader.start();
  }

  for (Thread t : threads) {
   t.start();
  }
  for (Thread t : threads) {
   try {
    t.join(joinTimeout);
    assertFalse(t.isAlive());
   } catch (InterruptedException e) {
   }
  }
 }

 @Test
 void promote() {
  final Thread readThread = new Thread("MailboxLockTest-Reader") {
   @Override
   public void run() {
    Mailbox mbox;
    try {
     int lockCount = 10;
     mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
     //here's the interleaving we are explicitly exercising in this test
     //1. writer - mbox.lock(write)
     //2. reader - mbox.lock(read); call gets past the initial isWriteModeRequired() check and into tryLock(read)
     //3. writer - mbox.purge()
     //4. writer - mbox.unlock()
     //5. reader - tryLock(read) returns, then recheck isWriteModeRequired() and promote

     assertTrue(mbox.lock.isUnlocked());
     for (int i = 0; i < lockCount; i++) {
      mbox.lock.lock(false);
      //loop so we exercise recursion in promote..
     }
     assertTrue(mbox.lock.isWriteLockedByCurrentThread());
     //we're guaranteeing that reader lock is not held before writer
     //but not guaranteeing that purge is called while reader is waiting
     //i.e. if purge/release happens in writeThread before we actually get to lock call in this thread
     //subtle, and shouldn't matter since promote is called either way, but if we see races in test this could be cause
     mbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
     for (int i = 0; i < lockCount; i++) {
      mbox.lock.release();
     }
    } catch (ServiceException e) {
     e.printStackTrace();
     fail();
    }
   }
  };
  readThread.setDaemon(true);

  final Thread writeThread = new Thread("MailboxLockTest-Writer") {

   @Override
   public void run() {
    Mailbox mbox;
    try {
     mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
     mbox.lock.lock();
     //start read thread only after holding mailbox lock
     readThread.start();
     //wait until read thread has tried to obtain mailbox lock
     while (!mbox.lock.hasQueuedThreads()) {
      Thread.sleep(10);
     }
     mbox.purge(MailItem.Type.FOLDER);
     mbox.lock.release();
    } catch (ServiceException | InterruptedException e) {
     e.printStackTrace();
     fail();
    }
   }
  };
  writeThread.setDaemon(true);


  writeThread.start();

  int joinTimeout = 10000; //use a timeout so test can fail gracefully in case we have a deadlock
  try {
   writeThread.join(joinTimeout);
   if (writeThread.isAlive()) {
    System.out.println("Write Thread");
    for (StackTraceElement ste : writeThread.getStackTrace()) {
     System.out.println(ste);
    }
    if (readThread.isAlive()) {
     System.out.println("Read Thread");
     for (StackTraceElement ste : readThread.getStackTrace()) {
      System.out.println(ste);
     }
    }
   }
   assertFalse(writeThread.isAlive());
  } catch (InterruptedException e) {
   e.printStackTrace();
  }
  try {
   readThread.join(joinTimeout);
   assertFalse(readThread.isAlive());
  } catch (InterruptedException e) {
   e.printStackTrace();
  }
 }

    private void joinWithTimeout(Thread thread, long timeout) {
        try {
            thread.join(timeout);
        } catch (InterruptedException e) {
        }
        assertFalse(thread.isAlive());
    }

 @Test
 void tooManyWaiters() {
  Mailbox mbox = null;
  try {
   mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  } catch (ServiceException e) {
   fail();
  }

  int threads = LC.zimbra_mailbox_lock_max_waiting_threads.intValue();
  final AtomicBoolean done = new AtomicBoolean(false);
  final Set<Thread> waitThreads = new HashSet<Thread>();
  for (int i = 0; i < threads; i++) {
   Thread waitThread = new Thread("MailboxLockTest-Waiter") {
    @Override
    public void run() {
     Mailbox mbox;
     try {
      mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
      mbox.lock.lock(false);
      while (!done.get()) {
       try {
        Thread.sleep(100);
       } catch (InterruptedException e) {
       }
      }
      mbox.lock.release();
     } catch (ServiceException e) {
     }
    }
   };
   waitThread.setDaemon(true);
   waitThreads.add(waitThread);
  }

  Thread writeThread = new Thread("MailboxLockTest-Writer") {
   @Override
   public void run() {
    Mailbox mbox;
    try {
     mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
     mbox.lock.lock(true);
     for (Thread waiter : waitThreads) {
      waiter.start();
     }
     while (!done.get()) {
      try {
       Thread.sleep(100);
      } catch (InterruptedException e) {
      }
     }
     mbox.lock.release();
    } catch (ServiceException e) {
    }
   }
  };

  writeThread.start();

  while (mbox.lock.getQueueLength() < LC.zimbra_mailbox_lock_max_waiting_threads.intValue()) {
   try {
    Thread.sleep(100);
   } catch (InterruptedException e) {
   }
  }

  try {
   mbox.lock.lock(false); //one more reader...this should give too many waiters
   fail("expected too many waiters");
  } catch (LockFailedException e) {
   //expected
   assertTrue(e.getMessage().startsWith("too many waiters"));
   done.set(true); //cause writer to finish
  }

  long joinTimeout = 50000;
  joinWithTimeout(writeThread, joinTimeout);
  for (Thread t : waitThreads) {
   joinWithTimeout(t, joinTimeout);
  }

  //now do a write lock in same thread. previously this would break due to read assert not clearing
  mbox.lock.lock(true);
  mbox.lock.release();
 }

 @Test
 void tooManyWaitersWithSingleReadOwner() {
  Mailbox mbox = null;
  try {
   mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  } catch (ServiceException e) {
   fail();
  }

  int threads = LC.zimbra_mailbox_lock_max_waiting_threads.intValue();
  final AtomicBoolean done = new AtomicBoolean(false);
  final Set<Thread> waitThreads = new HashSet<Thread>();
  for (int i = 0; i < threads; i++) {
   Thread waitThread = new Thread("MailboxLockTest-Waiter") {
    @Override
    public void run() {
     Mailbox mbox;
     try {
      mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
      mbox.lock.lock(true);
      while (!done.get()) {
       try {
        Thread.sleep(100);
       } catch (InterruptedException e) {
       }
      }
      mbox.lock.release();
     } catch (ServiceException e) {
     }
    }
   };
   waitThread.setDaemon(true);
   waitThreads.add(waitThread);
  }

  Thread readThread = new Thread("MailboxLockTest-Reader") {
   @Override
   public void run() {
    Mailbox mbox;
    try {
     mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
     int holdCount = 20;
     for (int i = 0; i < holdCount; i++) {
      mbox.lock.lock(false);
     }
     for (Thread waiter : waitThreads) {
      waiter.start();
     }
     while (!done.get()) {
      try {
       Thread.sleep(100);
      } catch (InterruptedException e) {
      }
     }
     for (int i = 0; i < holdCount; i++) {
      mbox.lock.release();
     }
    } catch (ServiceException e) {
    }
   }
  };

  readThread.start();

  while (mbox.lock.getQueueLength() < LC.zimbra_mailbox_lock_max_waiting_threads.intValue()) {
   try {
    Thread.sleep(100);
   } catch (InterruptedException e) {
   }
  }

  try {
   mbox.lock.lock(false); //one more reader...this should give too many waiters
   fail("expected too many waiters");
  } catch (LockFailedException e) {
   //expected
   assertTrue(e.getMessage().startsWith("too many waiters"));
   done.set(true); //cause writer to finish
  }

  long joinTimeout = 50000;
  joinWithTimeout(readThread, joinTimeout);
  for (Thread t : waitThreads) {
   joinWithTimeout(t, joinTimeout);
  }

  //now do a write lock in same thread. previously this would break due to read assert not clearing
  mbox.lock.lock(true);
  mbox.lock.release();
 }


 @Test
 void tooManyWaitersWithMultipleReadOwners() {
  Mailbox mbox = null;
  try {
   mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
  } catch (ServiceException e) {
   fail();
  }

  int threads = LC.zimbra_mailbox_lock_max_waiting_threads.intValue();
  final AtomicBoolean done = new AtomicBoolean(false);
  final Set<Thread> waitThreads = new HashSet<Thread>();
  for (int i = 0; i < threads; i++) {
   Thread waitThread = new Thread("MailboxLockTest-Waiter-" + i) {
    @Override
    public void run() {
     Mailbox mbox;
     try {
      mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
      mbox.lock.lock(true);
      while (!done.get()) {
       try {
        Thread.sleep(100);
       } catch (InterruptedException e) {
       }
      }
      mbox.lock.release();
     } catch (ServiceException e) {
     }
    }
   };
   waitThread.setDaemon(true);
   waitThreads.add(waitThread);
  }

  int readThreadCount = 20;
  final Set<Thread> readThreads = new HashSet<Thread>();
  for (int i = 0; i < readThreadCount; i++) {
   Thread readThread = new Thread("MailboxLockTest-Reader-" + i) {
    @Override
    public void run() {
     Mailbox mbox;
     try {
      mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
      int holdCount = 20;
      for (int i = 0; i < holdCount; i++) {
       mbox.lock.lock(false);
      }
      while (!done.get()) {
       try {
        Thread.sleep(100);
       } catch (InterruptedException e) {
       }
      }
      for (int i = 0; i < holdCount; i++) {
       mbox.lock.release();
      }
     } catch (ServiceException e) {
     }
    }
   };
   readThreads.add(readThread);
  }


  Thread lastReadThread = new Thread("MailboxLockTest-LastReader") {
   @Override
   public void run() {
    Mailbox mbox;
    try {
     mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
     int holdCount = 20;
     for (int i = 0; i < holdCount; i++) {
      mbox.lock.lock(false);
     }
     //this thread starts the waiters
     //and the other readers
     for (Thread reader : readThreads) {
      reader.start();
     }
     for (Thread waiter : waitThreads) {
      waiter.start();
     }
     while (!done.get()) {
      try {
       Thread.sleep(100);
      } catch (InterruptedException e) {
      }
     }
     for (int i = 0; i < holdCount; i++) {
      mbox.lock.release();
     }
    } catch (ServiceException e) {
    }
   }
  };

  lastReadThread.start();

  while (mbox.lock.getQueueLength() < LC.zimbra_mailbox_lock_max_waiting_threads.intValue()) {
   try {
    Thread.sleep(100);
   } catch (InterruptedException e) {
   }
  }

  try {
   mbox.lock.lock(false); //one more reader...this should give too many waiters
   fail("expected too many waiters");
  } catch (LockFailedException e) {
   //expected
   assertTrue(e.getMessage().startsWith("too many waiters"));
   done.set(true); //cause writer to finish
  }

  long joinTimeout = 50000;
  joinWithTimeout(lastReadThread, joinTimeout);
  for (Thread t : readThreads) {
   joinWithTimeout(t, joinTimeout);
  }
  for (Thread t : waitThreads) {
   joinWithTimeout(t, joinTimeout);
  }

  //now do a write lock in same thread. previously this would break due to read assert not clearing
  mbox.lock.lock(true);
  mbox.lock.release();
 }

 @Test
 void testZMailboxReenter() throws Exception {
  ZMailboxLock lock = new ZMailboxLock(1, 1);
  for (int i = 0; i < 3; i++) {
   lock.lock();
  }
  assertEquals(3, lock.getHoldCount());
  for (int i = 0; i < 3; i++) {
   lock.release();
  }
  assertEquals(0, lock.getHoldCount());
 }

 @Test
 void testZMailboxLockTimeout() throws Exception {
  int maxNumThreads = 3;
  int timeout = 0;
  ZMailboxLock lock = new ZMailboxLock(maxNumThreads, timeout);
  Thread thread = new Thread(String.format("MailboxLockTest-ZMailbox")) {
   @Override
   public void run() {
    lock.lock();
    try {
     Thread.sleep(1000);
    } catch (InterruptedException e) {
     e.printStackTrace();
    } finally {
     lock.release();
    }
   }
  };
  thread.setDaemon(true);
  thread.start();
  Thread.sleep(100);
  try {
   lock.lock();
   fail("should not be able to acquire the lock; should time out");
  } catch (com.zimbra.client.ZMailboxLock.LockFailedException e) {
   assertTrue(e.getMessage().startsWith("lock timeout"));
  }
  thread.join();
 }

 @Test
 void testZMailboxLockTooManyWaiters() throws Exception {
  int maxNumThreads = 3;
  int timeout = 10;
  ZMailboxLock lock = new ZMailboxLock(maxNumThreads, timeout);
  final Set<Thread> threads = new HashSet<Thread>();
  for (int i = 0; i < maxNumThreads + 1; i++) {
   // one thread will acquire the lock, 3 will wait
   Thread thread = new Thread(String.format("MailboxLockTest-ZMailbox-%s", i)) {
    @Override
    public void run() {
     lock.lock();
     try {
      Thread.sleep(500);
     } catch (InterruptedException e) {
      e.printStackTrace();
     }
     lock.release();
    }
   };
   thread.setDaemon(true);
   threads.add(thread);
  }
  for (Thread t : threads) {
   t.start();
  }
  Thread.sleep(100);
  try {
   lock.lock();
   fail("should not be able to acquire lock due to too many waiting threads");
  } catch (com.zimbra.client.ZMailboxLock.LockFailedException e) {
   assertTrue(e.getMessage().startsWith("too many waiters"));
  }
  for (Thread t : threads) {
   t.join();
  }
 }
}
