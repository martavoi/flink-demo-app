# Flink CSV Streaming Application

A Flink streaming application that **continuously monitors** a CSV file for updates, processes new records in real-time, groups them by profile_id, and computes average amounts over a 30-day sliding window.

## Features

- **Continuously monitors** `input/` directory for new CSV files (checks every 5 seconds)
- **Real-time processing** of new records as they are appended to the file
- Groups records by `profile_id` attribute
- Computes average `amount` over last 30 days using a sliding window (by `timestamp` attribute)
- Outputs results to `results.csv` in real-time
- Uses event-time processing for accurate temporal calculations
- **Runs forever** until manually stopped

## Quick Start

1. **Start Flink cluster:** `./manage-cluster.sh start`
2. **Submit application:** `./submit-to-cluster.sh`
3. **Add new CSV files:** `./add-csv-file.sh`
4. **Monitor:** http://localhost:8081
5. **Stop job:** `./check-cluster.sh` → get JOB_ID → `flink stop <JOB_ID>`

## Project Structure

```
├── pom.xml                                    # Maven build configuration
├── input/                                     # Directory for CSV files (monitored)
├── submit-to-cluster.sh                       # Submit to Flink cluster
├── check-cluster.sh                          # Check cluster status
├── manage-cluster.sh                         # Start/stop cluster
├── add-csv-file.sh                           # Add new CSV files for testing
├── src/main/java/com/example/
│   ├── CsvRecord.java                         # Input data model
│   ├── AverageResult.java                     # Output data model
│   └── CsvStreamingApp.java                   # Main Flink application
└── README.md                                  # This file
```

## Prerequisites

- Java 11 or higher
- Apache Maven 3.6 or higher
- Apache Flink 1.18.0 (included as dependency)

## Input Data Format

The application expects CSV files with the following format:

```csv
profile_id,amount,timestamp
user001,100.50,2024-01-01 10:00:00
user002,250.75,2024-01-01 11:00:00
```

### Fields:
- `profile_id`: String identifier for the user profile
- `amount`: Numeric value (double) representing the transaction amount
- `timestamp`: Date and time in format `yyyy-MM-dd HH:mm:ss`

## Output Format

Results are written to `results.csv` with the following format:

```csv
profile_id,average_amount,window_end
user001,123.45,2024-01-31 23:59:59
```

### Fields:
- `profile_id`: The user profile identifier
- `average_amount`: Computed average amount over the 30-day window (rounded to 2 decimal places)
- `window_end`: End timestamp of the sliding window

## Building the Application

1. Clone or download the project
2. Navigate to the project directory
3. Build using Maven:

```bash
mvn clean package
```

This will create a JAR file in the `target/` directory: `flink-test-app-1.0-SNAPSHOT.jar`

## Running the Application

### Prerequisites for Running
Make sure you have:
- Input file named `input.csv` in the project root directory
- Flink runtime available (either local execution or Flink cluster)

### How Continuous Monitoring Works
- The application monitors `input.csv` every 5 seconds for changes
- When you append new records to the file, they are automatically processed
- The application runs forever until you stop it with Ctrl+C
- Results are written to `results.csv` in real-time as new averages are calculated

### Option 1: Run with Maven (Local Development)

```bash
mvn exec:java -Dexec.mainClass="com.example.CsvStreamingApp"
```

### Option 2: Run the JAR file

```bash
# Run locally (embedded Flink runtime)
java -cp target/flink-test-app-1.0-SNAPSHOT.jar com.example.CsvStreamingApp
```

### Option 3: Submit to Flink Cluster

**For Local Flink Cluster:**

Use the provided script:
```bash
./submit-to-cluster.sh
```

Or manually:
```bash
# Build the application
mvn clean package

# Submit to running Flink cluster
flink run target/flink-test-app-1.0-SNAPSHOT.jar
```

**With custom file paths:**
```bash
flink run -D input.file=/path/to/your/input.csv -D output.file=/path/to/results.csv target/flink-test-app-1.0-SNAPSHOT.jar
```

## Application Configuration

The application uses the following default settings (can be modified in `CsvStreamingApp.java`):

- **Input file**: `input.csv`
- **Output file**: `results.csv`
- **Window size**: 30 days
- **Slide interval**: 1 day (windows slide daily)
- **Parallelism**: 1 (for local testing)
- **Watermark**: 1-minute bounded out-of-orderness

## Window Behavior

The application uses a 30-day sliding window that moves forward by 1 day:
- Window 1: Days 1-30
- Window 2: Days 2-31
- Window 3: Days 3-32
- And so on...

This means each profile will generate multiple average calculations as new data arrives and windows slide forward.

## Sample Data

The included `input.csv` contains sample data with:
- 3 different user profiles (`user001`, `user002`, `user003`)
- Transactions spanning approximately 2 months
- Various amounts to demonstrate averaging calculations

## Testing Continuous Monitoring

To test the continuous monitoring feature:

1. Start the application: `./run.sh`
2. In another terminal, append new records to the input file:
   ```bash
   echo "user001,500.00,2024-03-10 14:30:00" >> input.csv
   echo "user002,750.25,2024-03-10 15:45:00" >> input.csv
   ```
3. Watch the console output - you should see new results being processed
4. Check `results.csv` for the updated averages

The application will automatically detect the file changes and process the new records within 5 seconds.

## Cluster Deployment

### Prerequisites for Cluster Deployment

