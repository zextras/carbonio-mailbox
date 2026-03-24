FROM localhost/carbonio-sidecar-base:latest

# Service registration
COPY packages/appserver-service/carbonio-mailbox-internal-api.hcl /consul/config/

# Setup files
COPY packages/appserver-service/carbonio-mailbox-internal-api-service-protocol.json /etc/carbonio/mailbox-internal-api/service-discover/
COPY packages/appserver-service/carbonio-mailbox-internal-api-intentions.json       /etc/carbonio/mailbox-internal-api/service-discover/
COPY packages/appserver-service/carbonio-mailbox-internal-api-policies.json         /etc/carbonio/mailbox-internal-api/service-discover/

# Setup script
COPY packages/appserver-service/carbonio-mailbox-internal-api /usr/local/bin/carbonio-mailbox-internal-api
RUN chmod +x /usr/local/bin/carbonio-mailbox-internal-api

ENV SERVICE_NAME=carbonio-mailbox-internal-api
ENV SETUP_SCRIPT="carbonio-mailbox-internal-api setup"
