FROM ubuntu:14.04

MAINTAINER Lauren Wolfe “lulumialu@gmail.com”

RUN apt-get update
RUN apt-get install -y nginx
RUN apt-get install -y nano

COPY /html/ /usr/share/nginx/html/
COPY /keylines/ /usr/share/nginx/html/keylines/
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD nginx -g "daemon off;"