#!/bin/bash
# OVERVIEW:
# Runs the tools that are need to generate the parser from the wcps.g4 grammar
#
# USAGE:
#  cd $PETASCOPE_SOURCES/wcps2/parser && ./buildParser.sh
#

PATH_TO_ANTLR_TOOL="/usr/local/lib/antlr-4.1-complete.jar"

#Backup the visitor classes that define the actions to be taken on parsing
mkdir -p backupEvaluator
mv wcpsEvaluator.java backupEvaluator
mv ParserErrorHandler.java backupEvaluator

#Clean any existing files from the existent parser
rm *.class *.tokens *.java

#Build the parser
export antlr4="java -jar $PATH_TO_ANTLR_TOOL"
$antlr4 -package petascope.wcps2.parser wcps.g4
$antlr4 -package petascope.wcps2.parser -no-listener -visitor wcps.g4

#Compile the parser classes
javac *.java

#Put back the visitor classes into the folder
mv backupEvaluator/wcpsEvaluator.java .
mv backupEvaluator/ParserErrorHandler.java .

#Cleanup
rm -r backupEvaluator
