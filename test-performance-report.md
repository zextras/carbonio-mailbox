# Test Performance Report

**Source:** CI log from UT/IT phase (`-DexcludedGroups=api,flaky,e2e`)
**Total CI step time:** 5m 12s
**Total test execution time:** ~223s (~3m 43s)
**Maven overhead (reactor, JVM forks, jacoco, failsafe):** ~89s (~1m 29s)
**Test classes:** 365
**Tests run:** 1912 (1 failure, 1 error, 41 skipped)

## Duration Distribution by Module

| Range    | store | common | soap | client | **Total** | **Time (s)** |
|----------|-------|--------|------|--------|-----------|--------------|
| < 1s     |   247 |     26 |    6 |      2 |   **281** |     **~39s** |
| 1s – 2s  |    23 |      7 |    3 |      0 |    **33** |     **~46s** |
| 2s – 3s  |    12 |      7 |    0 |      0 |    **19** |     **~47s** |
| 3s – 4s  |     6 |      2 |    3 |      0 |    **11** |     **~38s** |
| 4s – 5s  |     4 |      0 |    0 |      0 |     **4** |     **~17s** |
| 5s – 6s  |     6 |      2 |    1 |      0 |     **9** |     **~49s** |
| 6s – 7s  |     3 |      0 |    0 |      0 |     **3** |     **~19s** |
| 7s – 8s  |     2 |      0 |    0 |      0 |     **2** |     **~15s** |
| 8s – 9s  |     2 |      0 |    0 |      0 |     **2** |     **~18s** |
| >= 9s    |     1 |      0 |    0 |      0 |     **1** |     **~10s** |
| **Total**| **306** | **44** | **13** | **2** | **365** | **~223s** |

## Module Summary

| Module   | Classes | Time   | Slow (>=4s) | % of total time |
|----------|---------|--------|-------------|-----------------|
| store    |     306 |  169s  |          18 |             76% |
| common   |      44 |   37s  |           2 |             17% |
| soap     |      13 |   17s  |           1 |              8% |
| client   |       2 |   ~0s  |           0 |              0% |
| **TOTAL**|   **365**| **223s** |      **21** |        **100%** |

## Slow Test Classes (>= 4s) — Tagged @Tag("slow")

| Time   | Module | Class                                                        |
|--------|--------|--------------------------------------------------------------|
|  8.8s  | store  | com.zimbra.cs.filter.NotifyMailtoTest                        |
|  8.7s  | store  | com.zimbra.cs.mailbox.MailboxLockTest                        |
|  7.5s  | store  | com.zimbra.cs.filter.ReplaceHeaderTest                       |
|  7.3s  | store  | com.zextras.mailbox.store.ephemeral.RedisEphemeralStoreTest  |
|  6.4s  | store  | com.zimbra.cs.mailbox.PurgeTest                              |
|  6.3s  | store  | com.zimbra.cs.service.admin.DeleteAccountTest                |
|  6.1s  | store  | com.zimbra.cs.util.calltohome.CallToHomeRunnerTest           |
|  6.0s  | store  | com.zimbra.cs.filter.DeleteHeaderTest                        |
|  6.0s  | store  | com.zimbra.cs.db.DbVolumeBlobsTest                           |
|  5.9s  | soap   | com.zimbra.soap.account.GetInfoResponseTest                  |
|  5.7s  | store  | com.zimbra.cs.filter.SetVariableTest                         |
|  5.5s  | common | com.zimbra.common.util.HttpConnectionManagerMetricsExportTest|
|  5.4s  | common | com.zimbra.common.util.HttpUtilTest                          |
|  5.2s  | store  | com.zimbra.cs.account.ldap.DomainMaxAccountsValidatorTest    |
|  5.2s  | store  | com.zimbra.cs.mailbox.FolderTest                             |
|  5.0s  | store  | com.zimbra.cs.filter.RelationalExtensionTest                 |
|  4.5s  | store  | com.zimbra.cs.mailbox.MailboxTest                            |
|  4.3s  | store  | com.zimbra.cs.filter.FileIntoCopyTest                        |
|  4.3s  | store  | com.zimbra.cs.filter.EnvelopeTest                            |
|  4.2s  | store  | com.zimbra.cs.service.mail.SyncTest                          |
|  3.7s  | soap   | com.zimbra.soap.mail.SendMsgRequestTest                      |

## Key Observations

- **21 slow classes (6%) account for ~102s (46%) of all test time**
- **store module** dominates: 306 classes, 169s, 18 of 21 slow tests
- **7 filter tests** (NotifyMailto, ReplaceHeader, DeleteHeader, SetVariable, RelationalExtension, FileIntoCopy, Envelope) take ~46s combined — likely share an expensive setup pattern
- **281 classes (77%) finish in < 1s** — the fast majority
- Maven overhead (~89s) is significant — JVM fork startup across 8 modules contributes
