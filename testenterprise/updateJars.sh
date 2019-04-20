#!/bin/bash
#
# Script to update the jar files to the test directory.
#
source ./version.sh
DERBY_VERSION=10.14.2.0
LOGBACK_VERSION=1.2.3
SLF4J_VERSION=1.7.16
SWING_LAYOUT_VERSION=1.0.3

if [ ! -e testDeploy ]
then
    mkdir testDeploy
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
cp ../qvcse-server/target/qvcse-server-$QVCS_VERSION.jar testDeploy
cp ../qvcse-gui/target/qvcse-gui-$QVCS_VERSION.jar testDeploy
cp ../qvcse-guilib/target/qvcse-guilib-$QVCS_VERSION.jar testDeploy/lib
cp ../qvcse-qvcslib/target/qvcse-qvcslib-$QVCS_VERSION.jar testDeploy/lib
cp ../qvcse-apache-diff/target/qvcse-apache-diff-$QVCS_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/org/apache/derby/derby/$DERBY_VERSION/derby-$DERBY_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/org/swinglabs/swing-layout/$SWING_LAYOUT_VERSION/swing-layout-$SWING_LAYOUT_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/ch/qos/logback/logback-classic/$LOGBACK_VERSION/logback-classic-$LOGBACK_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/ch/qos/logback/logback-core/$LOGBACK_VERSION/logback-core-$LOGBACK_VERSION.jar testDeploy/lib
cp $HOME/.m2/repository/org/slf4j/slf4j-api/$SLF4J_VERSION/slf4j-api-$SLF4J_VERSION.jar testDeploy/lib
cp ../qvcse-admin/target/qvcse-admin-$QVCS_VERSION.jar testDeploy
cp *.properties testDeploy
