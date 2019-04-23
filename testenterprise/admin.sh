#!/bin/bash
#
# Shell script to start the admin application with debug enabled.
#

# Get the current version...
source ./version.sh

cd testDeploy
QVCS_HOME=`pwd`;
#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007 -Dapple.laf.useScreenMenuBar=true -Xmx256M -jar qvcse-admin-$QVCS_VERSION.jar "$QVCS_HOME"
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007 -Dapple.laf.useScreenMenuBar=true -Xmx256M -jar qvcse-admin-$QVCS_VERSION.jar
cd ..
