#!/bin/sh
#Script to setup Jena Fuseki database with a docker image
echo "Starting fuseki docker setup script"
docker run -p 3030:3030 --name fuseki1 stain/jena-fuseki --update --mem /ds &&
wait
echo "Finished fuseki docker setup script"
 