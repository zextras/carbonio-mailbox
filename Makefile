.ONESHELL:
.SHELLFLAGS := -ec

build:
	mvn clean install -DskipTests -Dfile.encoding=UTF-8

tests:
	mvn jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -DexcludedGroups=api,flaky,e2e

flaky-api-e2e-tests:
	cd store && mvn jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -Dgroups=flaky,api && mvn jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -Dgroups=e2e

api-tests:
	cd store && mvn verify -Dgroups=api -Dfile.encoding=UTF-8

flaky-tests:
	cd store && mvn verify -Dgroups=flaky -Dfile.encoding=UTF-8

e2e-tests:
	cd store && mvn verify -Dgroups=e2e -Dfile.encoding=UTF-8

api-docs:
	( cd soap && mvn antrun:run@generate-soap-docs )
	VERSION=$$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
	mkdir -p docs
	tar -czf docs/carbonio-mailbox-api-docs-$${VERSION}.tar.gz -C soap/target/docs/soap .

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

