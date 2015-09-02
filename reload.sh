#!/bin/sh
docker stop keylines
docker rm keylines
docker build -t lulumialu/keylines nginx/
docker run -d -p 8080:80 --name keylines lulumialu/keylines