1. **Flink Cluster Setup:**
   ```bash
   # Start local Flink cluster (using provided script)
   ./manage-cluster.sh start
   
   # Or manually
   /home/martavoi/flink-2.0.0/bin/start-cluster.sh
   
   # Verify cluster is running
   ./check-cluster.sh
   ```

2. **File Accessibility:**
   - Ensure `input.csv` is accessible from all Flink nodes
   - For local cluster: place files in a shared directory
   - For distributed cluster: use HDFS, S3, or shared network storage

3. **Environment Variables (already configured in scripts):**
   ```bash
   export FLINK_HOME=/home/martavoi/flink-2.0.0
   export PATH=$FLINK_HOME/bin:$PATH
   ```

### Cluster Deployment Steps

1. **Build for cluster deployment:**
   ```bash
   mvn clean package
   ```

2. **Submit to cluster:**
   ```bash
   ./submit-to-cluster.sh
   ```

3. **Monitor the job:**
   - Access Flink Web UI: http://localhost:8081
   - Check job status, metrics, and logs
   - Monitor throughput and latency

### Cluster Configuration Options

**Custom file paths:**
```bash
flink run \
  -D input.file=/shared/data/transactions.csv \
  -D output.file=/shared/results/averages.csv \
  target/flink-test-app-1.0-SNAPSHOT.jar
```

**Parallelism settings:**
```bash
flink run -p 4 target/flink-test-app-1.0-SNAPSHOT.jar
```

**Job name:**
```bash
flink run --job-name="CSV-Streaming-App" target/flink-test-app-1.0-SNAPSHOT.jar
```

### Stopping the Job

```bash
# List running jobs
flink list

# Stop a job (replace JOB_ID with actual ID)
flink stop JOB_ID

# Cancel a job (for immediate termination)
flink cancel JOB_ID
```

### Cluster Troubleshooting

1. **Job fails to start:**
   - Check if input file exists and is readable
   - Verify file paths are accessible from all task managers
   - Check Flink logs in `$FLINK_HOME/log/`

2. **No data processing:**
   - Ensure input file has new data being appended
   - Check file monitoring is working (5-second intervals)
   - Verify timestamp format in CSV data

3. **Performance issues:**
   - Increase parallelism: `flink run -p <number> ...`
   - Monitor resource usage in Flink Web UI
   - Check for backpressure in the pipeline

## Flink Web UI Guide

Access the Flink Web UI at: **http://localhost:8081**

### 🎯 **Key Tabs and Features:**

#### **1. Overview Tab**
- **Cluster status**: JobManager and TaskManager health
- **Resource usage**: CPU, memory, network metrics
- **Running jobs**: See all active jobs and their status

#### **2. Jobs Tab** 
- **Job details**: Click on your job to see detailed information
- **Job graph**: Visual representation of your data pipeline
- **Metrics**: Throughput, latency, records processed

#### **3. Logs Tab**
- **JobManager logs**: Cluster-level logs and coordination
- **TaskManager logs**: Task execution logs and errors
- **Search functionality**: Filter logs by level (INFO, WARN, ERROR)

#### **4. Checkpoints Tab** (Inside Job Details)
- **Checkpoint status**: Success/failure of state snapshots
- **Checkpoint size**: How much state is being saved
- **Checkpoint duration**: How long checkpoints take
- **Recovery**: Shows last successful checkpoint for failure recovery

#### **5. Configuration Tab**
- **Job configuration**: All parameters and settings
- **Flink configuration**: Cluster-wide settings

### 🚀 **Development Tips:**

**For your 1-minute window application:**
1. **Check "Records Sent"** in job metrics - should increase every 10 seconds
2. **Monitor "Watermarks"** to see event-time progress
3. **Watch "Checkpoints"** - should complete successfully every few seconds
4. **Check "Backpressure"** - should be low/green for healthy processing

**Quick Actions:**
- **Cancel job**: Red "Cancel" button in job overview
- **Take savepoint**: Manual backup of job state
- **Restart job**: From savepoint if needed

## Monitoring and Debugging

### Console Output
The application prints results to the console in addition to writing to the CSV file.

Example console output:
```
AverageResult{profileId='user001', averageAmount=123.45, windowEnd=2024-03-05T14:30:00}
```

### Log Locations
- **Application logs**: Visible in Flink Web UI → Jobs → Your Job → Logs
- **Cluster logs**: `$FLINK_HOME/log/` directory
- **Job Manager**: `flink-*-standalonesession-*.log`
- **Task Manager**: `flink-*-taskexecutor-*.log`

## Troubleshooting

### Common Issues:

1. **FileNotFoundException**: Ensure `input.csv` exists in the project root
2. **ClassNotFoundException**: Make sure all Flink dependencies are in the classpath
3. **TimestampException**: Verify timestamp format in CSV matches `yyyy-MM-dd HH:mm:ss`
4. **NumberFormatException**: Check that amount fields contain valid numeric values

### Logging:
Check the Flink logs for detailed error messages and processing statistics.

## Extending the Application

To modify the application for different requirements:

1. **Change window size**: Modify `WINDOW_SIZE_DAYS` in `CsvStreamingApp.java`
2. **Different slide interval**: Modify `SLIDE_SIZE_DAYS`
3. **Additional fields**: Extend `CsvRecord` and `AverageResult` classes
4. **Different aggregation**: Replace the window function with custom logic
5. **Multiple input files**: Modify the file source configuration

## Dependencies

Key Flink dependencies included:
- `flink-streaming-java`: Core streaming API
- `flink-clients`: Client libraries
- `flink-connector-files`: File system connector
- `flink-csv`: CSV format support
- `flink-table-api-java-bridge`: Table API integration 