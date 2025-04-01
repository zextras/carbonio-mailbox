#!/usr/bin/env bash

# JARS
cp ./store/target/zm-store.jar ./pre-package/jars/
cp ./store/target/dependencies/* ./pre-package/jars/

cp ./native/target/zm-native*.jar ./pre-package/jars/

cp ./soap/target/zm-soap*.jar ./pre-package/jars/

cp ./common/target/zm-common*.jar ./pre-package/jars/


# LDAP
mkdir -p ./pre-package/ldap/ldifs/
mkdir -p ./pre-package/ldap/schemas/

cp ./store/ldap/generated/schema/* ./pre-package/ldap/schemas/

cp ./store/ldap/generated/carbonio.ldif ./pre-package/ldap/ldifs/
cp ./store/ldap/generated/mimehandlers.ldif ./pre-package/ldap/ldifs/
cp ./store/ldap/generated/zimbra_defaultcos.ldif ./pre-package/ldap/ldifs/
cp ./store/ldap/generated/zimbra_defaultexternalcos.ldif ./pre-package/ldap/ldifs/
cp ./store/ldap/generated/zimbra_globalconfig.ldif ./pre-package/ldap/ldifs/
cp ./docker/standalone/initial_data.ldif ./pre-package/ldap/ldifs/

