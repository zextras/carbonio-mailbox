#!/bin/bash

set -e

SHELLSPEC_VERSION="0.28.1"

cd "$(dirname "${0}")"

if [[ ! -d "lib" ]]
then
  mkdir lib
  (
    cd lib
    curl -ssLOJS "https://github.com/shellspec/shellspec/archive/${SHELLSPEC_VERSION}.tar.gz"
    tar -zxf "shellspec-${SHELLSPEC_VERSION}.tar.gz"
    mv "shellspec-${SHELLSPEC_VERSION}" "shellspec"
    rm "shellspec-${SHELLSPEC_VERSION}.tar.gz"
  )
fi
