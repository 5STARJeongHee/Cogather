#!/bin/bash

REPOSITORY=/home/ubuntu/app/step2

PROJECT_NAME=cogather-webservice

echo "> Build 파일 복사"
cp $REPOSITORY/zip/*.war $REPOSITORY/docker/webapps

cd $REPOSITORY/docker && sudo docker-compose up -d 