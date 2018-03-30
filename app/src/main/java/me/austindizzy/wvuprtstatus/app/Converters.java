package me.austindizzy.wvuprtstatus.app;


import android.arch.persistence.room.TypeConverter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

public class Converters {

    public static CharSequence timestampToWhen(long t) {
        CharSequence when;
        long now = System.currentTimeMillis();
        long timestamp = t * 1000;
        if (now - timestamp > (60*60*24*7)*1000) {
            when = DateFormat.format("h:mma, M/d/y", timestamp);
        } else {
            when = DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.SECOND_IN_MILLIS);
        }
        return when;
    }
    @TypeConverter
    public String[] fromString(String s) {
        return s.split(",");
    }

    @TypeConverter
    public String fromStringArr(String[] arr) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < arr.length; i++) {
            s.append(arr[i]);
        }
        return s.toString();
    }
}
