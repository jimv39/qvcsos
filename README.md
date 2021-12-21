qvcsos
======

QVCS-Enterprise open source project is a Java client/server source control tool for small (possibly remote) teams.

## Note that the 4.1.1 branch is a BIG change from earlier releases. These changes are not yet fully documented.
A quick summary of the changes: all revision data is now stored directly in a postgres database.

## Getting started

First, some assumptions:

1. You have already installed a suitable JDK. The code base requires JDK 8 at this point.
2. You have already installed Apache maven.
3. You have some fluency with maven, and its various plugins.

Given you're comfortable with the tooling...

#### Step 1:
You first need to build the qvcse-build-tools project located at qvcsos/qvcse-build-tools:

```
cd qvcse-build-tools
mvn clean install
```

This will install the build tools (basically a shared checkstyle configuration file) into your local maven repository.

#### Step 2:
Now you will want to build the master project:

```
cd qvcsos
mvn clean install -DskipTests
```

Notice that we're skipping the tests in the above command. This is to make the build faster. You can leave off the ```-DskipTests``` if you wish, but it will make the build take quite a bit longer.

#### Step 3:
You need to install Postgresql. Do a web search to find an installation strategy that works best for your environment. Once installed, you need to execute the postgres_qvcsos410_prod_script.sql SQL script 
located in the testenterprise directory. Typically, you would run this script as the postgres user. You may want to make edits to the script to supply a different password, etc.

#### Step 4:
Now you can test that the build produced a useful set of jar files. To do that:

```
cd testenterprise
chmod +x *sh
./updateJars.sh
```
Edit the qvcs.postgresql.connection.properties file located in the testenterprise/testDeploy/qvcsBehaviorProperties directory to update the database connection properties so that they point to the
database that you created in Step 3.
```
Start the server:
```
./server.sh
```

If the server starts, open a web browser, and point it to localhost:9080. Choose the Tutorials/Getting Started page for some guidance. Open a new shell window:

```
cd testenterprise
./admin.sh
```

Set up the server definition, and login to the server as ADMIN/ADMIN.

### Contributing
At this time, you'll need to fork the repo, make your changes, and then submit a pull request, as I need to review anything going in to the repo at this point. You'll need to include the standard
Apache header on anything you add. If you modify existing code, and want to add your name to the copyright, that's fine.

Note that the build uses the checkstyle plugin to make sure the code passes the checkstyle constraints that I've adopted. I have things set up so that the checkstyle plugin will fail the build for
code that doesn't pass.

### Work In Progress
At this point, I'm going through the code to clean it up, improve readability and maintainability, etc. Along the way, there is a lot of refactoring going on to improve the code structure and
organization. The 4.1.1 branch (a work in progress) has a lot of changes.
