@echo off
chcp 65001
if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & pause & EXIT
set "JAVA=%JAVA_HOME%\bin\java.exe"

set SERVER=server

set "JAVA_OPT= -Dfile.encoding=utf-8 -Xms256m -Xmx256m  -Xss256k -XX:MaxPermSize=128m -XX:NewRatio=4 -XX:+UseG1GC -XX:ParallelGCThreads=8 -XX:MaxGCPauseMillis=500 -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/java_heapdump.hprof -XX:-UseLargePages"
echo %JAVA_OPT%

call "%JAVA%" %JAVA_OPT% -jar %SERVER%.jar
pause