#!/bin/bash
java -Xmx1964m -jar /app/runner.jar "$@" &
while true; do
    sleep 3600
done
