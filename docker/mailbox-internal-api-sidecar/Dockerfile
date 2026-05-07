FROM registry.dev.zextras.com/dev/carbonio-sidecar:devel

# Service registration
COPY packages/appserver-service/carbonio-mailbox-internal-api.hcl /consul/config/

# Setup files
COPY packages/appserver-service/carbonio-mailbox-internal-api-service-protocol.json /etc/carbonio/mailbox-internal-api/service-discover/service-protocol.json
COPY packages/appserver-service/carbonio-mailbox-internal-api-intentions.json       /etc/carbonio/mailbox-internal-api/service-discover/intentions.json
COPY packages/appserver-service/carbonio-mailbox-internal-api-policies.json         /etc/carbonio/mailbox-internal-api/service-discover/policies.json

# Setup script
COPY packages/appserver-service/carbonio-mailbox-internal-api /usr/local/bin/carbonio-mailbox-internal-api

ENV SERVICE_NAME=carbonio-mailbox-internal-api
ENV SETUP_SCRIPT="carbonio-mailbox-internal-api setup"
ENV TOKEN_FILE=/etc/carbonio/mailbox-internal-api/service-discover/token
