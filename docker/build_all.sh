#!/bin/bash
set -ex
SRCDIR=/src
PKGDIR=/pkg

# Zextras Configuration Variables
. build.config

_buildno=$(date +'%Y%m')${BUILD_NUMBER}

prepare() {
  rm -rf "${PKGDIR}/*"
}

build() {

  # Set JDK 8 as runtime on Distros
  # Alpine JAVA_HOME & JAVA_PATH
  # export JAVA_HOME=/lib/jvm/java-8-openjdk-amd64
  # export JAVA_PATH=/lib/jvm/java-8-openjdk-amd64
  export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/
  export JAVA_PATH=/usr/lib/jvm/java-11-openjdk-amd64/bin

  # Use a custom script to use a defined clang version and a priority
  clang-switch.sh 10 100

  # Let's build for real now
  cd "${SRCDIR}"/zm-build || exit
  ./build.pl --build-no="${_buildno}" \
    --build-release-no="${_releaseno}" \
    --build-release-candidate="${_rc}" \
    --build-prod-flag --ant-options="-DskipTests=1"
}

package() {
  # Let's package core files
  cd "${SRCDIR}"/ || exit
  cd .staging/"${_major}${_minor}${_micro}-FOSS-${_buildno}" || exit
  find . -type d -name "opt" -exec rsync -av {} "${PKGDIR}" \;
  find . -type d -name "opt" | xargs rm -rf
  find . -type d -name "etc" -exec rsync -av {} "${PKGDIR}" \;
  find . -type d -name "etc" | xargs rm -rf

  cd "${SRCDIR}/zm-admin-console/build/stage/"
  find . -type d -name "opt" -exec rsync -av {} "${PKGDIR}" \;
  cd "${SRCDIR}/zm-web-client/build/stage/"
  find . -type d -name "opt" -exec rsync -av {} "${PKGDIR}" \;

  chmod 750 -R "${PKGDIR}/etc/sudoers.d"
  chmod +x "${PKGDIR}"/opt/zextras/bin/*
}

process_args() {
  # Process other arguments.
  BUILD_NUMBER=$1

  case ${BUILD_NUMBER} in
    '' | *[!0-9]*)
      echo "Build number required"
      exit 1
      ;;
    *)
      build
      package
      ;;
  esac
}

prepare
process_args "$@"
