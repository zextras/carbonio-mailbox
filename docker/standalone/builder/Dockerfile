FROM maven:3-eclipse-temurin-17-alpine AS builder

RUN apk add --no-cache make gcc g++ musl-dev

WORKDIR /app
ARG MAVEN_OPTS=""

# Copy all project files
COPY . .

RUN --mount=type=cache,target=/root/.m2 \
     sh -c 'export MAVEN_OPTS="${MAVEN_OPTS}" && mvn clean install -DskipTests && mkdir -p /mailbox'
RUN cp -a store* milter* attribute-manager right-manager \
    mailbox-ldap-lib native client common packages soap jython-libs \
    /mailbox/

FROM scratch AS export
COPY --from=builder /mailbox /mailbox