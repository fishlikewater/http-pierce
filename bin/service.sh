#!/bin/bash
#项目jar名称
APP_NAME=http-pierce

#JDK指定
JAVA_HOME=./jre

#关闭debug模式则设置为空
APP_DEBUGE=
#APP_DEBUGE="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=18099"

#JVM参数  -Xms程序启动时占用内存大小 -Xmx程序运行期间最大可占用的内存大小 -Xss设定每个线程的堆栈大小   -Xmn 年轻代大小
JVM_OPTS="-Dfile.encoding=utf-8 -Xms1024m -Xmx1024m  -Xss256k -XX:MaxPermSize=512m -XX:NewRatio=4 -XX:+UseG1GC -XX:ParallelGCThreads=8 -XX:MaxGCPauseMillis=500"
JVM_OPTS="${JVM_OPTS} -XX:-HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/java_heapdump.hprof"
JAVA_OPT="${JVM_OPTS} -XX:-UseLargePages"


# 如果不指定jdk则使用默认
if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    javaexe="$JAVA_HOME/bin/java"
elif type -p java > /dev/null 2>&1; then
    javaexe=$(type -p java)
elif [[ -x "/usr/bin/java" ]];  then
    javaexe="/usr/bin/java"
else
    echo "Unable to find Java"
    exit 1
fi

#获取当前工作空间
SOURCE="$0"
while [ -h "$SOURCE"  ]; do
    DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd  )"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /*  ]] && SOURCE="$DIR/$SOURCE"
done
WORKING_DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

#项目目录
APP_HOME="$(dirname "$WORKING_DIR")"

#项目配置文件路径
#APP_CONF="$APP_HOME/resources/application.properties"

JAR_FILE=$APP_NAME.jar

pid=00000
#校验进程
APP_PID=$APP_HOME/$APP_NAME.pid

#APP_CONF="--spring.config.location=file:$APP_CONF "

start(){
 checkpid
 if [ $? -eq 0 ]; then
    echo JDK路径: $JAVA_HOME
    echo 项目目录: $APP_HOME
    echo 项目名称: $APP_NAME
    #echo 配置文件: $APP_CONF
    echo JVM参数: $JAVA_OPT
    if  [ ! -n "$APP_DEBUGE" ] ;then
        echo "关闭debug模式!"
    else
        echo "开启debug模式!"
    fi

    /bin/sh -c  "$javaexe -jar $APP_DEBUGE $JAVA_OPT $JAR_FILE > /dev/null 2>&1 & echo \$!" > "$APP_PID"

    sleep 10s
    checkpid
    if [ $? -eq 0 ]; then
        echo "---------------------------------"
        echo "启动失败"
        echo "---------------------------------"
    else
        echo "---------------------------------"
        echo "启动完成"
        echo "---------------------------------"
    fi
  else
      echo "$APP_NAME is runing PID: $pid"
  fi

}

status(){
   checkpid
   if [ $? -eq 0 ]; then
     echo  "$APP_NAME not runing"
   else
     echo "$APP_NAME runing PID: $pid"
   fi
}

checkpid(){
   if [[ -f "$APP_PID" ]]; then
        pid=$(cat "$APP_PID")
        checkPidNum
        if [ $? -eq 0 ];then
		return 0;
        else
                return 1;
        fi
   else
        checkPidNum
        if [ $? -eq 0 ];then
		echo "${?}"
		return 0;
        else
                return 1;
        fi
   fi
}

checkPidNum(){
   	PIDNUM=`ps -ef|grep $JAR_FILE|grep -v grep|awk '{print $2}'`
	if [ -z "${PIDNUM}" ]; then
		echo "pid is null."
		return 0
	else
		echo "${APP_NAME} running. pid=${pid}"
		return 1
	fi
}
stop(){
    checkpid
    if [ $? -eq 0 ]; then
      echo "$APP_NAME not runing"
    else
      echo "$APP_NAME stop..."
      kill -15 $pid
    fi
}
restart(){
    stop
    sleep 1s
    start
}

case $1 in
          start) start;;
          stop)  stop;;
          restart)  restart;;
          status)  status;;
              *)  echo "require start|stop|restart|status"  ;;
esac