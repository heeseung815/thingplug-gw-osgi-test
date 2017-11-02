#!/usr/bin/sh

app_home=`realpath "$(dirname "$0")"`
pid_file=$app_home/thingplug.pid

if [ -s $pid_file ]; then
    kill `cat $pid_file`
    exit 0
else
    echo "thingplug is not running"
    exit 1
fi

