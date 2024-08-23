package com.example.hometask.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.hometask.model.User;

/**
 * AppDatabase is the main database class for the application.
 * It uses Room persistence library to abstract the database operations.
 */
@Database(entities = {User.class}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    /**
     * The name of the database file.
     */
    private static final String DATABASE_NAME = "user_database";

    /**
     * Abstract method to get the UserDao.
     * Room will generate an implementation of this method.
     *
     * @return UserDao instance for database operations related to User entity.
     */
    public abstract UserDao userDao();

    /**
     * The singleton instance of the database.
     */
    private static volatile AppDatabase INSTANCE;

    /**
     * Migration from version 1 to 2 of the database.
     * Adds a 'created_at' column to the users table.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN created_at INTEGER");
        }
    };

    /**
     * Gets the singleton instance of the database.
     * If the instance doesn't exist, it creates one.
     * This method is thread-safe.
     *
     * @param context The application context.
     * @return The singleton instance of AppDatabase.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration() // This will handle any unforeseen schema changes
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}