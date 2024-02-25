@echo off

:::::

set "DEFINITION_FILE=E:\eclipse-projects\test-yaml\test.yaml"
set "OUTPUT_FILE=.\dynamo.env"
set "JAVA_HOME=E:\JDK11"

:::::::::::::

call mvn clean install
REM --- sqlite cannot be included in the shaded jar so it is downloaded separately
call mvn dependency:copy -Dartifact=com.almworks.sqlite4java:sqlite4java:1.0.392 -DoutputDirectory=. -U

copy ".\target\setup-dynamo.jar" .

%JAVA_HOME%/bin/java -cp "sqlite4java-1.0.392.jar;setup-dynamo.jar" com.npocmaka.local.dynamo.Main "%DEFINITION_FILE%" "%OUTPUT_FILE%"