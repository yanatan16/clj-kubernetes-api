#!/usr/bin/env bash
set -e

if [[ -z ${1} ]]; then
    echo "usage: scripts/get_swagger_json.sh <version>"
    exit 1
fi

curl -f "http://kubernetes.io/swagger-spec/api/${1}/" -o resources/swagger/${1}.json
