#!/bin/sh
#
# Shell script to start the server application, with debug enabled.
#

# Get the current version...
source ./version.sh

cd testDeploy
QVCS_HOME=`pwd`;
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Xmx256M -jar qvcse-server-$QVCS_VERSION.jar "$QVCS_HOME" 9887 9888 9889 9890 9080
cd ..
