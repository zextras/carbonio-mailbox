# About this Directory

This directory hold the artifacts required to update the existing LDAP install.

## WARNINGS

1. Make sure they are valid JSON files and follows the schema provided in `example.template` file. The upgrade script
   will simply ignore the file otherwise and will leads to a system that do not have new updates installed.
2. Make sure update files are named properly based on the instructions provided below.
3. Any file other than JSON extension is ignored by the upgrade script and make no sense to be part of this directory (
   the exceptions are provided below).
4. JSON file creation is not needed while creating new attributes without default value.

## Content that is supposed to reside to this directory

1. example.template (The template developer have to use to create <update_timestamp>.json files.)
2. Readme (this file)
3. and the update files.

## What are update files?

Update files are the JSON files created following the pattern found in `example.template` that contains data that our
update script (`/opt/zextras/libexec/ldapattributeupdate`) consumes.

The aim is to have newly added entries/attributes to these JSON file in which will be installed
in `/opt/zextras/common/etc/openldap/zimbra/updates/attrs/`

### Naming of update files

Update files are named as the value of last timestamp of last commit on `attrs.xml`(this can be retrieved
using `git log -1 --pretty=format:%at carbonio-mailbox/store/conf/attrs/attrs.xml`).

## How update files are consumed by ldapattributeupdate script?

The `ldapattributeupdate` script runs in postinstall part of the installation procedure in upgrade hook.
This script runs after few other scripts that populate the LDIF files and install our schema in place where LDAP looks
for them. The aim of these update file is to provide information to our update script about: which entry got new
attribute and when.
