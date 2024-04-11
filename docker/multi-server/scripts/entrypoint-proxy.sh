#!/bin/bash

/scripts/setup_repo.sh
apt install service-discover-agent -y

echo "Waiting for Service ${1}..."
wait-for-it "${1}" -t 30 --
echo "service is up."

/scripts/carbonio-bootstrap.sh

tail -f /dev/null