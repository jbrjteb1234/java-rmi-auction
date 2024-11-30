#!/bin/bash

# Ensure the RMI registry is running
echo "Starting RMI registry..."
rmiregistry &
sleep 2 # Allow the registry to initialize

# Start the replicas
echo "Starting replicas..."
java Replica 1 &
replica1_pid=$! # Save process ID to allow cleanup later

java Replica 2 &
replica2_pid=$!

java Replica 3 &
replica3_pid=$!

sleep 2 # Allow replicas to initialize

# Start the Front-End
echo "Starting the Front-End..."
java Frontend &
frontend_pid=$!

# Wait a moment to ensure all processes are running
sleep 1

echo "All services started successfully."
echo "Replica1 PID: $replica1_pid"
echo "Replica2 PID: $replica2_pid"
echo "Replica3 PID: $replica3_pid"
echo "FrontEnd PID: $frontend_pid"

# Trap SIGINT and SIGTERM to clean up
trap "echo 'Shutting down services...'; kill $replica1_pid $replica2_pid $replica3_pid $frontend_pid; exit" SIGINT SIGTERM

# Keep the script running to monitor processes
while true; do
    sleep 5
done
