# ClusterVAS

A docker based node manager and an orchestrator SDK (for Java applications) to build an OpenVAS cluster

* Project Wiki: https://gitlab.com/opentoolset/clustervas/-/wikis/home
* Project Web site: https://opentoolset.org/


## Features
* Management of containerized OpenVAS instances and their life-cycles
* Support for parallel vulnerability scanning through loading multiple OpenVAS containers in parallel
* Remote access to one or more node managers via provided SDK for a completely distrubited solution
* Support for remote management of OpenVAS scanning process with delegation of GMP commands through SDK
* TLS securith with mutual certificate authentication between node managers and orchestrators
* Controlling the node manager at runtime with provided command line interface

## System Architecture

![clustervas-architecture](https://gitlab.com/opentoolset/clustervas/-/wikis/uploads/55c7c204b97f865c661e2517de2948e3/clustervas-architecture.png)