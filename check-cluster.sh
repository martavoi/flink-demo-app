#!/bin/bash

# Script to check Flink cluster status and manage jobs

# Set Flink installation path
export FLINK_HOME="/home/martavoi/flink-2.0.0"
export PATH="$FLINK_HOME/bin:$PATH"

echo "=== Flink Cluster Status Check ==="
echo "Using Flink installation: $FLINK_HOME"
echo ""

# Check if flink command is available
if ! command -v flink &> /dev/null; then
    echo "❌ Error: 'flink' command not found in PATH"
    echo "Make sure Flink is properly installed and FLINK_HOME is set"
    exit 1
fi

echo "✅ Flink command found"

# Check cluster connectivity
echo ""
echo "Checking cluster connectivity..."
flink list &>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ Flink cluster is running and accessible"
else
    echo "❌ Cannot connect to Flink cluster"
    echo "Make sure the cluster is started with: $FLINK_HOME/bin/start-cluster.sh"
    echo ""
    echo "Or check if the cluster is running:"
    echo "  - JobManager Web UI: http://localhost:8081"
    echo "  - Check logs in $FLINK_HOME/log/"
    exit 1
fi

# List running jobs
echo ""
echo "=== Running Jobs ==="
flink list

echo ""
echo "=== Cluster Information ==="
echo "Flink Web UI: http://localhost:8081"
echo ""

# Show available commands
echo "=== Useful Commands ==="
echo "Submit job:     ./submit-to-cluster.sh"
echo "List jobs:      flink list"
echo "Stop job:       flink stop <JOB_ID>"
echo "Cancel job:     flink cancel <JOB_ID>"
echo "Cluster info:   flink list"
echo "" 