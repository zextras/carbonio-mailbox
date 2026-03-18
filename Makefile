VERSION := $(shell cat version.txt)

build:
	mvn clean install -DskipTests

tests:
	mvn verify -DexcludedGroups=api,flaky,e2e

api-tests:
	cd store && mvn verify -Dgroups=api

flaky-tests:
	cd store && mvn verify -Dgroups=flaky

e2e-tests:
	cd store && mvn verify -Dgroups=e2e

build-packages: build
	./build_packages.sh	

sys-install:
	./install_packages.sh ${HOST}

sys-deploy: build-packages sys-install sys-restart

sys-status:
	@$(call execute_zextras_cmd, "zmmailboxdctl status")

sys-restart:
	@$(call execute_zextras_cmd, "zmmailboxdctl restart")

define execute_zextras_cmd
  ssh root@${HOST} "su - zextras -c '$(1)'"
endef

