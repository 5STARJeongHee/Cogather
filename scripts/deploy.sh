#!/bin/bash

REPOSITORY=/home/ubuntu/app/step2

PROJECT_NAME=cogather-webservice

echo "> Build 파일 복사"
sudo docker stop cogather
sudo docker rm cogather 
rm -rf $REPOSITORY/zip/docker/webapps/*.war
mv $REPOSITORY/zip/*.war $REPOSITORY/zip/docker/webapps/cogather.war
# cp $REPOSITORY/zip/cogather.war $REPOSITORY/zip/docker/webapps/cogather.war

cd $REPOSITORY/zip/docker && sudo docker-compose up -d 