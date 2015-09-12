#!/bin/sh
docker stop graph
docker rm graph
rm -rf TumorGraph
git clone https://github.com/laurenwolfe/TumorGraph.git
cd TumorGraph
docker build -t lulumialu/tumorgraph .
docker run -it -p 8182:8182 --name graph lulumialu/tumorgraph