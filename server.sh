#!/bin/bash

# Define the file path
FILE="keys/server_public.key"

# Check if the file exists
if [ ! -f "$FILE" ]; then
    mkdir -p "keys"  
    touch "$FILE"     
    echo "$FILE created."
fi

rmiregistry &
sleep 2
java AuctionServer