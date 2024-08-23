package com.example.hometask.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import com.example.hometask.database.Converters;
import java.io.Serializable;
import java.util.Objects;

/**
 * User entity represents a user in the application.
 * It's used both as a Room database entity and as a model for API responses.
 */
@Entity(tableName = "users")
public class User implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String email;

    @ColumnInfo(name = "first_name")
    @SerializedName("first_name")
    private String firstName;

    @ColumnInfo(name = "last_name")
    @SerializedName("last_name")
    private String lastName;

    private String avatar;

    @ColumnInfo(name = "created_at")
    @TypeConverters(Converters.class)
    private Date createdAt;

    /**
     * Constructor for creating a new User.
     *
     * @param email     The user's email address.
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @param avatar    The URL or path to the user's avatar image.
     */
    public User(String email, String firstName, String lastName, String avatar) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.createdAt = new Date(); // Set current date when user is created
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    /**
     * Checks if this User is equal to another object.
     * Users are considered equal if they have the same id.
     *
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    /**
     * Generates a hash code for this User.
     * The hash code is based on the user's id.
     *
     * @return The hash code for this User.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}