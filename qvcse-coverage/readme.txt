How to use this qvcsos-coverage project:

This project exists for the sole purpose of capturing code-coverage data
from the unit tests that have been written for the qvcsos projects.

Step 1:
Use ant to copy the files into this project using this command line:

ant -f buildlinux.xml

This will copy java source and test code into this project.

Step 2:
Use the following mvn command line to execute the tests:

mvn clean verify

Step 3:
After the tests have completed, you can view the code coverage data by pointing your
web browser to:

target/site/jacoco/index.html


