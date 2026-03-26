# Quota Check Analysis: `checkSizeChange()` and Send/Receive Flow

## Overview

This document analyzes how mailbox quota enforcement works in Carbonio Mailbox,
focusing on `checkSizeChange()`, the `zimbraMailAllowReceiveButNotSendWhenOverQuota` flag,
and the send/receive message flows.

## `checkSizeChange()` — Definition

Located in `Mailbox.java:1329`:

```java
public void checkSizeChange(long newSize) throws ServiceException {
    Account acct = getAccount();
    long acctQuota = AccountUtil.getEffectiveQuota(acct);
    if (acctQuota != 0 && newSize > acctQuota) {
        throw MailServiceException.QUOTA_EXCEEDED(acctQuota);
    }
    Domain domain = Provisioning.getInstance().getDomain(acct);
    if (domain != null
        && AccountUtil.isOverAggregateQuota(domain)
        && !AccountUtil.isReceiveAllowedOverAggregateQuota(domain)) {
        throw MailServiceException.DOMAIN_QUOTA_EXCEEDED(domain.getDomainAggregateQuota());
    }
}
```

It checks both **account-level** and **domain-level aggregate** quotas.

## `checkSizeChange()` — Call Sites

| # | Location | Method | Operation Type |
|---|----------|--------|----------------|
| 1 | `Mailbox.java:1321` | `updateSize(delta, checkQuota)` | **Generic** — any item size increase |
| 2 | `Mailbox.java:6996` | `addMessageInternal()` | **Receive** — preemptive check on message delivery |
| 3 | `Mailbox.java:7878, 7884` | `copyInternal()` | **Copy** — copying items between folders |
| 4 | `Mailbox.java:7999` | `imapCopy()` | **IMAP COPY** command |

### Important: Only fires on size increases

`updateSize()` (`Mailbox.java:1314`) guards the check with `delta > 0`:

```java
void updateSize(long delta, boolean checkQuota) throws ServiceException {
    if (delta == 0) return;
    long size = getEffectiveSize(delta);
    if (delta > 0 && checkQuota) {
        checkSizeChange(size);
    }
    // ...
}
```

**Deletions always produce negative deltas**, so `checkSizeChange()` is never invoked
when deleting items. Users can always delete messages even when over quota.

### The `isQuotaCheckRequired()` mechanism

`updateSize()` receives its `checkQuota` parameter from `MailItem.isQuotaCheckRequired()`:

- **`MailItem` (base class)** — returns `true` (quota always checked)
- **`Message` override** — returns `!account.isMailAllowReceiveButNotSendWhenOverQuota()`
- **`Conversation` override** — same as `Message`

This means when `zimbraMailAllowReceiveButNotSendWhenOverQuota = true`, quota checks via
`updateSize()` are **skipped** for `Message` and `Conversation` items.

## The `zimbraMailAllowReceiveButNotSendWhenOverQuota` Flag

This flag controls two distinct quota enforcement strategies:

### When `true`: Allow receive, block send

- **Receive path** (`addMessageInternal`):
  - Preemptive `checkSizeChange()` at line 6995 is **skipped** (guarded by `if (!getAccount().isMailAllowReceiveButNotSendWhenOverQuota())`)
  - `Message.isQuotaCheckRequired()` returns `false` — `updateSize()` also skips the check
  - Result: **Receiving is allowed** even when over quota

- **Send path** (`SendMsg` / `SaveDraft`):
  - `AccountUtil.checkQuotaWhenSendMail()` is called and **actively blocks** sending
  - Result: **Sending is blocked** when over quota

### When `false`: Block both send and receive

- **Receive path**:
  - Preemptive `checkSizeChange()` at line 6995 **fires**
  - `Message.isQuotaCheckRequired()` returns `true` — `updateSize()` also checks
  - Result: **Receiving is blocked** when over quota

- **Send path**:
  - `AccountUtil.checkQuotaWhenSendMail()` short-circuits (the `if` condition fails)
  - But the save-to-sent `addMessage()` triggers `updateSize() → checkSizeChange()` because `isQuotaCheckRequired()` returns `true`
  - Result: **Sending is blocked indirectly** (at save-to-sent time, not upfront)

