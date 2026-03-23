FROM docker.io/envoyproxy/envoy:v1.27-latest
COPY --from=docker.io/hashicorp/consul:1.17 /bin/consul /usr/local/bin/consul
RUN apt-get update && apt-get install -y jq && rm -rf /var/lib/apt/lists/*

# Fake service-discover binary — returns a predefined bootstrap token
RUN printf '#!/bin/sh\necho "00000000-0000-0000-0000-000000000000"\n' > /usr/local/bin/service-discover \
    && chmod +x /usr/local/bin/service-discover

# Service registration (registered via consul services register)
COPY packages/appserver-service/carbonio-mailbox-admin.hcl /consul/config/

# Setup files at the path the setup script expects
COPY packages/appserver-service/carbonio-mailbox-admin-service-protocol.json /etc/carbonio/mailbox-admin/service-discover/
COPY packages/appserver-service/carbonio-mailbox-admin-intentions.json       /etc/carbonio/mailbox-admin/service-discover/
COPY packages/appserver-service/carbonio-mailbox-admin-policies.json         /etc/carbonio/mailbox-admin/service-discover/

# Setup script (runs as-is from the package)
COPY packages/appserver-service/carbonio-mailbox-admin /usr/local/bin/carbonio-mailbox-admin
RUN chmod +x /usr/local/bin/carbonio-mailbox-admin

ENV SERVICE_NAME=carbonio-mailbox-admin

ENTRYPOINT ["/bin/sh", "-c", "\
  echo '[sidecar] Waiting for consul agent...' && \
  until consul members >/dev/null 2>&1; do sleep 1; done && \
  echo '[sidecar] Consul agent ready.' && \
  consul services register /consul/config/*.hcl && \
  (carbonio-mailbox-admin setup || true) && \
  echo '[sidecar] Starting envoy for $SERVICE_NAME' && \
  exec consul connect envoy \
    -sidecar-for=$SERVICE_NAME \
    -admin-bind=localhost:0 \
"]
