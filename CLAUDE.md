# Carbonio Mailbox

Java/Maven multi-module project — the core mail server of the Carbonio platform (fork of Zimbra).

## Modules

- **store** — main module: mailbox logic, SOAP handlers, REST internal API, LDAP provisioning
- **common** — shared utilities and service exceptions
- **soap** — SOAP protocol and JAXB types
- **client** — SOAP client library
- **right-manager** — ACL / permission framework

## Build & test

See [Makefile](Makefile) for build, test, and deploy commands.

## Internal REST API

Service-to-service API under `store/.../com/zextras/mailbox/api/`. Wired in `InternalApiApplication`, uses JAX-RS (RESTEasy) + vavr `Try` for error handling. Resources live in `api/rest/resource/`, services in `api/rest/service/`.

## Package structure

- `com.zimbra` — legacy code (Zimbra origin). Modify when necessary, don't extend.
- `com.zextras` — new code. All new features and APIs go here.

## Conventions

- [Coding rules](coding_feedback.md)
- [Testing rules](test_feedback.md)
