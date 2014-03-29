#!/bin/sh
#
# Shell script to start the client application with debug enabled.
#

# Get the current version...
source ./version.sh

cd testDeploy
QVCS_HOME=`pwd`;
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 -Xmx256M -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
cd ..
