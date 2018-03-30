package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Bundle;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "updates")
public class PRTStatus {
    @PrimaryKey(autoGenerate = true)
    private long id;
    // Status can be an integer from 1 through 7 meaning the following:
    // 1 - normal, 2 - down between A and B, 3 - down for maintenance, 4 - down, 5 - special event, 6 - down at A, 7 - closed
    // However, PRT operators seem to play favorites with just 1, 5, and 7 even though others are more applicable
    @ColumnInfo(name = "status")
    private int status;
    // Message will contain the message text as entered by the PRT operators
    @ColumnInfo(name = "message")
    private String message;
    // Timestamp is a long integer of a Unix timestamp of the status event
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    // Stations is a string array of the stations the event occurred at
    @ColumnInfo(name = "stations")
    private String[] stations;
    // BussesDispatched is a boolean true if the event required busses to be dispatched
    @ColumnInfo(name="bussesDispatched")
    private boolean bussesDispatched;

    public PRTStatus() {}

    public PRTStatus(Bundle b) {
        this(b.getInt("status"), b.getString("message"), b.getLong("timestamp"), b.getStringArray("stations"), b.getBoolean("bussesDispatched"));
    }

    public PRTStatus(JSONObject jsonObj) {
        try {
            setStatus(jsonObj.getInt("status"));
            setTimestamp(jsonObj.getLong("timestamp"));
            setMessage(jsonObj.getString("message"));
            List<String> stations = new ArrayList<>();
            JSONArray stationsArr = jsonObj.getJSONArray("stations");
            for(int i = 0; i < stationsArr.length(); i++) {
                stations.add(stationsArr.getString(i));
            }
            setStations(stations.toArray(new String[stationsArr.length()]));
            setBussesDispatched(jsonObj.getBoolean("bussesDispatched"));
        } catch (JSONException err) {
            //TODO: something with err
        }
    }

    public PRTStatus(int s, String m, long t) {
        setStatus(s);
        setMessage(m);
        setTimestamp(t);
    }

    public PRTStatus(int s, String m, long t, String[] st, boolean b) {
        this(s, m, t);
        setStations(st);
        setBussesDispatched(b);
    }

    public String toString() {
        return getMessage();
    }

    // IsDown returns true if the current status is at a non-normal state (i.e. open but not running)
    public boolean IsDown() {
        return !IsOpen() && !IsClosed();
    }

    // IsClosed returns true if the current status is that the PRT is closed (i.e. status == 7)
    public boolean IsClosed() {
        return getStatus() == 7;
    }

    // IsOpen returns true if the current status is that the PRT is open (i.e. status == 1)
    public boolean IsOpen() { return getStatus() == 1; }

    public void setId(long i) { this.id = i; }

    public void setStatus(int s) {
        this.status = s;
    }

    public void setMessage(String m) {
        this.message = m;
    }

    public void setTimestamp(long t) {
        this.timestamp = t;
    }

    public void setStations(String[] st) {
        this.stations = st;
    }

    public void setBussesDispatched(boolean b) {
        this.bussesDispatched = b;
    }

    public long getId() { return this.id; }
    public int getStatus() { return this.status; }
    public String getMessage() { return this.message; }
    public long getTimestamp() { return this.timestamp; }
    public String[] getStations() { return this.stations; }
    public boolean getBussesDispatched() { return this.bussesDispatched; }
}
