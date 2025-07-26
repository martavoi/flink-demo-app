#!/bin/bash

# Script to build and submit Flink CSV Streaming Application to local cluster

# Set Flink installation path
export FLINK_HOME="/home/martavoi/flink-2.0.0"
export PATH="$FLINK_HOME/bin:$PATH"

echo "Building Flink CSV Streaming Application for cluster deployment..."
echo "Using Flink installation: $FLINK_HOME"

# Clean and build the project
mvn clean package -q

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo ""
    
    # Check if Flink cluster is running
    echo "Checking if Flink cluster is running..."
    if command -v flink &> /dev/null; then
        # Try to get cluster info
        flink list 2>/dev/null
        if [ $? -eq 0 ]; then
            echo "Flink cluster is running!"
        else
            echo "Warning: Cannot connect to Flink cluster. Make sure:"
            echo "1. Flink cluster is started (run: start-cluster.sh)"
            echo "2. Flink is in your PATH"
            echo "3. FLINK_HOME is set correctly"
            echo ""
        fi
    else
        echo "Warning: 'flink' command not found in PATH"
        echo "Make sure Flink is properly installed and configured"
        echo ""
    fi
    
    # Get the JAR file
    JAR_FILE="target/flink-test-app-1.0-SNAPSHOT.jar"
    
    if [ -f "$JAR_FILE" ]; then
        echo "Submitting job to Flink cluster..."
        echo "JAR file: $JAR_FILE"
        echo "Main class: com.example.CsvStreamingApp"
        echo ""
        echo "Make sure input.csv is accessible from the Flink cluster!"
        echo "Job will run continuously - use Flink Web UI to monitor and stop"
        echo "----------------------------------------"
        
        # Submit the job to the cluster with absolute directory path (detached mode)
        CURRENT_DIR=$(pwd)
        echo "Setting file paths:"
        echo "  Input directory: $CURRENT_DIR/input/"
        echo "  Output file: $CURRENT_DIR/results.csv"
        echo ""
        
        flink run \
            --detached \
            -Dinput.dir="$CURRENT_DIR/input/" \
            -Doutput.file="$CURRENT_DIR/results.csv" \
            "$JAR_FILE"
        
        if [ $? -eq 0 ]; then
            echo ""
            echo "Job submitted successfully in background!"
            echo "Access Flink Web UI at: http://localhost:8081"
            echo "Use './check-cluster.sh' to see job status"
            echo "Monitor your job and check logs in the Web UI"
        else
            echo ""
            echo "Failed to submit job to cluster"
            echo "Check the error messages above"
        fi
    else
        echo "Error: JAR file not found at $JAR_FILE"
        exit 1
    fi
else
    echo "Build failed! Please check the error messages above."
    exit 1
fi 