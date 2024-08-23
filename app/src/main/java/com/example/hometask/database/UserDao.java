package com.example.hometask.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.hometask.model.User;

import java.util.List;

/**
 * UserDao (Data Access Object) interface defines the database operations
 * that can be performed on the User entity.
 * Room will generate an implementation of this interface.
 */
@Dao
public interface UserDao {

    /**
     * Retrieves all users from the database.
     *
     * @return A List of all User objects in the database.
     */
    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    /**
     * Retrieves a specific user from the database by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The User object with the specified ID, or null if not found.
     */
    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);

    /**
     * Inserts a new user into the database.
     * If a user with the same ID already exists, it will be replaced.
     *
     * @param user The User object to insert.
     * @return The new rowId for the inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);

    /**
     * Updates an existing user in the database.
     *
     * @param user The User object to update. Must have an ID that exists in the database.
     */
    @Update
    void updateUser(User user);

    /**
     * Deletes a user from the database.
     *
     * @param user The User object to delete. Must have an ID that exists in the database.
     */
    @Delete
    void deleteUser(User user);
}