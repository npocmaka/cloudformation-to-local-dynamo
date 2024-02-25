#!/bin/bash

## ----
DEFINITION_FILE="./test.yaml"
OUTPUT_FILE="./dynamo.env"
## ----

export JAVA_HOME=/Users/me/jdk11/

mvn clean install

#sqlite cannot be included in the shaded jar so it is downloaded separately
mvn dependency:copy -Dartifact=com.almworks.sqlite4java:sqlite4java:1.0.392 -DoutputDirectory=. -U

cp ./target/setup-dynamo.jar .

$JAVA_HOME/bin/java -cp sqlite4java-1.0.392.jar:setup-dynamo.jar com.npocmaka.local.dynamo.Main $DEFINITION_FILE $OUTPUT_FILE
