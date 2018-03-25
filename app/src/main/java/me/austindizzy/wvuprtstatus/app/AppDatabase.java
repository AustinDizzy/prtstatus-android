package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

/**
 * Created by Austin on 3/2/2018.
 */

@Database(entities = {PRTStatus.class, Weather.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase mDB;
    public abstract StatusDao statusDao();
    public abstract WeatherDao weatherDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (mDB == null) {

            mDB = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "wvuprtstatus")
                    .addCallback()
                    .allowMainThreadQueries()
                    .build();
        }
        return mDB;
    }

    public static void destoryInstance() {
        mDB = null;
    }
}
