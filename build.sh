#!/usr/bin/env bash

echo "Building integration bridge"
mvn clean install -P docker
