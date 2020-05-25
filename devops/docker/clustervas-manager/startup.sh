#!/bin/bash

# ---
# Copyright 2020 ClusterVAS Team
# All rights reserved
#
# Startup script for ClusterVAS Manager container
# ---

export CLUSTERVAS_HOME=/clustervas
cd $CLUSTERVAS_HOME && ./clustervas-manager
