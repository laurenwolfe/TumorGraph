#!/bin/sh
docker stop visualize
docker rm visualize
docker build -t lulumialu/visualize nginx/
docker run -d -p 8080:80 --name visualize lulumialu/visualize