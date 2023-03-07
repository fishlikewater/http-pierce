@echo off
if not exist "jre\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk17 or later is better! & pause & EXIT
set "JAVA=jre\bin\java.exe"

set SERVER=http-pierce

set "JAVA_OPT= -Xms512m -Xmx512m -Xmn256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
echo %JAVA_OPT%

call "%JAVA%" %JAVA_OPT% -jar %SERVER%.jar
pause