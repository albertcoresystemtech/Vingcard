package com.cst.vingcard.database;

import android.content.Context;

import com.cst.vingcard.entity.Setup;

import java.util.ArrayList;
import java.util.List;

public class RoomCaller {

    private RoomDatabase roomDatabase;
    private RoomConfigurationDao dbDao;

    public RoomCaller(Context context) {
        roomDatabase = RoomDatabase.getVincardDatabase(context);
        dbDao = roomDatabase.vingCardConfigurationDao();
    }

    //----------for Setup

    public void insertSetup(final Setup setup) {
        try {
            Thread t = new Thread(() -> dbDao.insertDbSetup(setup));

            t.start();
            t.join();
        } catch (InterruptedException ignored) {
        }
    }

    public List<Setup> getAllSetup() {
        final List<Setup>[] setups = new List[]{new ArrayList<>()};

        try {
            Thread t = new Thread(() -> setups[0] = dbDao.getDbAllSetup());

            t.start();
            t.join();
        } catch (InterruptedException ignored) {
        }

        return setups[0];
    }

    public Setup getSetupInfo() {
        final Setup[] setups = {null};

        try {
            Thread t = new Thread(() -> setups[0] = dbDao.getDbSingleSetupInfo());
            t.start();
            t.join();
        } catch (InterruptedException ignored) {
        }

        return setups[0];
    }

    public Setup getLatestSetup() {
        final Setup[] setups = {null};

        try {
            Thread t = new Thread(() -> setups[0] = dbDao.getDbLatestSetup());

            t.start();
            t.join();
        } catch (InterruptedException ignored) {
        }

        return setups[0];
    }

    public Setup getSetupById(final int id) {
        final Setup[] setups = {null};

        try {
            Thread t = new Thread(() -> setups[0] = dbDao.getDbSetupById(id));

            t.start();
            t.join();
        } catch (InterruptedException ignored) {
        }

        return setups[0];
    }


    public void updateSetupEntry(final Setup setup) {
        try {
            Thread t = new Thread(() -> dbDao.updateDbSetupEntry(setup));

            t.start();
            t.join();
        } catch (InterruptedException ignored) {
        }
    }

    public void deleteAllSetup() {
        try {
            Thread t = new Thread(() -> dbDao.deleteDbAllSetup());

            t.start();
            t.join();
        } catch (InterruptedException ignored) {
        }
    }



}
