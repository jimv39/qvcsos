#!/bin/bash
#
# Shell script to start the client application with no debug.
#

# Get the current version...
source ./version.sh

cd testDeploy
QVCS_HOME=`pwd`;
java -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcse-client-no-debug-gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
cd ..
