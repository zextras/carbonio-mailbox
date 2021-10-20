#!/bin/bash
set -ex
SRCDIR=/src
PKGDIR=/pkg

# Zextras Configuration Variables
. build.config

_timestamp=$(date +'%Y%m%d')
_buildno=$(date +'%Y%m')${BUILD_NUMBER}

prepare() {
  rm -rf "${PKGDIR}/*"
}

check_os_tag() {
  local distro
  distro="$("$SRCDIR"/zm-build/rpmconf/Build/get_plat_tag.sh)"

  local ostag
  case ${distro} in
    ALPN_64*)
      ostag="alpn"
      ;;
    ASTRALINUX_64*)
      ostag="a2"
      ;;
    UBUNTU20_64*)
      ostag="u20"
      ;;
    UBUNTU18_64)
      ostag="u18"
      ;;
    RHEL8*)
      ostag="r8"
      ;;
    RHEL7*)
      ostag="r7"
      ;;
  esac
  echo "${distro}.${ostag}"
}

build() {
  local os_tag
  os_tag="$(check_os_tag)"

  # Set JDK 8 as runtime on Distros
  if [[ "${os_tag%%.*}" =~ "UBUNTU" ]]; then
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
    export JAVA_PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin
  fi

  if [[ "${os_tag%%.*}" =~ "ALPN" ]]; then
    export JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk
    export JAVA_PATH=/usr/lib/jvm/java-1.8-openjdk/bin
  fi

  if [[ "${os_tag%%.*}" =~ "ASTRALINUX" ]]; then
    export JAVA_HOME=/lib/jvm/java-8-openjdk-amd64
    export JAVA_PATH=/lib/jvm/java-8-openjdk-amd64/bin
  fi

  # Zimbra building
  cd "${SRCDIR}"/zm-build || exit

  # Let's build for real now
  ./build.pl --build-ts="${_timestamp}" --build-no="${_buildno}" \
    --build-release=${_release} --build-release-no=${_releaseno} \
    --build-os="${os_tag%%.*}" --build-release-candidate=${_rc} --build-type=${_buildtype} \
    --build-arch="amd64" --build-os="${os_tag%%.*}" --no-interactive \
    --build-prod-flag --disable-bundle --ant-options="-DskipTests=1"
}

package() {
  # Let's package zextras core files
  local os_tag
  os_tag="$(check_os_tag)"
  cd "${SRCDIR}"/ || exit
  cd .staging/""${os_tag%%.*}"-${_release}-${_major}${_minor}${_micro}-${_timestamp}-${_buildtype}-${_buildno}" || exit
  find . -type d -name "opt" -exec rsync -av {} "${PKGDIR}" \;
  find . -type d -name "opt" | xargs rm -rf
  find . -type d -name "etc" -exec rsync -av {} "${PKGDIR}" \;
  find . -type d -name "etc" | xargs rm -rf

  cd "${SRCDIR}/zm-mailbox/build/stage/"
  find . -type d -name "opt" -exec rsync -av {} "${PKGDIR}" \;
  cd "${SRCDIR}/zm-admin-console/build/stage/"
  find . -type d -name "opt" -exec rsync -av {} "${PKGDIR}" \;
  cd "${SRCDIR}/zm-web-client/build/stage/"
  find . -type d -name "opt" -exec rsync -av {} "${PKGDIR}" \;
  cd "${SRCDIR}/zm-timezones/build/stage"
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
