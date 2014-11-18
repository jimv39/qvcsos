qvcsos
======

QVCS-Enterprise open source project. A Java client/server source control tool for small (possibly remote) teams. At this writing, most of the product related documentation
is still hosted at http://www.qumasoft.com/ServerWebSite/index.html. The code here *is* different than the code in the product described on that site, but the functionality is the same,
except this version of the code does not have support for the Microsoft SCC API.

## Getting started

First, some assumptions:

1. You have already installed a suitable JDK. The code base requires JDK 8 at this point.
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

####Step 3:
Now you can test that the build produced a useful set of jar files. To do that:

```
cd testenterprise
./updateJars.sh
./server.sh
```

and then in a separate command window:

```
cd testenterprise
./admin.sh
```

Set up the server definition, and login to the server as ADMIN/ADMIN. There is some still accurate guidance on these setup steps [here](http://www.qumasoft.com/ServerWebSite/getstarted/readme1.html)

###Contributing
At this time, you'll need to fork the repo, make your changes, and then submit a pull request, as I need to review anything going in to the repo at this point. You'll need to include the standard
Apache header on anything you add. If you modify existing code, and want to add your name to the copyright, that's fine.

Note that the build uses the checkstyle plugin to make sure the code passes the checkstyle constraints that I've adopted. I have things set up so that the checkstyle plugin will fail the build for
code that doesn't pass.

###Work In Progress
At this point, I'm going through the code to clean it up, improve readability and maintainability, etc. Along the way, there is a lot of refactoring going on to improve the code structure and
organization. All this is being done on the *develop* branch. If you want to look at stable code, the master branch will not be changing very often.
