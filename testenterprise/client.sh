#!/bin/bash
#
# Shell script to start the client application with debug enabled.
#

# Get the current version...
source ./version.sh

if [ ! -e testDeploy/logs ]
then
    mkdir testDeploy/logs
fi

cd testDeploy
QVCS_HOME=`pwd`;
# java -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcsos-client-gc.log -Duser.name=JimVoris -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
# java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=6006 -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcsos-client-gc.log -Duser.name=JimVoris -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8006 -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcsos-client-gc.log -Duser.name=JimVoris -jar qvcse-gui-$QVCS_VERSION.jar
# java -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xloggc:$QVCS_HOME/logs/qvcsos-client-gc.log -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
cd ..
