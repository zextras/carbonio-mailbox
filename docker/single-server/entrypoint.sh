#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

# Start services
source /opt/zextras/.bashrc

ldap start

zmprov -l ms $(zmhostname) -zimbraServiceEnabled antivirus -zimbraServiceEnabled amavis -zimbraServiceEnabled stats
zmprov -l mcf zimbraSmtpPort 25
zmprov -l mcf zimbraSmtpHostname 127.0.0.1

zmcontrol start

# Wait until mailbox up
echo mailboxIsHealthy
HEALTHCHECK="false"

until [ $HEALTHCHECK == "true" ] || command; do
    echo "Checking mailbox health..."
    HEALTHCHECK=mailboxIsHealthy
done

echo "Mailbox healthy"

function mailboxIsHealthy() {
    return $(curl -X POST -k https://localhost:7071/service/admin/soap -d '
    {
        "Body": {
            "CheckHealthRequest": {
                "_jsns": "urn:zimbraAdmin",
            }
        }
    }' | jq ".Body.CheckHealthResponse.healthy")
}

curl -s -X POST -k https://localhost:7071/service/admin/soap -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:urn="urn:zimbra" xmlns:urn1="urn:zimbraAdmin">
   <soapenv:Body>
      <urn1:CheckHealthRequest/>
   </soapenv:Body>
</soapenv:Envelope>' | grep -i healthy=\"0\"

# Populate with some data
/opt/zextras/prepare_data.sh

echo "Keeping the container up..."

tail -f /dev/null
