FROM ubuntu:18.04

LABEL "product"="ClusterVAS"
LABEL "vendor"="ClusterVAS Team"

COPY install/*.deb        /install/
COPY install/*.tar.gz     /install/
COPY install-packages.sh  /install-packages.sh

ENV DEBIAN_FRONTEND=noninteractive
ENV LANG=C.UTF-8

RUN echo 'Acquire::http { Proxy "http://169.254.0.1:3142"; };' | cat >> /etc/apt/apt.conf
RUN apt-get update
RUN apt-get dist-upgrade -y
RUN bash /install-packages.sh

RUN apt autoremove cmdtest --purge
RUN dpkg -i /install/yarn_1.19.1_all.deb
RUN rm -rf /var/lib/apt/lists/*

RUN echo "Starting Build..." && mkdir -pv /install && mkdir -pv /build

# --- Greenbone Vulnerability Management (GVM) Solution
RUN cd /build && \
	tar -zxvf /install/gvm-libs-v10.0.1.tar.gz && \
	cd /build/*/ && \
	mkdir build && \
	cd build && \
	cmake -DCMAKE_BUILD_TYPE=Release .. && \
	make && \
	make install && \
	cd /build && \
	rm -rf *

# --- SMB module for the OpenVAS Scanner
RUN cd /build && \
	tar -zxvf /install/openvas-smb-v1.0.5.tar.gz && \
	cd /build/*/ && \
	mkdir build && \
	cd build && \
	cmake -DCMAKE_BUILD_TYPE=Release .. && \
	make && \
	make install && \
	cd /build && \
	rm -rf *
    
# --- Greenbone Vulnerability Manager Daemon (GVMD)
RUN cd /build && \
	tar -zxvf /install/gvmd-v8.0.1.tar.gz && \
	cd /build/*/ && \
	mkdir build && \
	cd build && \
	cmake -DCMAKE_BUILD_TYPE=Release .. && \
	make && \
	make install && \
	cd /build && \
	rm -rf *
    
# --- OpenVAS Scanner of the Greenbone Vulnerability Management (GVM) Solution
RUN cd /build && \
	tar -zxvf /install/openvas-scanner-v6.0.1.tar.gz && \
	cd /build/*/ && \
	mkdir build && \
	cd build && \
	cmake -DCMAKE_BUILD_TYPE=Release .. && \
	make && \
	make install && \
	cd /build && \
	rm -rf *
    
# --- Greenbone Security Assistant (GSA)
RUN cd /build && \
	tar -zxvf /install/gsa-v8.0.1.tar.gz && \
	cd /build/*/ && \
	mkdir build && \
	cd build && \
	cmake -DCMAKE_BUILD_TYPE=Release .. && \
	make && \
	make install && \
	cd /build && \
	rm -rf *
    
# --- Greenbone Vulnerability Management Python Library
RUN cd /build && \
	tar -zxvf /install/python-gvm-v1.0.0.beta3.tar.gz && \
	cd /build/*/ && \
	python3 setup.py install && \
	cd /build && \
	rm -rf *
    
# --- GVM-Tools
RUN cd /build && \
	tar -zxvf /install/gvm-tools-v2.0.0.beta1.tar.gz && \
	cd /build/*/ && \
	python3 setup.py install && \
	echo "/usr/local/lib" > /etc/ld.so.conf.d/openvas.conf && ldconfig && cd / && rm -rf /build
