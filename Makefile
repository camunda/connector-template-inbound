.DEFAULT_GOAL := help

ifdef CURRENTTAG
CURRENTTAG := $(shell git describe --tags --abbrev=0)
else
CURRENTTAG := latest
endif

IMAGE_NAME := connector-template-inbound
IMAGE_VERSION := $(CURRENTTAG)


#help: @ List available tasks
help:
	@clear
	@echo "Usage: make COMMAND"
	@echo "Commands :"
	@grep -E '[a-zA-Z\.\-]+:.*?@ .*$$' $(MAKEFILE_LIST)| tr -d '#' | awk 'BEGIN {FS = ":.*?@ "}; {printf "\033[32m%-14s\033[0m - %s\n", $$1, $$2}'

#clean: @ Cleanup
clean:
	@rm -rf target dependency-reduced-pom.xml

#build: @ Build JAR
build:
	mvn clean package -DskipTests
 
#image-build: @ Build a Docker image
image-build: build
	docker buildx build --load -t $(IMAGE_NAME):$(IMAGE_VERSION) -f Dockerfile .

#container-logs: @ Docker Container logs
container-logs:
	docker logs 'connectors' --follow

#compose-up: @ Docker Compose Up
compose-up:
	cd camunda-local && docker compose -f docker-compose-core.yaml up -d

#compose-down: @ Docker Compose Down
compose-down:
	cd camunda-local && docker compose -f docker-compose-core.yaml down
