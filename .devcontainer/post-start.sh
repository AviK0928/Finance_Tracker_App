#!/usr/bin/env bash
# Runs every time the codespace/container starts: brings up local Postgres.
set -euo pipefail

# docker-in-docker can take a few seconds to come up after a (re)start
for _ in $(seq 1 30); do
  docker info > /dev/null 2>&1 && break
  sleep 2
done

docker compose up -d db
