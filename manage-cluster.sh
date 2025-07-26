#!/bin/bash

# Script to manage Flink cluster (start, stop, status)

# Set Flink installation path
export FLINK_HOME="/home/martavoi/flink-2.0.0"
export PATH="$FLINK_HOME/bin:$PATH"

# Function to show usage
show_usage() {
    echo "Usage: $0 {start|stop|restart|status|logs}"
    echo ""
    echo "Commands:"
    echo "  start    - Start the Flink cluster"
    echo "  stop     - Stop the Flink cluster"
    echo "  restart  - Restart the Flink cluster"
    echo "  status   - Check cluster status and list jobs"
    echo "  logs     - Show recent logs"
    echo ""
    echo "Using Flink installation: $FLINK_HOME"
}

# Function to start cluster
start_cluster() {
    echo "Starting Flink cluster..."
    echo "Using Flink installation: $FLINK_HOME"
    
    if [ ! -f "$FLINK_HOME/bin/start-cluster.sh" ]; then
        echo "❌ Error: Flink installation not found at $FLINK_HOME"
        echo "Please check the FLINK_HOME path in this script"
        exit 1
    fi
    
    $FLINK_HOME/bin/start-cluster.sh
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Flink cluster started successfully!"
        echo "Web UI: http://localhost:8081"
        sleep 2
        check_status
    else
        echo "❌ Failed to start Flink cluster"
    fi
}

# Function to stop cluster
stop_cluster() {
    echo "Stopping Flink cluster..."
    $FLINK_HOME/bin/stop-cluster.sh
    
    if [ $? -eq 0 ]; then
        echo "✅ Flink cluster stopped successfully!"
    else
        echo "❌ Failed to stop Flink cluster"
    fi
}

# Function to check status
check_status() {
    echo "=== Flink Cluster Status ==="
    flink list 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ Flink cluster is running"
        echo "Web UI: http://localhost:8081"
    else
        echo "❌ Flink cluster is not running or not accessible"
    fi
}

# Function to show logs
show_logs() {
    echo "=== Recent Flink Logs ==="
    LOG_DIR="$FLINK_HOME/log"
    
    if [ -d "$LOG_DIR" ]; then
        echo "Log directory: $LOG_DIR"
        echo ""
        echo "--- JobManager logs ---"
        if ls $LOG_DIR/flink-*-standalonesession-*.log 1> /dev/null 2>&1; then
            tail -20 $LOG_DIR/flink-*-standalonesession-*.log
        else
            echo "No JobManager logs found"
        fi
        
        echo ""
        echo "--- TaskManager logs ---"
        if ls $LOG_DIR/flink-*-taskexecutor-*.log 1> /dev/null 2>&1; then
            tail -20 $LOG_DIR/flink-*-taskexecutor-*.log
        else
            echo "No TaskManager logs found"
        fi
    else
        echo "Log directory not found: $LOG_DIR"
    fi
}

# Main script logic
case "$1" in
    start)
        start_cluster
        ;;
    stop)
        stop_cluster
        ;;
    restart)
        stop_cluster
        sleep 2
        start_cluster
        ;;
    status)
        check_status
        ;;
    logs)
        show_logs
        ;;
    "")
        show_usage
        ;;
    *)
        echo "Unknown command: $1"
        echo ""
        show_usage
        exit 1
        ;;
esac 