#!/bin/bash

set -e

#cd "$(dirname "$0")"

if [ ! -e "utils/shellspec/lib/shellspec" ]; then
  (./utils/shellspec/install.sh)
fi

utils/shellspec/lib/shellspec/shellspec "${@}"
