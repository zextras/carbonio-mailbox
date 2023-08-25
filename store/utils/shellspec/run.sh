#!/bin/bash

set -e

#cd "$(dirname "$0")"

if [ ! -e "utils/shellspec/lib/shellspec" ]; then
  (./utils/shellspec/install.sh)
fi

if [ "${CI}" != "true" ]; then
  utils/shellspec/lib/shellspec/shellspec "${@}"
else
  echo "Shellspec is temporarily disabled on Jenkins"
fi
