#!/bin/bash

/scripts/setup_repo.sh

echo "Waiting for service ${1}..."
wait-for-it "${1}" -t 30 --
echo "ok, moving on."

/scripts/carbonio-bootstrap.sh

su - zextras -c "/opt/zextras/bin/zmmailboxdctl stop || exit 1"

su - zextras -c "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /scripts/zm-store-jar-with-dependencies.jar -webDescriptor=/opt/zextras/jetty_base/webapps/service/WEB-INF/web.xml"

tail -f /dev/null