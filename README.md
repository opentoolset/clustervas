# ClusterVAS

A [Docker](https://www.docker.com/) based node manager and an orchestrator SDK (for Java applications) to build an [OpenVAS](https://www.openvas.org/) cluster

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

![clustervas-architecture](https://gitlab.com/opentoolset/clustervas/-/wikis/uploads/bb94be7ebb739e3ced107ae5aa41d3c0/clustervas-architecture.png)

# Installation and Usage

* [ClusterVAS SDK Tutorial](https://gitlab.com/opentoolset/clustervas/-/wikis/ClusterVAS-SDK-Tutorial)
* [ClusterVAS Node Manager Installation and Startup](https://gitlab.com/opentoolset/clustervas/-/wikis/ClusterVAS-Node-Manager-Installation)
* [Integration and Sample Operations](https://gitlab.com/opentoolset/clustervas/-/wikis/Integration-and-Sample-Operations)