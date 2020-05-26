FROM ubuntu:18.04

LABEL "product"="ClusterVAS"
LABEL "vendor"="ClusterVAS Team"

RUN apt update
RUN apt-get -y dist-upgrade
RUN apt-get install -y openjdk-8-jre-headless
RUN apt-get install -y docker.io
RUN apt-get install -y bash-completion less vim net-tools iputils-ping traceroute telnet

RUN mkdir -p /clustervas/
COPY clustervas-manager /clustervas/
COPY startup.sh         /clustervas/

RUN chmod u+x /clustervas/startup.sh

# Ana dizine konumlanilir:
WORKDIR /clustervas

# Startup
CMD /clustervas/startup.sh
