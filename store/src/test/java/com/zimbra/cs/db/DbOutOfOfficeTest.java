// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.mail.OutOfOfficeRepository;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailboxManager;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DbOutOfOfficeTest extends MailboxTestSuite {

  private static final String RECIPIENT = "sender@example.com";
  private static final Instant NOW = Instant.now();
  private static final Instant ONE_HOUR_AGO = NOW.minus(Duration.ofHours(1));
  private static final Instant TWO_HOURS_AGO = NOW.minus(Duration.ofHours(2));

  private final OutOfOfficeRepository repository = new DbOutOfOffice();
  private int mailboxId;

  @BeforeEach
  void createMailbox() throws Exception {
    mailboxId = newMailboxId();
  }

  @Test
  void nothingIsSentAtFirst() throws Exception {
    assertFalse(repository.alreadySent(mailboxId, RECIPIENT, ONE_HOUR_AGO));
  }

  @Test
  void sendAfterSinceIsAlreadySent() throws Exception {
    repository.setSentTime(mailboxId, RECIPIENT, NOW);

    assertTrue(repository.alreadySent(mailboxId, RECIPIENT, ONE_HOUR_AGO));
  }

  @Test
  void sendBeforeSinceIsNotAlreadySent() throws Exception {
    repository.setSentTime(mailboxId, RECIPIENT, TWO_HOURS_AGO);

    assertFalse(repository.alreadySent(mailboxId, RECIPIENT, ONE_HOUR_AGO));
  }

  @Test
  void secondSendUpdatesTheSentTime() throws Exception {
    repository.setSentTime(mailboxId, RECIPIENT, TWO_HOURS_AGO);
    repository.setSentTime(mailboxId, RECIPIENT, NOW);

    assertTrue(repository.alreadySent(mailboxId, RECIPIENT, ONE_HOUR_AGO));
  }

  @Test
  void recipientIgnoresCase() throws Exception {
    repository.setSentTime(mailboxId, "Sender@Example.COM", NOW);

    assertTrue(repository.alreadySent(mailboxId, "SENDER@example.com", ONE_HOUR_AGO));
  }

  @Test
  void clearRemovesOnlyThatMailbox() throws Exception {
    int otherMailboxId = newMailboxId();
    repository.setSentTime(mailboxId, RECIPIENT, NOW);
    repository.setSentTime(otherMailboxId, RECIPIENT, NOW);

    repository.clear(mailboxId);

    assertFalse(repository.alreadySent(mailboxId, RECIPIENT, ONE_HOUR_AGO));
    assertTrue(repository.alreadySent(otherMailboxId, RECIPIENT, ONE_HOUR_AGO));
  }

  @Test
  void pruneRemovesOnlyEntriesUpToTheCutoff() throws Exception {
    repository.setSentTime(mailboxId, "old@example.com", TWO_HOURS_AGO);
    repository.setSentTime(mailboxId, "recent@example.com", NOW);

    repository.prune(ONE_HOUR_AGO);

    assertFalse(repository.alreadySent(mailboxId, "old@example.com", Instant.EPOCH));
    assertTrue(repository.alreadySent(mailboxId, "recent@example.com", Instant.EPOCH));
  }

  private int newMailboxId() throws Exception {
    Account account = createAccount().create();
    return MailboxManager.getInstance().getMailboxByAccountId(account.getId()).getId();
  }
}
