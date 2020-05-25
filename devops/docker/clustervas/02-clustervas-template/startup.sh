#!/bin/bash

# ---
# Copyright 2020 ClusterVAS Team
# All rights reserved
#
# Startup script for initial operations within ClusterVAS container
# ---

echo "ClusterVAS container starting..."

mkdir -pv /clustervas/data/usr/local/var/lib/openvas/plugins
# mkdir -pv /clustervas/data/usr/local/var/lib/gvm/scap-data
# mkdir -pv /clustervas/data/usr/local/var/lib/gvm/cert-data

redis-server --unixsocket /tmp/redis.sock --unixsocketperm 700 --timeout 0 --databases 128 --maxclients 512 --daemonize yes --port 6379 --bind 0.0.0.0
while  [ "$(redis-cli -s /tmp/redis.sock ping)" != "PONG" ]; do
	echo "Redis not yet ready. Retrying...";
	sleep 1;
done
echo "Redis is ready."

# if [ ! -f "/clustervas/firstrun" ]; then
#  echo "Setting up user"
gvmd --create-user=admin --password=admin
#  touch /clustervas/firstrun
# fi

echo "Services are starting..."
openvassd
gvmd

# gsad --verbose --http-only --no-redirect --port=9392

# greenbone-nvt-sync
# greenbone-certdata-sync
# greenbone-scapdata-sync

echo "ClusterVAS container started."
