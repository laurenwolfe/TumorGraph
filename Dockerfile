#Version 0.7

FROM ubuntu:14.04
#FROM nginx
MAINTAINER Lauren Wolfe “lulumialu@gmail.com”

ENV TITAN_VER titan-0.5.4-hadoop2

RUN apt-get install -y python3-software-properties
RUN apt-get install -y software-properties-common
RUN apt-get install -y dpkg-dev
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN apt-get -y dist-upgrade
RUN apt-get install -y wget unzip

RUN apt-get install -y nginx
RUN echo "\ndaemon off;" >> /etc/nginx/nginx.conf
RUN mkdir /etc/nginx/ssl
ADD default /etc/nginx/sites-available/default


RUN echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java7-installer wget unzip

RUN wget -O /tmp/cassandra.tar.gz http://mirror.metrocast.net/apache/cassandra/2.1.8/apache-cassandra-2.1.8-bin.tar.gz
RUN wget -O /tmp/titan.zip http://s3.thinkaurelius.com/downloads/titan/$TITAN_VER.zip

WORKDIR /opt/

RUN unzip /tmp/titan.zip
RUN rm /tmp/titan.zip
RUN tar -xvzf /tmp/cassandra.tar.gz
RUN rm /tmp/cassandra.tar.gz

WORKDIR /opt/$TITAN_VER/

COPY /main/conf/rexster-cassandra-es.xml /conf/
COPY /main/groovy/PWLoad.groovy /
COPY /data/filenames.tsv /
COPY /data/stad.all.16jan15.TP.pwpv /
COPY /main/html/* /html/

RUN mkdir -p /rexhome/ext/titan
RUN cp -r /lib/*.* /rexhome/ext/titan

RUN nginx
RUN bin/titan.sh start

EXPOSE 80 8182

CMD ["/bin/bash"]