#!/usr/bin/sh

app_home=`realpath "$(dirname "$0")"`
pid_file=$app_home/thingplug.pid

if [ -s $pid_file ]; then
  echo "thingplug is already running, pid=`cat $pid_file`"
  exit 1
fi

nohup $app_home/thingplug >/dev/null 2>&1 &

