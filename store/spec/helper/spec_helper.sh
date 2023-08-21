#!/bin/bash
#shellcheck shell=bash

set -eu

# shellcheck disable=SC2039

spec_helper_precheck() {
  minimum_version "0.28.1"
  if [[ "${SHELL_TYPE}" != "bash" ]]; then
    abort "Only bash is supported."
  fi

  if [[ "$(command -v docker)" = "" ]]; then
        abort "docker executable is mandatory in order to run tests."
  fi
}
