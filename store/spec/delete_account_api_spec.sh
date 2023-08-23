#!/bin/bash

Describe 'delete account SOAP API'
  Include './spec/helper/admin_soap.sh'
  Include './spec/helper/container.sh'

  RANDOM_PORT_ADMIN_SOAP=""
  CONTAINER_ID=""

  setup() {
    RANDOM_PORT_ADMIN_SOAP="$(((RANDOM + 1024) % 65000))"
    CONTAINER_ID="$(launch_container "${RANDOM_PORT_ADMIN_SOAP}")"

     wait_for_container_endpoint_to_be_ready "${CONTAINER_ID}" "localhost:6071"

     set_admin_port "${RANDOM_PORT_ADMIN_SOAP}"
  }

  cleanup() {
    podman kill "${CONTAINER_ID}" > /dev/null
    podman rm -v "${CONTAINER_ID}" > /dev/null
  }

  BeforeEach 'setup'
  AfterEach 'cleanup'


  It 'should delete an existing account'
    delete_existing_account() {
      local auth_token
      auth_token="$(auth_request "zextras" "password")"
      local account_to_delete_id=
      account_to_delete_id"$(podman exec -u0 "${CONTAINER_ID}" /opt/zextras/bin/zmprov ca test@mail.local test123)"
      delete_account_request "${auth_token}" "${account_to_delete_id}"
    }

    When call delete_existing_account
    The status should be success
    The output should equal '<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Header><context xmlns="urn:zimbra"/></soap:Header><soap:Body><DeleteAccountResponse xmlns="urn:zimbraAdmin"/></soap:Body></soap:Envelope>'
  End
End
