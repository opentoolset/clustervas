FROM clustervas-base:latest

LABEL "product"="ClusterVAS"
LABEL "vendor"="ClusterVAS Team"

RUN rm -r /usr/local/var/lib/openvas/plugins; ln -s /clustervas/data/usr/local/var/lib/openvas/plugins /usr/local/var/lib/openvas/
RUN rm -r /usr/local/var/lib/gvm/scap-data;   ln -s /clustervas/data/usr/local/var/lib/gvm/scap-data   /usr/local/var/lib/gvm/
RUN rm -r /usr/local/var/lib/gvm/cert-data;   ln -s /clustervas/data/usr/local/var/lib/gvm/cert-data   /usr/local/var/lib/gvm/

RUN mkdir -pv /clustervas
WORKDIR /clustervas

COPY startup.sh /clustervas/
RUN chmod u+x   /clustervas/startup.sh

# Startup
CMD /clustervas/startup.sh; /bin/bash
