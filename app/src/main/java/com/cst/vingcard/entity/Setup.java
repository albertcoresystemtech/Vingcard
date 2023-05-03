package com.cst.vingcard.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = Setup.TABLE_NAME)
public class Setup {

    public static final String TABLE_NAME = "setup";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_HOST_URL = "host_url";
    public static final String COLUMN_TOKEN = "token";
    public static final String COLUMN_CREATED_AT = "created_at";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = COLUMN_HOST_URL)
    private String hostUrl;

    @ColumnInfo(name = COLUMN_TOKEN)
    private String token;

    @ColumnInfo(name = COLUMN_CREATED_AT)
    private String createdAt;


    public Setup() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostUrl() {
        return hostUrl == null ? "" : hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public String getToken() {
        return token == null ? "" : token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

}
