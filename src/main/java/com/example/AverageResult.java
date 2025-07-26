package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Data model for the result of average calculation.
 * Will be written to results.csv
 */
public class AverageResult {
    private String profileId;
    private double averageAmount;
    private LocalDateTime windowEnd;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public AverageResult() {}
    
    public AverageResult(String profileId, double averageAmount, LocalDateTime windowEnd) {
        this.profileId = profileId;
        this.averageAmount = averageAmount;
        this.windowEnd = windowEnd;
    }
    
    // Convert to CSV line for output
    public String toCsvLine() {
        return profileId + "," + String.format("%.2f", averageAmount) + "," + windowEnd.format(FORMATTER);
    }
    
    // Getters and setters
    public String getProfileId() {
        return profileId;
    }
    
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
    
    public double getAverageAmount() {
        return averageAmount;
    }
    
    public void setAverageAmount(double averageAmount) {
        this.averageAmount = averageAmount;
    }
    
    public LocalDateTime getWindowEnd() {
        return windowEnd;
    }
    
    public void setWindowEnd(LocalDateTime windowEnd) {
        this.windowEnd = windowEnd;
    }
    
    @Override
    public String toString() {
        return "AverageResult{" +
                "profileId='" + profileId + '\'' +
                ", averageAmount=" + averageAmount +
                ", windowEnd=" + windowEnd +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AverageResult that = (AverageResult) o;
        return Double.compare(that.averageAmount, averageAmount) == 0 &&
                Objects.equals(profileId, that.profileId) &&
                Objects.equals(windowEnd, that.windowEnd);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(profileId, averageAmount, windowEnd);
    }
} 