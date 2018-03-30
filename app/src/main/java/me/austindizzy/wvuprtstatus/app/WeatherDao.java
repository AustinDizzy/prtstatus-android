package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface WeatherDao {

    @SuppressWarnings("unused")
    @Query("SELECT * FROM weather WHERE timestamp between (:now-(60*60*24*7)) and :now ORDER BY timestamp DESC")
    List<Weather> getRecent(long now);

    @Query("SELECT * FROM weather ORDER BY timestamp DESC LIMIT 1")
    Weather getLast();

    @SuppressWarnings("unused")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Weather w);
}
