package com.cst.vingcard.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;

import com.cst.vingcard.entity.Setup;

@Database(entities = {
        Setup.class
},
        version = 5,
        exportSchema = false)

public abstract class RoomDatabase extends androidx.room.RoomDatabase {

    private static RoomDatabase INSTANCE;

    public static RoomDatabase getVincardDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, RoomDatabase.class, "vingTerminal")
                    // allow queries on the main thread.
                    // Don't do this on a real app! See PersistenceBasicSample for an example.
                    //.allowMainThreadQueries()
                    //.addMigrations(RoomMigrationConstants.MIGRATION_1_2) ->example if doing DB migration in the future
                    .build();
        }

        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public abstract RoomConfigurationDao vingCardConfigurationDao();

}
