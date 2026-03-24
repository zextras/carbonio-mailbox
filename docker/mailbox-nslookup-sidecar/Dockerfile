FROM registry.dev.zextras.com/dev/carbonio-sidecar:latest

# Service registration
COPY packages/appserver-service/carbonio-mailbox-nslookup.hcl /consul/config/

# Setup files
COPY packages/appserver-service/carbonio-mailbox-nslookup-service-protocol.json /etc/carbonio/mailbox-nslookup/service-discover/service-protocol.json
COPY packages/appserver-service/carbonio-mailbox-nslookup-intentions.json       /etc/carbonio/mailbox-nslookup/service-discover/intentions.json
COPY packages/appserver-service/carbonio-mailbox-nslookup-policies.json         /etc/carbonio/mailbox-nslookup/service-discover/policies.json

# Setup script
COPY packages/appserver-service/carbonio-mailbox-nslookup /usr/local/bin/carbonio-mailbox-nslookup
RUN chmod +x /usr/local/bin/carbonio-mailbox-nslookup

ENV SERVICE_NAME=carbonio-mailbox-nslookup
ENV SETUP_SCRIPT="carbonio-mailbox-nslookup setup"
ENV TOKEN_FILE=/etc/carbonio/mailbox-nslookup/service-discover/token