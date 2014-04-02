#!/bin/sh
#
# Shell script to start the server application, with debug enabled.
#

# Get the current version...
source ./version.sh

cd testDeploy
QVCS_HOME=`pwd`;
# Port 9889 is default client port; port 9890 is default admin port; port 9080 is web server (http) port.
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$HOME/qvcse-server-gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -jar qvcse-server-$QVCS_VERSION.jar "$QVCS_HOME" 9889 9890 9080
cd ..
