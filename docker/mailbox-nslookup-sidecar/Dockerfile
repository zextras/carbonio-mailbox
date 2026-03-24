FROM localhost/carbonio-sidecar-base:latest

# Service registration
COPY packages/appserver-service/carbonio-mailbox-nslookup.hcl /consul/config/

# Setup files
COPY packages/appserver-service/carbonio-mailbox-nslookup-service-protocol.json /etc/carbonio/mailbox-nslookup/service-discover/
COPY packages/appserver-service/carbonio-mailbox-nslookup-intentions.json       /etc/carbonio/mailbox-nslookup/service-discover/
COPY packages/appserver-service/carbonio-mailbox-nslookup-policies.json         /etc/carbonio/mailbox-nslookup/service-discover/

# Setup script
COPY packages/appserver-service/carbonio-mailbox-nslookup /usr/local/bin/carbonio-mailbox-nslookup
RUN chmod +x /usr/local/bin/carbonio-mailbox-nslookup

ENV SERVICE_NAME=carbonio-mailbox-nslookup
ENV SETUP_SCRIPT="carbonio-mailbox-nslookup setup"