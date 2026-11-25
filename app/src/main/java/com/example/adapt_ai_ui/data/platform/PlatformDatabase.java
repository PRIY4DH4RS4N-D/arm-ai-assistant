package com.example.adapt_ai_ui.data.platform;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import java.util.List;

@Database(entities = {PlatformDatabase.Profile.class, PlatformDatabase.Record.class},
    version = 1, exportSchema = false)
public abstract class PlatformDatabase extends RoomDatabase {
    public abstract Store store();

    @Entity(tableName = "profiles")
    public static class Profile {
        @PrimaryKey @NonNull public String id = "";
        @NonNull public String name = "";
        public boolean guest;
        public long createdAt;
    }

    @Entity(tableName = "learning_records", primaryKeys = {"profileId", "recordKey"},
        foreignKeys = @ForeignKey(entity = Profile.class, parentColumns = "id",
            childColumns = "profileId", onDelete = ForeignKey.CASCADE))
    public static class Record {
        @NonNull public String profileId = "";
        @NonNull public String recordKey = "";
        @NonNull public String payload = "";
    }

    @Dao
    public interface Store {
        @Query("SELECT * FROM profiles ORDER BY createdAt, id") List<Profile> profiles();
        @Query("SELECT * FROM profiles WHERE id = :id") Profile profile(String id);
        @Insert void insertProfile(Profile profile);
        @Query("UPDATE profiles SET name = :name, guest = :guest WHERE id = :id")
        void updateProfile(String id, String name, boolean guest);
        @Query("DELETE FROM profiles WHERE id = :id") int deleteProfile(String id);
        @Insert(onConflict = OnConflictStrategy.REPLACE) void write(Record record);
        @Query("SELECT payload FROM learning_records WHERE profileId = :id AND recordKey = :key")
        String read(String id, String key);
        @Query("DELETE FROM learning_records WHERE profileId = :id AND recordKey = :key")
        int delete(String id, String key);
        @Query("DELETE FROM learning_records WHERE profileId = :id") void clear(String id);
    }
}
