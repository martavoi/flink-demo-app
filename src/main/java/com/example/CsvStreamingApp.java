package com.example;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;

/**
 * Flink Streaming Application for CSV processing with 30-day sliding window aggregation.
 * 
 * This application:
 * 1. Reads CSV records from an input file
 * 2. Groups records by profile_id
 * 3. Computes average amount over 30-day sliding windows
 * 4. Outputs results to results.csv
 */
public class CsvStreamingApp {
    
    // Monitor a directory for new CSV files instead of a single file
    // Use absolute paths to avoid path resolution issues
    private static final String INPUT_DIR = System.getProperty("input.dir", 
        "/home/martavoi/repo/flink-test-app/input/");
    private static final String OUTPUT_FILE = System.getProperty("output.file", 
        "/home/martavoi/repo/flink-test-app/results.csv");
    // Development settings - use smaller windows for testing
    private static final int WINDOW_SIZE_MINUTES = 1; // 1 minute window for development
    private static final int SLIDE_SIZE_SECONDS = 10; // Slide every 10 seconds for frequent updates
    
    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Configure parallelism (can be overridden by cluster settings)
        env.setParallelism(1);
        
        // Log configuration
        System.out.println("=== Flink CSV Streaming Application Starting ===");
        System.out.println("Input directory: " + INPUT_DIR);
        System.out.println("Output file: " + OUTPUT_FILE);
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        
        // Show resolved absolute paths
        java.io.File inputDir = new java.io.File(INPUT_DIR);
        java.io.File outputFile = new java.io.File(OUTPUT_FILE);
        System.out.println("Resolved input directory: " + inputDir.getAbsolutePath());
        System.out.println("Resolved output file: " + outputFile.getAbsolutePath());
        System.out.println("Input directory exists: " + inputDir.exists());
        
        System.out.println("Window size: " + WINDOW_SIZE_MINUTES + " minute(s)");
        System.out.println("Slide interval: " + SLIDE_SIZE_SECONDS + " second(s)");
        System.out.println("Monitoring interval: 5 seconds");
        System.out.println("================================================");
        
        // Create file source for monitoring directory for new CSV files
        FileSource<String> source = FileSource
                .forRecordStreamFormat(new TextLineInputFormat(), new Path(INPUT_DIR))
                .monitorContinuously(Duration.ofSeconds(5)) // Check for new files every 5 seconds
                .build();
        
        // Enable checkpointing for proper file monitoring
        env.enableCheckpointing(5000); // Checkpoint every 5 seconds
        
        // Create data stream from source with proper watermarks for continuous processing
        DataStream<String> lines = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "csv-file-source"
        );
        
        // Print all lines first to debug
        lines.print("All lines");
        
        // Filter out header line and empty lines, then parse CSV records
        DataStream<CsvRecord> records = lines
                .filter(line -> {
                    boolean isValid = line != null && !line.trim().isEmpty() && !line.startsWith("profile_id");
                    System.out.println("Line: '" + line + "' - Valid: " + isValid);
                    return isValid;
                })
                .map(new MapFunction<String, CsvRecord>() {
                    @Override
                    public CsvRecord map(String line) throws Exception {
                        System.out.println("Parsing line: " + line);
                        CsvRecord record = CsvRecord.fromCsvLine(line);
                        System.out.println("Created record: " + record);
                        return record;
                    }
                });
        
        // Use count-based window to fire immediately when we have data
        DataStream<AverageResult> averages = records
                .keyBy(CsvRecord::getProfileId)
                .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(1))) // Every 1 minute 
                .apply(new WindowFunction<CsvRecord, AverageResult, String, TimeWindow>() {
                    @Override
                    public void apply(String profileId, TimeWindow window, 
                                    Iterable<CsvRecord> records, 
                                    Collector<AverageResult> out) throws Exception {
                        
                        double sum = 0.0;
                        int count = 0;
                        
                        System.out.println("Window fired for profile: " + profileId);
                        
                        Iterator<CsvRecord> iterator = records.iterator();
                        while (iterator.hasNext()) {
                            CsvRecord record = iterator.next();
                            sum += record.getAmount();
                            count++;
                        }
                        
                        if (count > 0) {
                            double average = sum / count;
                            LocalDateTime windowEnd = LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(window.getEnd()), 
                                    ZoneId.systemDefault()
                            );
                            
                            AverageResult result = new AverageResult(profileId, average, windowEnd);
                            System.out.println("Emitting result: " + result);
                            out.collect(result);
                        }
                    }
                });
        
        // Print results to console for debugging
        averages.print("Results");
        
        // Write results to CSV file using map transformation (Flink 2.0 compatible)
        averages
                .map(result -> {
                    System.out.println("Writing result to file: " + result);
                    // Write each result to file immediately
                    try (FileWriter writer = new FileWriter(OUTPUT_FILE, true)) {
                        // Write header if file is empty (simple check)
                        java.io.File file = new java.io.File(OUTPUT_FILE);
                        if (file.length() == 0) {
                            writer.write("profile_id,average_amount,window_end\n");
                            System.out.println("Wrote header to file");
                        }
                        writer.write(result.toCsvLine() + "\n");
                        writer.flush();
                        System.out.println("Successfully wrote to file: " + result.toCsvLine());
                    } catch (IOException e) {
                        System.err.println("Error writing to CSV file: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return result;
                })
                .name("csv-file-writer"); // Give the operation a name for monitoring
        
        // Execute the application
        env.execute("CSV Streaming Application with 30-day Average");
    }
    

} 