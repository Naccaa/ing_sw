#!/bin/bash
set -e

sh start-docker.sh
docker-compose rm db -fsv
docker-compose down -v
docker-compose --profile dev_all up --build