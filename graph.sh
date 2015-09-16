#!/bin/sh
docker stop graph
docker rm graph
docker build -t --rm lulumialu/tumorgraph .
docker run -it -p 8182:8182 --name graph lulumialu/tumorgraph