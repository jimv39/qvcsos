#!/bin/sh
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
: ${SOURCE_HOME:="$SCRIPT_DIR"}
# Change from one version string in the pom.xml files to another version string
# Usage -- $1 -- original version string $2 -- new version string.

sed -ie "s#$1#$2#" pom.xml
sed -ie "s#$1#$2#" copy.xml

cd $SOURCE_HOME/qvcse-admin
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcse-apache-diff
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcse-gui
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcse-guilib
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcse-qvcslib
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcse-server
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcse-autoupdate
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcsosdb
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcse-coverage
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/qvcsos-test-utils
sed -ie "s#$1#$2#" pom.xml

cd $SOURCE_HOME/testenterprise
sed -ie "s#$1#$2#" version.sh

cd $SOURCE_HOME/qvcse-gui/src/main/java/com/qumasoft/guitools/qwin
sed -ie "s#$1#$2#" QWinFrame.java
sed -ie "s#$1#$2#" QWinFrame.form

cd $SOURCE_HOME/qvcse-qvcslib/src/main/java/com/qumasoft/qvcslib
sed -ie "s#$1#$2#" QVCSConstants.java

cd $SOURCE_HOME/qvcse-server/src/main/resources/ServerWebSite
sed -ie "s#$1#$2#" index.html

cd $SOURCE_HOME/qvcse-server/src/test/java/com/qumasoft
sed -ie "s#$1#$2#" TestHelper.java

cd $SOURCE_HOME
