FROM registry.dev.zextras.com/dev/carbonio-sidecar:latest

# Service registration
COPY packages/appserver-service/carbonio-mailbox.hcl /consul/config/

# Setup files
COPY packages/appserver-service/carbonio-mailbox-service-protocol.json /etc/carbonio/mailbox/service-discover/
COPY packages/appserver-service/carbonio-mailbox-intentions.json       /etc/carbonio/mailbox/service-discover/
COPY packages/appserver-service/carbonio-mailbox-policies.json         /etc/carbonio/mailbox/service-discover/

# Setup script
COPY packages/appserver-service/carbonio-mailbox /usr/local/bin/carbonio-mailbox
RUN chmod +x /usr/local/bin/carbonio-mailbox

ENV SERVICE_NAME=carbonio-mailbox
ENV SETUP_SCRIPT="carbonio-mailbox setup"