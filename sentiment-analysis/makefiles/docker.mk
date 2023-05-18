export DOCKER_BUILDKIT=1

export PYTHON_VERSION=$(shell cat .python-version)
export FFF_PYPI_USERNAME=$(shell cat ~/.config/gh/hosts.yml | grep user | awk '{print $$2}' | head -1)
export FFF_PYPI_PASSWORD?=$(shell cat ~/.config/gh/hosts.yml | grep oauth_token | awk '{print $$2}' | head -1)
DOCKER_BUILD=docker build --build-arg PYTHON_VERSION=$(PYTHON_VERSION) --build-arg FFF_PYPI_USERNAME=$(FFF_PYPI_USERNAME) --secret id=FFF_PYPI_PASSWORD

~/.config/gh/hosts.yml:
	gh auth login

.PHONY: docker-build
docker-build: ~/.config/gh/hosts.yml
	$(DOCKER_BUILD) --target=app -t $(IMG_NAME) .

.PHONY: build-test-image
build-test-image: Dockerfile
	$(DOCKER_BUILD) --target=test -t $(IMG_NAME)-test .

.PHONY: build-dev-image
build-dev-image: Dockerfile
	$(DOCKER_BUILD) --target=production -t $(IMG_NAME) .

.PHONY: build-e2e-image
build-e2e-image: e2e/Dockerfile
	$(DOCKER_BUILD) -t $(IMG_NAME)-e2e e2e

.PHONY: docker-compose
docker-compose: docker-compose.yaml
	docker compose up --build

.PHONY: docker-compose-sentiment
docker-compose: docker-compose.yaml
	docker compose up sentiment_analysis_api --build

