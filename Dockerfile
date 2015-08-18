#Version 0.6

FROM ubuntu:14.04
MAINTAINER Lauren Wolfe “lulumialu@gmail.com”

ENV TITAN_VER titan-0.5.4-hadoop2

RUN apt-get install -y python3-software-properties
RUN apt-get install -y software-properties-common
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN apt-get -y dist-upgrade
RUN apt-get install -y wget unzip
RUN echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java7-installer wget unzip
RUN wget -O /tmp/titan.zip http://s3.thinkaurelius.com/downloads/titan/$TITAN_VER.zip

WORKDIR /opt/

RUN unzip /tmp/titan.zip
RUN rm /tmp/titan.zip

COPY rexster-cassandra-es.xml /opt/$TITAN_VER/conf/
COPY PWLoad.groovy /opt/$TITAN_VER/
COPY filenames.tsv /opt/$TITAN_VER/
COPY stad.all.16jan15.TP.pwpv /opt/$TITAN_VER/

WORKDIR /opt/$TITAN_VER/

RUN mkdir -p /rexhome/ext/titan
RUN cp -r /lib/*.* /rexhome/ext/titan

EXPOSE 8182 8183 8184