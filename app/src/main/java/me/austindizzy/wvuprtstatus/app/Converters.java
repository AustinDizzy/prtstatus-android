package me.austindizzy.wvuprtstatus.app;


import android.arch.persistence.room.TypeConverter;

public class Converters {
    @TypeConverter
    public String[] fromString(String s) {
        return s.split(",");
    }

    @TypeConverter
    public String fromStringArr(String[] arr) {
        if (arr == null) {
            return "";
        }
        String s = arr[0];
        for(int i = 1; i < arr.length; i++) {
            s += "," + arr[i];
        }
        return s;
    }
}
