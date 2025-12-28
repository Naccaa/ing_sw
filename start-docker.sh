#!/bin/bash
set -e

if [[ "$(systemctl is-active docker)" != "active" ]]; then
    systemctl start docker
fi
