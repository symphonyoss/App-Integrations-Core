#!/bin/bash

# Run integrations locally by simulating what Travis would do

. ./env.sh

mvn clean install -Dmaven.test.skip=true -pl \!integration-provisioning

# Download certs... we'll skip this as we already have them in a local ./certs folder
# curl -s https://raw.githubusercontent.com/symphonyoss/contrib-toolbox/master/scripts/download-files.sh | bash

# Inject vars
rm -rf application.yaml
curl -s https://raw.githubusercontent.com/symphonyoss/contrib-toolbox/master/scripts/inject-vars.sh | bash -s -- ./docs/configuration/application.yaml.template application.yaml

rm -rf tomcat ; mkdir tomcat

java $LIBS_PATH \
-Dlogs.basedir=target \
-Dlog4j.configurationFile=./docs/configuration/log4j.properties \
-jar integration-web/target/integration.jar \
--spring.config.location=$PWD \
--server.tomcat.basedir=$PWD/tomcat
