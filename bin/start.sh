#!/bin/bash
SERVER_NAME='proxy'
# jar名称
JAR_NAME='proxy.jar'
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/resources
# SERVER_PORT=`sed '/server.port/!d;s/.*=//' config/application.properties | tr -d '\r'`
# 获取应用的端口号
#SERVER_PORT=`sed -nr '/port: [0-9]+/ s/.*port: +([0-9]+).*/\1/p' $CONF_DIR/application.properties`
PIDS=`ps -ef | grep java | grep $SERVER_NAME |awk '{print $2}'`
if [ "$1" = "status" ]; then
  if [ -n "$PIDS" ]; then
    echo "The $SERVER_NAME is running...!"
    echo "PID: $PIDS"
    exit 0
  else
    echo "The $SERVER_NAME is stopped"
    exit 0
  fi
fi
if [ -n "$PIDS" ]; then
  echo "ERROR: The $SERVER_NAME already started!"
  echo "PID: $PIDS"
  exit 1
fi
if [ -n "$SERVER_PORT" ]; then
  SERVER_PORT_COUNT=`netstat -tln | grep $SERVER_PORT | wc -l`
  if [ $SERVER_PORT_COUNT -gt 0 ]; then
    echo "ERROR: The $SERVER_NAME port $SERVER_PORT already used!"
    exit 1
  fi
fi
LOGS_DIR=$DEPLOY_DIR/logs
if [ ! -d $LOGS_DIR ]; then
  mkdir $LOGS_DIR
fi
STDOUT_FILE=$LOGS_DIR/stdout.log
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
JAVA_DEBUG_OPTS=""
if [ "$1" = "debug" ]; then
  JAVA_DEBUG_OPTS=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n "
fi
JAVA_JMX_OPTS=""
if [ "$1" = "jmx" ]; then
  JAVA_JMX_OPTS=" -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false "
fi
JAVA_MEM_OPTS=""
BITS=`java -version 2>&1 | grep -i 64-bit`
if [ -n "$BITS" ]; then
  JAVA_MEM_OPTS=" -server -Xms256m -Xmx256m -Xmn120m -Xss256k -XX:MetaspaceSize=128M -XX:SurvivorRatio=3 -XX:+UseG1GC -Xloggc:gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps"
else
  JAVA_MEM_OPTS=" -server -Xms256m -Xmx256m -XX:SurvivorRatio=2 -XX:+UseParallelGC -XX:MetaspaceSize=128M "
fi
CONFIG_FILES=" -Dspring.config.location=$CONF_DIR/ "
echo -e "Starting the $SERVER_NAME ..."
echo java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS $CONFIG_FILES -jar $DEPLOY_DIR/bin/$JAR_NAME > $STDOUT_FILE 2>&1 &
nohup java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS $CONFIG_FILES -jar $DEPLOY_DIR/bin/$JAR_NAME > $STDOUT_FILE 2>&1 &
echo "OK!"
PIDS=`ps -ef | grep java | grep $SERVER_NAME | awk '{print $2}'`
echo "PID: $PIDS"
echo "STDOUT: $STDOUT_FILE"
