@echo off

rem Run the spark pentaho report cli
SETLOCAL enabledelayedexpansion
TITLE Spark Pentaho

LIB_CLASSPATH=%~dp0\..\lib\*
java -cp "%CLASSPATH%;%LIB_CLASSPATH%;." com.creditdatamw.labs.sparkpentaho.Application %*
