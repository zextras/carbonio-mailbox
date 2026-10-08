FROM registry.dev.zextras.com/dev/carbonio-sidecar:devel

# Service registration
COPY packages/appserver-service/carbonio-authz.hcl /consul/config/

# Setup files
COPY packages/appserver-service/carbonio-authz-service-protocol.json /etc/carbonio/authz/service-discover/service-protocol.json
COPY packages/appserver-service/carbonio-authz-intentions.json       /etc/carbonio/authz/service-discover/intentions.json
COPY packages/appserver-service/carbonio-authz-policies.json         /etc/carbonio/authz/service-discover/policies.json

# Setup script
COPY packages/appserver-service/carbonio-authz /usr/local/bin/carbonio-authz

ENV SERVICE_NAME=carbonio-authz
ENV SETUP_SCRIPT="carbonio-authz setup"
ENV TOKEN_FILE=/etc/carbonio/authz/service-discover/token
