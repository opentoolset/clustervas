FROM cydecsys-probe-base-openvas-10:latest

LABEL "product"="ClusterVAS"
LABEL "vendor"="ClusterVAS Team"

RUN mkdir -p /cydecsys/
COPY cydecsys-probe.jar         /cydecsys/
COPY startup.sh                 /cydecsys/

RUN chmod a+x /cydecsys/startup.sh

# Asagisi, ileride sifre atamasi gerekirse acilacak:
# RUN echo 'root:secret' | chpasswd

# Asagidaki dizinin gorevi bitti. Simdi silinecek ve calisma aninda otomatik olusturulacak:
RUN rm /cydecsys/data

# Ana dizine konumlanilir:
WORKDIR /cydecsys

# Startup
CMD /cydecsys/startup.sh; /bin/bash
