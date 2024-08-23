package com.example.hometask.database;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Converters class provides type conversion methods for Room database.
 * It allows Room to store complex types like Date by converting them to and from types that Room can persist.
 */
public class Converters {

    /**
     * Converts a Long timestamp to a Date object.
     * This method is used when reading dates from the database.
     *
     * @param value The timestamp value as Long, can be null.
     * @return A Date object corresponding to the timestamp, or null if the input is null.
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Converts a Date object to a Long timestamp.
     * This method is used when writing dates to the database.
     *
     * @param date The Date object to convert, can be null.
     * @return The timestamp as a Long value, or null if the input Date is null.
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}