VERSION := $(shell cat version.txt)

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
