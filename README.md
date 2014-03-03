qvcsos
======

QVCS-Enterprise open source project. Java client/server source control for small (possibly remote) teams.

## Getting started

First, some assumptions:

1. You have already installed a suitable JDK. The code base requires JDK 7 at this point.
2. You have already installed Apache maven.
3. You have some fluency with maven, and its various plugins.

Given you're comfortable with the tooling...

####Step 1:
You first need to build the qvcse-build-tools project located at qvcsos/qvcse-build-tools:
```
cd qvcse-build-tools
mvn clean install
```

This will install the build tools (basically a shared checkstyle configuration file) into your local maven repository.

####Step 2:
Now you will want to build the master project:
```
cd qvcsos
mvn clean install -DskipTests
```
Notice that we're skipping the tests in the above command. This is to make the build faster. You can leave off the ```-DskipTests``` if you wish, but it will make the build take quite a bit longer.

###Contributing
At this time, you'll need to fork the repo, make your changes, and then submit a pull request, as I need to review anything going in to the repo at this point. You'll need to include the standard
Apache header on anything you add. If you modify existing code, and want to add your name to the copyright, that's fine.

Note that the build uses the checkstyle plugin to make sure the code passes the checkstyle constraints that I've adopted. I have things set up so that the checkstyle plugin will fail the build for
code that doesn't pass. I won't even look at code that has checkstyle errors.
