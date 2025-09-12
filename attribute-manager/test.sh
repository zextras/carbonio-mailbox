#!/bin/bash

mvn exec:java -Dexec.mainClass="com.zimbra.cs.account.AttributeManagerUtil" -Dexec.args="-a generateGetters -c server -r ./a/ZAttrServer.java"