### Summary table

| Flag value | Receive | Send blocked by | Other ops (copy, contacts, etc.) |
|---|---|---|---|
| `true` | Allowed | `checkQuotaWhenSendMail()` in `SendMsg:110` / `SaveDraft:130` (upfront) | Depends on item type (`isQuotaCheckRequired()`) |
| `false` | Blocked | `checkSizeChange()` via `updateSize()` when saving to Sent folder (indirect) | Blocked via `updateSize()` |

## `checkQuotaWhenSendMail()` — The Send-Specific Check

Located in `AccountUtil.java:131`:

```java
public static void checkQuotaWhenSendMail(Mailbox mbox) throws ServiceException {
    Account account = mbox.getAccount();
    long acctQuota = AccountUtil.getEffectiveQuota(account);
    if (account.isMailAllowReceiveButNotSendWhenOverQuota()
        && acctQuota != 0
        && mbox.getSize() > acctQuota) {
        throw MailServiceException.QUOTA_EXCEEDED(acctQuota);
    }
    Domain domain = Provisioning.getInstance().getDomain(account);
    if (domain != null
        && AccountUtil.isOverAggregateQuota(domain)
        && !AccountUtil.isSendAllowedOverAggregateQuota(domain)) {
        throw MailServiceException.DOMAIN_QUOTA_EXCEEDED(domain.getDomainAggregateQuota());
    }
}
```

Called from:
- `SendMsg.handle()` at line 110 — first thing in the SOAP handler
- `SaveDraft.handle()` at line 130 — only when `autoSendTime != 0` (scheduled send)

Note: The account-level check only fires when `isMailAllowReceiveButNotSendWhenOverQuota() == true`.
The domain aggregate check always applies.

## Send Message Flow — Detailed

The send flow in `MailSender.sendMimeMessage()` (`MailSender.java:588`) executes in this order:

1. **`SendMsg.handle()`** — `AccountUtil.checkQuotaWhenSendMail(mbox)` (line 110)
2. **`MailSender.sendMimeMessage()`** — Compose MIME, resolve recipients, run mutators
3. **Save to Sent folder** (lines 699-761) — `mbox.addMessage()` → `addMessageInternal()` → `updateSize()` → `checkSizeChange()`
4. **Send via SMTP** (line 830) — `sendMessage(mbox, mm, rollbacks)`

The save-to-sent happens **before** the actual SMTP send. This means:
- If save-to-sent fails with `QUOTA_EXCEEDED`, the message is **never sent via SMTP**
- There is no risk of "sent but not saved to Sent folder" due to quota

## Race Condition: Incoming Message During Send

**Scenario**: User is not over quota, initiates send. Between step 1 (quota check) and
step 3 (save to Sent), an incoming message pushes the mailbox over quota.

### With `zimbraMailAllowReceiveButNotSendWhenOverQuota = true`:
- Step 1 passes (not over quota yet)
- Step 3: `Message.isQuotaCheckRequired()` returns `false` — save-to-sent **succeeds**
- Step 4: SMTP send **succeeds**
- **No problem** — the flag disables quota checks for Message items

### With `zimbraMailAllowReceiveButNotSendWhenOverQuota = false`:
- Step 1: `checkQuotaWhenSendMail()` short-circuits (flag is false) — passes
- Step 3: `Message.isQuotaCheckRequired()` returns `true` — `checkSizeChange()` fires — **QUOTA_EXCEEDED**
- Step 4: Never reached
- **The send fails unexpectedly** even though the user was under quota when they initiated it

## Draft-Then-Send Scenario

1. User creates a draft while under quota — succeeds (no send quota check for drafts)
2. Time passes, user goes over quota
3. User hits "Send":
   - With flag `true`: `checkQuotaWhenSendMail()` fires at `SendMsg:110` — **send blocked**
   - With flag `false`: `checkQuotaWhenSendMail()` skips, but save-to-sent `checkSizeChange()` fires — **send blocked**

In both cases, the deferred send is blocked. The check happens at send time, not at draft creation time.

