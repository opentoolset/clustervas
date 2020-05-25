FROM ubuntu:18.04

LABEL "product"="ClusterVAS"
LABEL "vendor"="ClusterVAS Team"

RUN mkdir -p /clustervas/
COPY clustervas-manager.jar /clustervas/
COPY startup.sh             /clustervas/

RUN chmod a+x /clustervas/startup.sh

# Ana dizine konumlanilir:
WORKDIR /clustervas

# Startup
CMD /clustervas/startup.sh
