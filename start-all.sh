#!/bin/bash
set -e

docker-compose rm db -fsv
docker-compose down -v
docker-compose --profile dev_all up --build