## Planned Changes: Storages-Backed Quota Enforcement

The goal is to delegate quota tracking to an external **Storages** service, which becomes
the source of truth for quota usage. The changes prepare the codebase for this integration.

### 1. Rename `checkSizeChange()` to `checkSizeChangeAddOperation()`

**File:** `Mailbox.java`

The method has been renamed to `checkSizeChangeAddOperation()` to make it explicit that
this check only applies to **add operations** (size increases). This clarifies intent: the
method is never called on deletions (guarded by `delta > 0` in `updateSize()`), and the
new name reflects that.

All call sites updated:
- `updateSize()` — `Mailbox.java:1323`
- `addMessageInternal()` — `Mailbox.java:7004`
- `copyInternal()` — `Mailbox.java:7886, 7887`
- `imapCopy()` — `Mailbox.java:8007`

### 2. Storages integration points in `checkSizeChangeAddOperation()`

**File:** `Mailbox.java:1334`

The plan is to replace the local quota calculation with a call to Storages:

```java
// Current (local check):
if (acctQuota != 0 && newSize > acctQuota) {
    throw MailServiceException.QUOTA_EXCEEDED(acctQuota);
}

// Planned (Storages-backed):
// storagesClient.updateUsage({usage: newSize, op: "add"}) -> storages checks if already over quota.
// if overquota === true -> throw exception
```

The Storages service will:
- Receive the new usage value and the operation type (`"add"`)
- Determine if the account is over quota
- Return an over-quota signal, which the mailbox will use to throw `QUOTA_EXCEEDED`

### 3. Storages integration for delete operations

**File:** `Mailbox.java` — `updateSize()`

For delete operations (`delta < 0`), the plan is to notify Storages of the reduced usage
**without** performing a quota check (deletions are always allowed):

```java
// Planned:
// if (!addingMessage)
//   storagesClient.updateUsage({usage: newSize, op: "delete"}) -> always allow
```

This keeps Storages in sync with actual mailbox size even on deletions.

### 4. Storages integration in `checkQuotaWhenSendMail()`

**File:** `AccountUtil.java:131`

The send-specific quota check will also be backed by Storages:

```java
// Planned (replaces local size comparison):
// storagesClient.getAccountQuota(account.getId()) -> isOverQuota
// No need to increase usage on storages (sending doesn't add to mailbox size at this point)
```

Unlike the add operation path, the send check only needs to **query** the quota status
from Storages, not update usage — the actual size increase happens later when saving to
the Sent folder.

### 5. New helper methods in `AccountUtil`

**File:** `AccountUtil.java`

Two new methods added for querying quota policies at the account level (resolving the
domain internally):

- **`isSendAllowedOverQuota(Account)`** — returns `true` if the domain aggregate quota
  policy is `ALLOWSENDRECEIVE`
- **`isReceiveAllowedOverQuota(Account)`** — returns `true` if the domain aggregate quota
  policy is not `BLOCKSENDRECEIVE`

These complement the existing domain-level methods (`isSendAllowedOverAggregateQuota`,
`isReceiveAllowedOverAggregateQuota`) and provide a convenient account-level API for
future use in the Storages integration.

### 6. Clarifying comments in `Conversation.isQuotaCheckRequired()`

**File:** `Conversation.java:857`

Added comments documenting the semantics of `isMailAllowReceiveButNotSendWhenOverQuota()`:

```java
// TRUE: allow receiving mails but not sending
// FALSE: do NOT allow receiving mails when overquota
return !account.isMailAllowReceiveButNotSendWhenOverQuota();
```

### Summary of planned Storages integration

| Operation | Current behavior | Planned behavior |
|---|---|---|
| **Add** (receive, copy, etc.) | Local check: `newSize > acctQuota` | `storagesClient.updateUsage({usage, op: "add"})` — Storages checks and rejects if over quota |
| **Delete** | No quota check (delta < 0) | `storagesClient.updateUsage({usage, op: "delete"})` — always allowed, keeps Storages in sync |
| **Send** (upfront check) | Local check: `mbox.getSize() > acctQuota` | `storagesClient.getAccountQuota(id)` — query-only, no usage update |