package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.preference.ListPreference;

import java.util.List;

@Dao
public interface StatusDao {

    @Query("SELECT * FROM updates ORDER BY timestamp DESC LIMIT :num+1")
    List<PRTStatus> getNUpdates(int num);

    @Query("SELECT * FROM updates WHERE timestamp between (:now-(60*60*24*:days)) and :now ORDER BY timestamp DESC")
    List<PRTStatus> getNDays(long now, int days);

    @Query("SELECT * FROM updates ORDER BY timestamp DESC LIMIT 1")
    PRTStatus getLast();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PRTStatus status);
}
