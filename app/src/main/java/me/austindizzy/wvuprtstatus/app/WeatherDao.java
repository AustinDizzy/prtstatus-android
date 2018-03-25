package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface WeatherDao {
    @Query("SELECT * FROM weather")
    List<Weather> getAll();

    @Query("SELECT * FROM weather WHERE timestamp between (:now-(60*60*24*7)) and :now ORDER BY timestamp DESC")
    List<Weather> getRecent(long now);

    @Query("SELECT * FROM weather ORDER BY timestamp DESC LIMIT 1")
    Weather getLast();

    @Insert
    void insert(Weather w);
}
