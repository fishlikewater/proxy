@echo off
chcp 65001
if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & pause & EXIT
set "JAVA=%JAVA_HOME%\bin\java.exe"

set SERVER=client

set "JAVA_OPT= -Xms256m -Xmx256m -Xmn128m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/java_heapdump.hprof"
echo %JAVA_OPT%

call "%JAVA%" %JAVA_OPT% -jar %SERVER%.jar
pause