@REM
@REM Copyright © 2024 zhangxiang (fishlikewater@126.com)
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off
chcp 65001
if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & pause & EXIT
set "JAVA=%JAVA_HOME%\bin\java.exe"

set SERVER=client

set "JAVA_OPT= -Xms256m -Xmx256m -Xmn128m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/java_heapdump.hprof"
echo %JAVA_OPT%

call "%JAVA%" %JAVA_OPT% -jar %SERVER%.jar
pause