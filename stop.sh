#!/bin/bash

portS=$(cat dynamo.env | grep "PORT")
pidS=$(cat dynamo.env | grep "PID")

IFS=':'

read -ra portArr <<< "$portS"
read -ra pidArr <<< "$pidS"

portS=${portArr[1]}
pidS=${pidArr[1]}

port=`echo ${portS} | rev | cut -c 2- | rev`
pid=`echo ${pidS} | rev | cut -c 2- | rev`

port=$(echo $port | tr -d ' ')
pid=$(echo $pid | tr -d ' ')

process=`lsof -i tcp:"$port"| tail -n 1`

IFS=' '

read -ra procChunks <<< "$process"


if [[ ${procChunks[1]} == ${pid} ]] && [[ ${procChunks[0]} == java ]]; then
  kill -9 "$pid"
fi


