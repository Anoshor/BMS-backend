package com.bms.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleDateDeserializer extends JsonDeserializer<LocalDate> {
    
    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getValueAsString();
        
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try standard ISO format first: yyyy-MM-dd
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e1) {
            try {
                // Try ISO date-time format: yyyy-MM-ddTHH:mm:ss
                return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                try {
                    // Try JavaScript Date format: "Wed Sep 10 2025 21:12:21 GMT+0530"
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, 
                        DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z"));
                    return zonedDateTime.toLocalDate();
                } catch (DateTimeParseException e3) {
                    try {
                        // Try alternative JS format: "Wed Sep 10 2025 21:12:00 GMT+0530"
                        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString,
                            DateTimeFormatter.ofPattern("EEE MMM d yyyy HH:mm:ss 'GMT'Z"));
                        return zonedDateTime.toLocalDate();
                    } catch (DateTimeParseException e4) {
                        // If all fail, throw descriptive error
                        throw new IOException("Unable to parse date: '" + dateString + 
                            "'. Expected formats: yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss, or JavaScript Date string");
                    }
                }
            }
        }
    }
}