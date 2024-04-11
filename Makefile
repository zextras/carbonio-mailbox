VERSION := $(shell cat version.txt)

build:
	mvn clean install -DskipTests

build-packages: build
	./build_packages.sh	

sys-install:
	./install_packages.sh ${HOST}

sys-deploy: build-packages sys-install sys-restart

sys-status:
	@$(call execute_zextras_cmd, "zmmailboxdctl status")

sys-restart:
	@$(call execute_zextras_cmd, "zmmailboxdctl restart")

help:
	@echo "rc-start: starts the rc by creating a new RC branch, bumping the version and opening a PR to main"
	@echo "rc-finish: finishes the rc by merging the RC PR, checkout main, pull and tag the latest merge commit"

tag:
	git checkout main
	git pull
	git tag $(VERSION)
	git push origin $(VERSION)

rc-start:
	git checkout devel
	git pull
	git checkout -B chore/RC
	release-it --ci
	gh pr create -B main -b "Open RC for $(VERSION)" --title "chore: RC $(VERSION)"

rc-merge:
	gh pr merge chore/RC -m

rc-finish: rc-merge tag

define execute_zextras_cmd
  ssh root@${HOST} "su - zextras -c '$(1)'"
endef

