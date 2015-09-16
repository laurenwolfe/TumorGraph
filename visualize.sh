#!/bin/sh
docker stop visualize
docker rm visualize
docker build --rm -t lulumialu/visualize nginx/
docker run -d -p 9090:80 --name visualize lulumialu/visualize
say reload complete