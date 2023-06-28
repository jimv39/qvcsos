#!/bin/bash
#
# Script to update the jar files to the test directory.
#
source ./version.sh
LOGBACK_VERSION=1.2.3
SLF4J_VERSION=1.7.16
SWING_LAYOUT_VERSION=1.0.3
POSTGRES_VERSION=42.2.26.jre7

if [ ! -e testDeploy ]
then
    mkdir testDeploy
fi
if [ ! -e testDeploy/logs ]
then
    mkdir testDeploy/logs
fi
if [ ! -e testDeploy/lib ]
then
    mkdir testDeploy/lib
fi
if [ ! -e testDeploy/qvcsBehaviorProperties ]
then
    mkdir testDeploy/qvcsBehaviorProperties
fi
cp ../testFiles/qvcsBehaviorProperties/*.properties testDeploy/qvcsBehaviorProperties
cp ../qvcse-admin/target/qvcse-admin-$QVCS_VERSION.jar testDeploy
cp ../qvcse-server/target/qvcse-server-$QVCS_VERSION.jar testDeploy
cp ../qvcse-gui/target/qvcse-gui-$QVCS_VERSION.jar testDeploy
cp ../qvcse-gui/target/qvcse-gui-$QVCS_VERSION.zip testDeploy/qvcse-client.zip
chmod +x testDeploy/qvcse-client.zip
cp ../qvcse-guilib/target/qvcse-guilib-$QVCS_VERSION.jar testDeploy/lib
cp ../qvcse-qvcslib/target/qvcse-qvcslib-$QVCS_VERSION.jar testDeploy/lib
cp ../qvcse-apache-diff/target/qvcse-apache-diff-$QVCS_VERSION.jar testDeploy/lib
cp ../qvcsosdb/target/qvcsosdb-$QVCS_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/org/swinglabs/swing-layout/$SWING_LAYOUT_VERSION/swing-layout-$SWING_LAYOUT_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/ch/qos/logback/logback-classic/$LOGBACK_VERSION/logback-classic-$LOGBACK_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/ch/qos/logback/logback-core/$LOGBACK_VERSION/logback-core-$LOGBACK_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/org/slf4j/slf4j-api/$SLF4J_VERSION/slf4j-api-$SLF4J_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/org/postgresql/postgresql/$POSTGRES_VERSION/postgresql-$POSTGRES_VERSION.jar testDeploy/lib
# cp *.properties testDeploy

