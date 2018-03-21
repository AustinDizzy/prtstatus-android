package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface StatusDao {
    @Query("SELECT * FROM updates")
    List<PRTStatus> getAll();

    @Query("SELECT * FROM updates WHERE timestamp between (:now-(60*60*24*7)) and :now ORDER BY id ASC")
    List<PRTStatus> getRecent(long now);

    @Query("SELECT * FROM updates ORDER BY id DESC LIMIT 1")
    PRTStatus getLast();

    @Insert
    void insert(PRTStatus status);
}
