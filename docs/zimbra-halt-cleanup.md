# Zimbra.halt cleanup

## Problem

`Zimbra.halt(String, Throwable)` (and the no-arg variant) terminates the JVM
after logging a fatal:

```java
ZimbraLog.system.fatal(message, t);
System.exit(1);   // or Runtime.getRuntime().halt(1)
```

It is used in two distinct patterns:

1. **Genuine fatal errors.** DB commit failures (`Mailbox.endTransaction`,
   line 10684) and OOMs in indexing (`MailboxIndex` lines 532, 610, 1421).
   Killing the JVM is the right call.

2. **Defensive checks in singleton accessors.** E.g. `LdapClient.getInstance()`
   calls halt when the singleton is null. The intent is "if this is null in
   production something is catastrophically wrong" — but in tests it fires
   every time we cleanly shut down the LDAP layer. Background threads
   (GAL sync, indexing, scheduled tasks) outlive the test lifecycle, call
   `getInstance()` after `tearDown` has nulled the singleton, and halt the
   forked JVM. Surefire then times out waiting for "Good Bye" and SIGKILLs.

Pattern (2) is the bug we want to remove.

## Current state (this commit)

`Zimbra.halt` checks a system property and skips `System.exit(1)` when set:

```java
} finally {
  if (!Objects.equals("true", System.getProperty("runningTests"))) {
    System.exit(1);
  }
}
```

`store/pom.xml` sets `-DrunningTests=true` for surefire and failsafe argLines.
In tests, halt logs and returns; the calling thread typically NPEs on the
post-null code path and terminates cleanly. The JVM exits naturally because
no non-daemon user threads remain alive (sync-only — daemon flag was not
changed).

This is **a stopgap**. Production code branches on "are we in tests", which:
- Couples main and test code.
- Is easy to miss at new halt call-sites.
- Leaves the underlying design (halt-as-assertion) in place.

## Plan to remove the stopgap

Goal: drop the `runningTests` guard and the `-DrunningTests=true` argLines.

### Step 1 — Audit halt call-sites

Grep for `Zimbra.halt(` across `main/`. For each, classify:

- **Genuine fatal** — keep halting. Examples: DB commit failure, OOM.
- **Defensive assertion** — convert per Step 2.

### Step 2 — Replace defensive halts with `IllegalStateException`

Pattern, e.g. in `LdapClient.getInstance`:

```java
public static LdapClient getInstance() {
  LdapClient i = instance;
  if (i == null) {
    throw new IllegalStateException("LdapClient not initialized");
  }
  return i;
}
```

Effects:
- Production callers that hit a null singleton see the same fast-fail signal,
  no JVM kill. If a thread can't continue without it, the thread dies, the
  exception is logged, the rest of the server keeps running.
- Test background threads die cleanly on the exception instead of triggering
  shutdown hooks.

For each replacement, run the full store test suite to verify no caller
relied on the JVM-kill behaviour.

### Step 3 — Remove the stopgap

- Delete the `runningTests` check from `Zimbra.halt`.
- Remove `-DrunningTests=true` from both argLines in `store/pom.xml`.

### Step 4 (optional follow-up) — Subsystem lifecycle

If audit reveals widespread halt-as-assertion (it likely does), introduce a
small `Lifecycle` interface with Starting/Running/Stopping/Stopped states.
Background loops check state and exit on Stopping. tearDown order becomes a
non-issue. Bigger change, separate effort.

## Out of scope

- `Mailbox.endTransaction`'s halt on DB commit failure. Genuine fatal — leave
  it.
- The `Zimbra.halt("out of memory", e)` sites in `MailboxIndex`. OOMEs are
  defensibly fatal. Re-evaluate independently if/when we want to do graceful
  per-task recovery; not part of this cleanup.

## Acceptance criteria

- `mvn -pl store -am test -Dtest='ContactAutoCompleteTest#lastNameFirstName'
  -Dmaven.build.cache.enabled=false` passes without `-DrunningTests=true`.
- `grep -r "runningTests" store/src/main common/src/main` returns no
  matches (only test/build configuration may reference it, and that should
  also be removed in Step 3).
- No `Zimbra.halt` frame appears in any thread dump captured during teardown
  of the in-memory test harness.
