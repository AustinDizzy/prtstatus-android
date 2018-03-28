package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface StatusDao {

    @Query("SELECT * FROM updates WHERE timestamp between (:now-(60*60*24*7)) and :now ORDER BY timestamp DESC LIMIT 15")
    List<PRTStatus> getRecent(long now);

    @Query("SELECT * FROM updates ORDER BY timestamp DESC LIMIT 1")
    PRTStatus getLast();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PRTStatus status);
}
