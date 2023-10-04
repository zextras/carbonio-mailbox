#!/bin/bash

wait_for_container_endpoint_to_be_ready() {
  local container_id="${1}"
  local endpoint_url="${2}"
  podman exec -u0 "${container_id}" /usr/bin/wait-for-it "${endpoint_url}" -t0
}

launch_container() {
  local admin_soap_port="${1}"

  podman run \
        -d \
        -p "${admin_soap_port}":6071 \
        -u root \
        --entrypoint=/sbin/init \
        --hostname=carbonio.mail.local \
        --add-host mail.local:127.0.0.1 \
        carbonio/ce-single-u20
}
