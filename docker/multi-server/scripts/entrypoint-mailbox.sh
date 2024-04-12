#!/bin/bash

/scripts/setup_repo.sh

echo "Waiting for service ${1}..."
wait-for-it "${1}" -t 30 --
echo "ok, moving on."

/scripts/carbonio-bootstrap.sh

echo "tail -f /dev/null"
tail -f /dev/null