#!/bin/bash
#
# Shell script to start the server application, with debug enabled.
#

# Get the current version...
source ./version.sh

cd testDeploy
QVCS_HOME=`pwd`;
# Port 9889 is default client port; port 9890 is default admin port; port 9080 is web server (http) port.
# java -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcsos-server-gc.log -jar qvcse-server-$QVCS_VERSION.jar "$QVCS_HOME" 29889 29890 29080 derby
# java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcsos-server-gc.log -jar qvcse-server-$QVCS_VERSION.jar "$QVCS_HOME" 29889 29890 29080 derby
# java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=6005 -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcsos-server-gc.log -jar qvcse-server-$QVCS_VERSION.jar "$QVCS_HOME" 29889 29890 29080 derby
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8005 -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcsos-server-gc.log -jar qvcse-server-$QVCS_VERSION.jar "$QVCS_HOME" 29889 29890 29080 postgresql
cd ..

