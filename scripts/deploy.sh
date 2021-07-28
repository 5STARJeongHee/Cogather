#!/bin/bash

REPOSITORY=/home/ubuntu/app/step2
DESTINATION=/home/ubuntu/Cogather/docker
PROJECT_NAME=cogather-webservice

echo "> Build 파일 복사"
cp $REPOSITORY/zip/**/*.war $DESTINATION/webapps

cd $DESTINATION && compose up -d 