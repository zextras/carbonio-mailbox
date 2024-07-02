#!/bin/bash

/scripts/setup_repo.sh

/scripts/carbonio-bootstrap.sh

HOSTNAME=$(hostname -i)
wait-for-it "${HOSTNAME}":389 -t 30 --

tail -f /dev/null