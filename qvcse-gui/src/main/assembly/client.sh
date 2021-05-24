#!/bin/bash
#
# Shell script to start the client application with debug enabled.
#

# Get the current version...
source ./version.sh

if [ ! -e logs ]
then
    mkdir logs
fi

QVCS_HOME=`pwd`;
java -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xlog:gc:$QVCS_HOME/logs/qvcsos-client-gc.log -Duser.name=ChangeToYourQvcsosUserName -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
# java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006 -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xlog:gc:$QVCS_HOME/logs/qvcsos-client-gc.log -Duser.name=ChangeToYourQvcsosUserName -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
# java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xlog:gc:$QVCS_HOME/logs/qvcsos-client-gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Duser.name=ChangeToYourQvcsosUserName -jar qvcse-gui-$QVCS_VERSION.jar
# java -Dapple.laf.useScreenMenuBar=true -Xmx512m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xlog:gc:$QVCS_HOME/logs/qvcsos-client-gc.log -Duser.name=ChangeToYourQvcsosUserName -jar qvcse-gui-$QVCS_VERSION.jar "$QVCS_HOME"
