#!/bin/sh
docker stop tumorgraph
docker rm tumorgraph
docker build -t lulumialu/tumorgraph .
docker run -it -p 8182:8182 --name tumorgraph lulumialu/tumorgraph