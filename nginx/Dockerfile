#FROM ubuntu:14.04
FROM ubuntu:14.04

MAINTAINER Lauren Wolfe “lulumialu@gmail.com”

RUN apt-get install -y --fix-missing software-properties-common
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN apt-get -y dist-upgrade
RUN apt-get install -y wget unzip
RUN apt-get install -y wget unzip
RUN apt-get install -y curl
RUN echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java7-installer wget unzip
RUN apt-get install -y groovy

RUN apt-get install -y nginx
RUN apt-get install -y nano

COPY /html/ /usr/share/nginx/html/
COPY /js/ /usr/share/nginx/html/js/
COPY /css/ /usr/share/nginx/html/css/
COPY /css/images/ /usr/share/nginx/html/css/images/
COPY /css/fonts/ /usr/share/nginx/html/css/fonts/

RUN rm -v /etc/nginx/nginx.conf

COPY nginx.conf /etc/nginx/nginx.conf
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

EXPOSE 80

CMD service nginx start