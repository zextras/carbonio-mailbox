FROM localhost/carbonio-sidecar-base:latest

# Service registration
COPY packages/appserver-service/carbonio-mailbox-admin.hcl /consul/config/

# Setup files
COPY packages/appserver-service/carbonio-mailbox-admin-service-protocol.json /etc/carbonio/mailbox-admin/service-discover/
COPY packages/appserver-service/carbonio-mailbox-admin-intentions.json       /etc/carbonio/mailbox-admin/service-discover/
COPY packages/appserver-service/carbonio-mailbox-admin-policies.json         /etc/carbonio/mailbox-admin/service-discover/

# Setup script
COPY packages/appserver-service/carbonio-mailbox-admin /usr/local/bin/carbonio-mailbox-admin
RUN chmod +x /usr/local/bin/carbonio-mailbox-admin

ENV SERVICE_NAME=carbonio-mailbox-admin
ENV SETUP_SCRIPT="carbonio-mailbox-admin setup"