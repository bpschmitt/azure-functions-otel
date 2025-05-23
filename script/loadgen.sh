#!/bin/bash

i=1
while [ $i -le 100 ]; do
	curl -s http://localhost:7071/api/HttpExample?name=Brad
        ((i++))
	sleep 3
done

exit 0
