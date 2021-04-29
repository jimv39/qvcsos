#!/bin/bash
#
# Shell script to start the client application with debug enabled.
#

# Get the current version...
source ./version.sh

cd testDeploy
QVCS_HOME=`pwd`;
#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006 -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$HOME/qvcse-client-gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$HOME/qvcse-client-gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -jar qvcse-gui-$QVCS_VERSION.jar
java -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$HOME/qvcse-client-gc.log -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
cd ..
