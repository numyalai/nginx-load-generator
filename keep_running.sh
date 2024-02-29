#!/bin/bash
java -Xmx4g -jar /app/runner.jar "$@" &
while true; do
    sleep 3600
done
