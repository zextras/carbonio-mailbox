# pre_flight

This directory should contain scripts that are supposed to be executed before a new LDAP schema is installed and loaded by slapd (i.e., before a process restart).
The scripts are executed by LdapPreFlight.pl in the order defined inside it.

## Use Cases

- Modify LDAP configuration (cn=config) that needs to be applied before slapd starts.
- Apply updates to LDAP modules (cn=module{0},cn=config).