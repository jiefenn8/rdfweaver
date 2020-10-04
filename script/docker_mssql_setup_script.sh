#!/bin/sh
#Script to setup and populate mssql database with a docker image
docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=YourStrong@Passw0rd' -p 1433:1433 --name sql1 -v $TRAVIS_BUILD_DIR/script:/script -d mcr.microsoft.com/mssql/server:2019-latest
docker exec -it sql1 /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -i /script/sqlserver_test_db.sql
 