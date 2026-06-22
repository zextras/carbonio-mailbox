# Ephemeral Store

The ephemeral store is the subsystem used to read/write short-lived and dynamic attributes outside the regular LDAP attribute model. It provides a common API (`EphemeralStore`) and multiple backends (LDAP, Redis, in-memory), plus migration support between backends.

## What it is used for

At runtime, account/domain entry code calls `EphemeralStore` through `Entry` methods such as:

- `getEphemeralAttr(...)`
- `modifyEphemeralAttr(...)`
- `deleteEphemeralAttr(...)`
- `purgeEphemeralAttr(...)`
- `hasEphemeralAttr(...)`

These methods build an `EphemeralLocation` (`LdapEntryLocation` for LDAP entries), create an `EphemeralKey`, and delegate to the active backend store instance.

## Core model

- `EphemeralKey`: logical key + optional dynamic component.
- `EphemeralInput`: key + value + optional expiration.
- `EphemeralLocation`: hierarchy identifying where data belongs.
- `EphemeralResult`: typed accessors (`getValue`, `getLongValue`, `getBoolValue`, etc.) over returned value(s).

Expiration can be absolute or relative (`EphemeralInput.AbsoluteExpiration`, `RelativeExpiration`).

## Store interface

`EphemeralStore` defines:

- `get`
- `set`
- `update`
- `delete`
- `has`
- `purgeExpired`
- `deleteData`

Backends implement the same contract with different storage/encoding behavior.

## Backend selection and lifecycle

Backend resolution is driven by URL prefix in `zimbraEphemeralBackendURL`:

- `ldap:...` -> `LdapEphemeralStore.Factory`
- `redis:...` -> `RedisEphemeralStoreFactory`

Factories are registered in `EphemeralStore.factories`. `EphemeralStore.getFactory()` creates and caches a global factory for the process, calls `startup()`, and returns backend stores via `factory.getStore()`.

`EphemeralBackendCheck` validates backend URL changes, ensures the backend can be initialized (`factory.test(url)`), manages migration constraints, and updates `zimbraPreviousEphemeralBackendURL`.

## Backend implementations

### 1) LDAP backend (`LdapEphemeralStore`)

Used for legacy/default behavior and compatible with LDAP-provisioned entries.

- Uses `LdapAttributeEncoder`.
- Stores encoded values inside LDAP multivalued attributes.
- Uses `DynamicResultsHelper` to:
  - filter dynamic key matches
  - decode values
  - purge expired values
  - clean unparseable values
- `deleteData(...)` is intentionally a no-op; LDAP-side cleanup is handled by provisioning flows.

### 2) Redis backend (`RedisEphemeralStore`)

Used for external ephemeral storage with native key expiration.

- Uses `JedisPool` (shared, thread-safe pool).
- Each operation borrows its own `Jedis` in try-with-resources.
- Key format: `locationPart|ephemeralKey[|dynamicComponent]`
- `set(...)`:
  - no expiration -> `SET`
  - expiration -> `PSETEX` using relative TTL
  - non-positive TTL is rejected and logged
- `purgeExpired(...)` is a no-op (Redis TTL handles expiration)
- `deleteData(...)` scans keys by location pattern and deletes in batches via `UNLINK` (non-blocking delete)
- Factory is singleton-per-process

Pool tuning values come from config:

- `getSSDBResourcePoolSize()`
- `getSSDBResourcePoolTimeout()`

### 3) In-memory backend (`InMemoryEphemeralStore`)

Mainly for tests/local scenarios.

- Uses a `Map<String, Multimap<String, String>>` grouped by location
- Uses `DynamicExpirationEncoder`
- Supports dynamic/expiration semantics by encoding data into values

## Encoding and dynamic keys

Some backends (LDAP/in-memory) cannot model dynamic keys and TTL natively. They rely on encoder/decoder logic:

- `AttributeEncoder` abstraction
- `DynamicExpirationEncoder` and `DynamicExpirationValueEncoder`
- `DynamicResultsHelper` to interpret and filter encoded values

For LDAP-specific attributes, `LdapAttributeEncoder` has custom decode rules for:

- `zimbraAuthTokens`
- `zimbraCsrfTokenData`

## Migration and forwarding mode

When migration metadata indicates a migration URL/status (`IN_PROGRESS` or `COMPLETED`), `EphemeralStore.setFactory(...)` can wrap the active backend in `ForwardingEphemeralStore`.

Forwarding behavior:

- Reads (`get`, `has`) from current store only.
- Writes/deletes/purge/deleteData go to current store first, then are attempted on future store.
- Failures on future store are logged at debug level and do not fail the primary operation.

Migration logic in `AttributeMigration` uses `ZimbraMigrationCallback` to:

1. validate destination backend
2. set migration backend type
3. write converted `EphemeralInput` values into destination store

## Usages
### Recommended usage flow

1. (Optional) Migrate existing ephemeral attributes from LDAP to Redis before switching backend:

```bash
zmmigrateattrs redis://<redis_host_or_ip>:<redis_port>
```

For targeted migration:

```bash
zmmigrateattrs -a <account@example.com> redis://<redis_host_or_ip>:<redis_port>
```

2. Switch primary ephemeral backend to Redis:

```bash
carbonio prov mcf zimbraEphemeralBackendUrl redis://<redis_host_or_ip>:<redis_port>
```

3. If needed, revert backend to LDAP:

```bash
carbonio prov mcf zimbraEphemeralBackendUrl ldap://default
```

## Operational notes

- Use `EphemeralStore.canConnectToURL(...)` / `factory.test(url)` for backend URL validation.
- `EphemeralStore.clearFactory()` shuts down and resets the global factory.
- `Zimbra` shutdown path calls ephemeral factory shutdown.

## Relevant classes

- `com.zimbra.cs.ephemeral.EphemeralStore`
- `com.zimbra.cs.ephemeral.LdapEphemeralStore`
- `com.zextras.mailbox.store.ephemeral.RedisEphemeralStore`
- `com.zimbra.cs.ephemeral.InMemoryEphemeralStore`
- `com.zimbra.cs.ephemeral.ForwardingEphemeralStore`
- `com.zimbra.cs.account.callback.EphemeralBackendCheck`
- `com.zimbra.cs.ephemeral.migrate.AttributeMigration`
