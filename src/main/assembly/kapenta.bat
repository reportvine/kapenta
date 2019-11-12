@echo off

rem Run the spark pentaho report cli
SETLOCAL enabledelayedexpansion
TITLE Spark Pentaho Report v0.3.0

set LIB_CLASSPATH=%~dp0\..\lib\*
java -cp "%CLASSPATH%;%LIB_CLASSPATH%;." com.creditdatamw.labs.kapenta.Application %*
