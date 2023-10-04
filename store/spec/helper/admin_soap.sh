#!/bin/bash

DEFAULT_ADMIN_ADDRESS="localhost"
DEFAULT_ADMIN_PORT="6071"
API_ENDPOINT="service/admin/soap/"

set_admin_address () {
  DEFAULT_ADMIN_ADDRESS="${1}"
}

set_admin_port() {
  DEFAULT_ADMIN_PORT="${1}"
}

auth_request() {
  local account="${1}"
  local password="${2}"

  curl \
    -sSLk \
    -XPOST \
    -H "Content-Type: text/xml" \
    --data "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:zimbra\" xmlns:urn1=\"urn:zimbraAdmin\"><soapenv:Body><urn1:AuthRequest><urn1:account>${account}</urn1:account><urn1:password>${password}</urn1:password></urn1:AuthRequest></soapenv:Body></soapenv:Envelope>" \
    "https://${DEFAULT_ADMIN_ADDRESS}:${DEFAULT_ADMIN_PORT}/${API_ENDPOINT}" | xq -x '//authToken[1]'
}

delete_account_request() {
  local auth_token="${1}"
  local account_id="${2}"

  curl \
    -sSLk \
    -XPOST \
    -H "Content-Type: text/xml" \
    --data "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:zimbra\" xmlns:urn1=\"urn:zimbraAdmin\"><soapenv:Header><urn:context><urn:authToken>${auth_token}</urn:authToken></urn:context></soapenv:Header><soapenv:Body><urn1:DeleteAccountRequest id=\"${account_id}\"/></soapenv:Body></soapenv:Envelope>" \
    "https://${DEFAULT_ADMIN_ADDRESS}:${DEFAULT_ADMIN_PORT}/${API_ENDPOINT}"
}
