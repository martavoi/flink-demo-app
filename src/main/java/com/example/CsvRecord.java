package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Data model for CSV input records.
 * Expected CSV format: profile_id,amount,timestamp
 */
public class CsvRecord {
    private String profileId;
    private double amount;
    private LocalDateTime timestamp;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public CsvRecord() {}
    
    public CsvRecord(String profileId, double amount, LocalDateTime timestamp) {
        this.profileId = profileId;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    
    // Factory method to create from CSV line
    public static CsvRecord fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid CSV line: " + csvLine);
        }
        
        String profileId = parts[0].trim();
        double amount = Double.parseDouble(parts[1].trim());
        LocalDateTime timestamp = LocalDateTime.parse(parts[2].trim(), FORMATTER);
        
        return new CsvRecord(profileId, amount, timestamp);
    }
    
    // Getters and setters
    public String getProfileId() {
        return profileId;
    }
    
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    // Get timestamp as milliseconds for Flink processing
    public long getTimestampMillis() {
        return timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    @Override
    public String toString() {
        return "CsvRecord{" +
                "profileId='" + profileId + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CsvRecord csvRecord = (CsvRecord) o;
        return Double.compare(csvRecord.amount, amount) == 0 &&
                Objects.equals(profileId, csvRecord.profileId) &&
                Objects.equals(timestamp, csvRecord.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(profileId, amount, timestamp);
    }
} 