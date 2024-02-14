#!/bin/sh
#
# DON'T EDIT THIS!
#
# CodeCrafters uses this file to test your code. Don't make any changes here!
#
# DON'T EDIT THIS!
set -e
mvn -B -f /Users/az/Documents/codecrafters-git-java/ --quiet package -Ddir=/tmp/codecrafters-git-target
exec java -jar /tmp/codecrafters-git-target/java_git.jar "$@"
