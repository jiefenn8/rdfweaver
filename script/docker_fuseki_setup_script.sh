#!/bin/sh
#Script to setup Jena Fuseki database with a docker image
echo "Starting fuseki docker setup script"
docker run -d -e FUSEKI_DATASET_1=ds -p 3030:3030 --name fuseki01 stain/jena-fuseki
wait
echo "Finished fuseki docker setup script"
 