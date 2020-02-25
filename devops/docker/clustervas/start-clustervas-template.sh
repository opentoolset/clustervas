#!/bin/bash

# ---
# Copyright 2020 ClusterVAS Team
# All rights reserved
#
# Startup script for ClusterVAS container
# ---

set -e

unset TEST

println() {
	echo $1
}

print_seperator() {
	println "----------------------------------------"
}

log_time() {
	print_seperator
	println $(date -Iseconds)
	print_seperator
}

run () {
	print_seperator
	println "$*"
	if [ "$TEST" ]; then
		return 0
	fi
	
	eval "$@"
}

help() {
	println "Valid options:"
	println "-d <data folder>"
	println "-t test/dry-run mode"
	println "-? help"
}

main()
{
	while getopts "d:t\?" opt; do
	  case "$opt" in
	    d)
	      DATA_FOLTER=$OPTARG
	      ;;
	    t)
			  TEST="test"
			  println "Dry-run mode..."
	      ;;
	    \?)
	    	help
	      exit
	      ;;
	  esac
	done

	if [ -z "$DATA_FOLTER" ]; then
		println "Missing parameter: data folder"
		help
		exit 1
	fi

	CONTAINER_NAME=clustervas-template

	run docker run -itd \
#	-p 8080:9392 \
#	-v /var/run/docker.sock:/var/run/docker.sock --privileged \
	-v $DATA_FOLTER:/clustervas/data \
	--name $CONTAINER_NAME \
	clustervas
	
#	DOCKER_EXEC="docker exec $CONTAINER_NAME bash -c"
	
#	run $DOCKER_EXEC \'gsad --verbose --http-only --no-redirect --port=9392\'
}

time main "$@"