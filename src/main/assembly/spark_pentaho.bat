@echo off
rem Pass arguments to the spark pentaho report cli
java -cp "%CLASSPATH%;../lib;../lib/*;." com.creditdatamw.labs.sparkpentaho.Application %*
