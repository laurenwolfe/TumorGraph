#Version 0.7

FROM ubuntu:14.04
MAINTAINER Lauren Wolfe “lulumialu@gmail.com”

ENV TITAN_VER titan-0.5.4-hadoop2

RUN apt-get install -y --fix-missing software-properties-common
RUN apt-get install -y --fix-missing python3-software-properties
#RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN apt-get install -y wget unzip
#RUN echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
#RUN apt-get install -y oracle-java7-installer wget unzip
RUN apt-get install -y default-jre
RUN apt-get install -y default-jdk

RUN wget -O /tmp/titan.zip http://s3.thinkaurelius.com/downloads/titan/$TITAN_VER.zip

WORKDIR /opt/

RUN unzip /tmp/titan.zip
RUN rm /tmp/titan.zip

WORKDIR /opt/$TITAN_VER/

COPY /main/conf/rexster-cassandra-es.xml /opt/$TITAN_VER/conf/
COPY /main/groovy/PWLoad.groovy /opt/$TITAN_VER/
COPY /main/groovy/schema.groovy /opt/$TITAN_VER/
COPY /main/groovy/loadToExisting.groovy /opt/$TITAN_VER/
COPY /data/filenames.tsv /opt/$TITAN_VER/
COPY /data/stad.all.17jan15.TP.pwpv /opt/$TITAN_VER/
#COPY brca.seq.20150903_private.TP.pwpv /opt/$TITAN_VER/

RUN mkdir -p /rexhome/ext/titan
RUN cp -r /lib/*.* /rexhome/ext/titan

EXPOSE 8182

CMD ["/bin/bash"]