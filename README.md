# ClusterVAS

A docker based node manager and an orchestrator SDK to build an OpenVAS cluster

* Project Wiki: https://gitlab.com/opentoolset/clustervas/-/wikis/home
* Project Web site: https://opentoolset.org/


## Features
* Management of containerized OpenVAS instances and their life-cycles
* Support for parallel vulnerability scanning through loading multiple OpenVAS containers in parallel
* Remote access to one or more node managers via provided SDK for a completely distrubited solution
* Support for remote management of OpenVAS scanning process with delegation of GMP commands through SDK
* TLS securith with mutual certificate authentication between node managers and orchestrators
* Controlling the node manager at runtime with provided command line interface

## ClusterVAS System Architecture

![clustervas-architecture](uploads/5e0cf4767af08f33a8dd4c986f3209c5/clustervas-architecture.png)