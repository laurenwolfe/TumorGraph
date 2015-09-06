#!/bin/sh
docker stop tumorgraph
docker rm tumorgraph
rm -rf TumorGraph
git clone https://github.com/laurenwolfe/TumorGraph.git
cd TumorGraph
docker build -t lulumialu/tumorgraph .
docker run -it -p 8182:8182 --name tumorgraph lulumialu/tumorgraph
say reload complete