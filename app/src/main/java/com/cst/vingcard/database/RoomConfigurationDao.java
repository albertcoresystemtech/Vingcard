package com.cst.vingcard.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cst.vingcard.entity.Setup;

import java.util.List;

@Dao
public interface RoomConfigurationDao {

    //----------for Setup
    @Insert
    void insertDbSetup(Setup... setup);

    @Query("SELECT * FROM " + Setup.TABLE_NAME + " ORDER BY " + Setup.COLUMN_CREATED_AT + " DESC")
    List<Setup> getDbAllSetup();

    @Query("SELECT * FROM " + Setup.TABLE_NAME + " LIMIT 1")
    Setup getDbSingleSetupInfo();

    @Query("SELECT * FROM " + Setup.TABLE_NAME + " ORDER BY " + Setup.COLUMN_CREATED_AT + " DESC LIMIT 1")
    Setup getDbLatestSetup();

    @Query("SELECT * FROM " + Setup.TABLE_NAME + " where " + Setup.COLUMN_ID + " = :id")
    Setup getDbSetupById(int id);

    @Update
    void updateDbSetupEntry(Setup debugLog);

    @Query("DELETE FROM " + Setup.TABLE_NAME)
    void deleteDbAllSetup();


}
