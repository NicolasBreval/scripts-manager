#!/bin/bash

current_folder=$PWD

for lib in $current_folder/libs/*/
do
    mvn -f "${lib}pom.xml" clean install -DskipTests
    status=$?

    if [ $status -eq 0 ]
    then
        echo "Project in $lib successfully installed!!!" 
    else
        echo "Error building and installing project in $lib, please, check the log. Canceling..."
        break;
    fi
done