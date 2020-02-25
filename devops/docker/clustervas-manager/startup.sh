#!/bin/bash

# ---
# Copyright 2020 ClusterVAS Team
# All rights reserved
#
# Startup script for ClusterVAS Manager container
# ---

mkdir -p /cydecsys/data/app/config
mkdir -p /cydecsys/data/app/security

ln -s /cydecsys/data/app/config   /cydecsys/config
ln -s /cydecsys/data/app/security /cydecsys/security

redis-server --unixsocket /tmp/redis.sock --unixsocketperm 700 --timeout 0 --databases 128 --maxclients 512 --daemonize yes --port 6379 --bind 0.0.0.0
echo "Testing redis status..."
X="$(redis-cli -s /tmp/redis.sock ping)"
while  [ "${X}" != "PONG" ]; do
	echo "Redis not yet ready..."
	sleep 1
	X="$(redis-cli -s /tmp/redis.sock ping)"
done
echo "Redis ready."

echo "Starting services"
openvassd
gvmd
gsad --verbose --http-only --no-redirect --port=9392

if [ ! -f "/firstrun" ]; then
  echo "Setting up user"
  gvmd --create-user=admin --password=admin
  touch /firstrun
fi

# export PS1='\u@$(hostname):\w\$ '

export CYDECSYS_HOME=/cydecsys
cd $CYDECSYS_HOME
nohup java -jar $CYDECSYS_HOME/cydecsys-probe.jar &
