#!/usr/bin/env bash

cp ./store/target/zm-store.jar ./pre-package/jars/
cp ./store/target/dependencies/* ./pre-package/jars/

cp ./native/target/zm-native*.jar ./pre-package/jars/

cp ./soap/target/zm-soap*.jar ./pre-package/jars/

cp ./common/target/zm-common*.jar ./pre-package/jars/
