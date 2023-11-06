#!/bin/bash

rm -rf schemas
mkdir schemas
cp ../soap/target/classes/com/zimbra/soap/ZimbraService.wsdl schemas/ZimbraService.wsdl
cp ../soap/target/classes/com/zimbra/soap/zimbraAccount.xsd schemas/zimbraAccount.xsd
cp ../soap/target/classes/com/zimbra/soap/zimbraAdminExt.xsd schemas/zimbraAdminExt.xsd
cp ../soap/target/classes/com/zimbra/soap/zimbraAdmin.xsd schemas/zimbraAdmin.xsd
cp ../soap/target/classes/com/zimbra/soap/zimbraMail.xsd schemas/zimbraMail.xsd
cp ../soap/target/classes/com/zimbra/soap/zimbra.xsd schemas/zimbra.xsd
