#!/usr/bin/env bash

set -e

echo "starting $0..."

[[ -z "$1" ]] && { echo "usage: $0 ENCRYPTION_PASSWORD"; exit 1; } || echo "ENV: $1"

# build the container-definitions.json
sed -i -e "s/{{AWS_CONTAINER_NAME}}/$AWS_CONTAINER_NAME/g" container-definitions.json
sed -i -e "s/{{DOCKER_REPO}}/$DOCKER_REPO/g" container-definitions.json
sed -i -e "s/{{PROJECT_VERSION}}/$PROJECT_VERSION/g" container-definitions.json
sed -i -e "s/{{IMAGE_NAME}}/$IMAGE_NAME/g" container-definitions.json
sed -i -e "s/{{IMAGE_PORT}}/$IMAGE_PORT/g" container-definitions.json
sed -i -e "s/{{IMAGE_MEMORY_RESERVATION}}/$IMAGE_MEMORY_RESERVATION/g" container-definitions.json
sed -i -e "s/{{SPRING_PROFILES_ACTIVE}}/$SPRING_PROFILES_ACTIVE/g" container-definitions.json
cat container-definitions.json
jq '.[].environment += [{"name":"JASYPT_ENCRYPTOR_PASSWORD", "value":"'$1'"}]' container-definitions.json > tmp.json && mv tmp.json container-definitions.json

echo "finished $0"
