## About this Directory
This directory contains migration files definition to execute a cleanup on LDAP.

### WARNINGS:
1. Make sure they are valid JSON files and follows the schema provided in [`example.template`](example.template) file.  The cleanup script will fail otherwise.
2. Make sure cleanup files are named properly based on the instructions provided below.
3. Any file other than JSON extension is ignored by the cleanup script.

### Content that is supposed to reside to this directory:
1. example.template (The template developer have to use to create <cleanup-timestamp>.json files.)
2. Readme (this file)
3. the cleanup files.

### What are cleanup files?
Cleanup files are JSON files containing information on attributes to remove from all entries on LDAP.

The aim is to remove these attributes on LDAP before removing them from schema, else entries with attributes not defined in schema will fail to update.

#### Naming of cleanup files:
Cleanup files are named as the value of last timestamp of  last commit on `attrs.xml`(this can be retrieved using `git log -1 --pretty=format:%at carbonio-mailbox/store/conf/attrs/attrs.xml`).

![](blob:https://zextras.atlassian.net/bb2b58e7-bafd-43cb-a480-f9ba7c9c0e88#media-blob-url=true&id=7c8a9cbd-3aa8-43b5-b59c-9822f497eee0&collection=contentId-2394128424&contextId=2394128424&mimeType=image%2Fpng&name=schema_proto.png&size=46878&height=369&width=544&alt=)

### How cleanup files are consumed by ldapattributecleanup script?
The [`ldapattributecleanup`](../libexec/ldapattributecleanup) script runs in postinstall part of the installation procedure in upgrade hook.
It runs as first step and starts LDAP.
It then checks current LDAP schema version, iterates all .json files in cleanup folder, sorted by timestamp, \
starting from current schema timestamp value.
It can work in two different ways and in this order of execution:
1) removes all attributes from all entries defined in "delete" array, if present, by order. \
Due to this generic approach the cleanup could take a bit if there are many entries in LDAP.
2) executes .ldif files defined in "ldif" array, if present, by order:
this is to add flexibility in cleanup when complex operations are performed.
The .ldif file must be located on the same directory as .json file.

The schema version is updated after each applied .json cleanup.

