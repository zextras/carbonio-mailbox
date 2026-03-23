FROM docker.io/envoyproxy/envoy:v1.27-latest
COPY --from=docker.io/hashicorp/consul:1.17 /bin/consul /usr/local/bin/consul
RUN apt-get update && apt-get install -y jq && rm -rf /var/lib/apt/lists/*

# Fake service-discover binary — returns a predefined bootstrap token
RUN printf '#!/bin/sh\necho "00000000-0000-0000-0000-000000000000"\n' > /usr/local/bin/service-discover \
    && chmod +x /usr/local/bin/service-discover

# Service registration (registered via consul services register)
COPY packages/appserver-service/carbonio-mailbox.hcl /consul/config/

# Setup files at the path the setup script expects
COPY packages/appserver-service/carbonio-mailbox-service-protocol.json /etc/carbonio/mailbox/service-discover/
COPY packages/appserver-service/carbonio-mailbox-intentions.json       /etc/carbonio/mailbox/service-discover/
COPY packages/appserver-service/carbonio-mailbox-policies.json         /etc/carbonio/mailbox/service-discover/

# Setup script (runs as-is from the package)
COPY packages/appserver-service/carbonio-mailbox /usr/local/bin/carbonio-mailbox
RUN chmod +x /usr/local/bin/carbonio-mailbox

ENV SERVICE_NAME=carbonio-mailbox

ENTRYPOINT ["/bin/sh", "-c", "\
  echo '[sidecar] Waiting for consul agent...' && \
  until consul members >/dev/null 2>&1; do sleep 1; done && \
  echo '[sidecar] Consul agent ready.' && \
  consul services register /consul/config/*.hcl && \
  (carbonio-mailbox setup || true) && \
  echo '[sidecar] Starting envoy for $SERVICE_NAME' && \
  exec consul connect envoy \
    -sidecar-for=$SERVICE_NAME \
    -admin-bind=localhost:0 \
"]
