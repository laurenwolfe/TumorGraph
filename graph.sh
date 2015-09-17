#!/bin/sh
docker stop graph
docker rm graph
docker build -t lulumialu/tumorgraph .
docker run -it -p 8182:8182 --name graph lulumialu/tumorgraph