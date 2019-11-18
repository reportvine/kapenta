@echo off

rem Run the kapenta report cli
SETLOCAL enabledelayedexpansion
TITLE Kapenta v0.3.0

set LIB_CLASSPATH=%~dp0\..\lib\*
java -cp "%CLASSPATH%;%LIB_CLASSPATH%;." com.creditdatamw.labs.kapenta.Main %*
