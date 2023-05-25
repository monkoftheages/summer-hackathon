OWNER?=      dublin
PROGRAM?=    sentiment-analysis
MAINTAINER?= dublin-eng
COMMIT?=     $(shell git rev-parse HEAD)
PYMD5?=      $(shell find . \( -name '*.py' -o -name '*.sh' \) -exec cat {} + | md5)
TAG?=        $(COMMIT)-$(PYMD5)
REGISTRY?=   190405961846.dkr.ecr.us-west-2.amazonaws.com
IMG?=        $(REGISTRY)/$(PROGRAM):$(TAG)
IMG_NAME=    sentiment-analysis

-include settings.mk
include $(CURDIR)/makefiles/*.mk


.PHONY: build
build: docker-build

.PHONY: test
test: build-test-image
