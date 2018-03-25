package me.austindizzy.wvuprtstatus.app;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

@Entity(tableName = "weather")
public class Weather {
    // Timestamp is a long integer of a Unix timestamp of the status event
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name= "temperature")
    private double temperature;

    @ColumnInfo(name = "humidity")
    private double humidity;

    @ColumnInfo(name = "weather")
    private String weather;

    @ColumnInfo(name = "conditions")
    private String conditions;

    @ColumnInfo(name = "feelsLike")
    private double feelsLike;

    @ColumnInfo(name = "precip1hr")
    private double precip1hr;

    @ColumnInfo(name = "precipToday")
    private double precipToday;

    @ColumnInfo(name = "visibility")
    private double visibility;

    @ColumnInfo(name = "windDir")
    private String windDir;

    @ColumnInfo(name = "windSpeed")
    private double windSpeed;

    public Weather() {}

    public Weather(Bundle b) {
        this(b.getDouble("temperature"), b.getDouble("humidity"),
                b.getString("weather"), b.getString("conditions"), b.getDouble("feelsLike"),
                b.getDouble("precip1hr"), b.getDouble("precipToday"), b.getDouble("visibility"),
                b.getString("windDir"), b.getDouble("windSpeed"));
    }

    public Weather(JSONObject jsonObj) throws JSONException {
        this(jsonObj.getDouble("temperature"), jsonObj.getDouble("humidity"),
                   jsonObj.getString("weather"), jsonObj.getString("conditions"),
                   jsonObj.getDouble("feelsLike"), jsonObj.getDouble("precip1hr"),
                   jsonObj.getDouble("precipToday"), jsonObj.getDouble("visibility"),
                   jsonObj.getString("windDir"), jsonObj.getDouble("windSpeed"));
    }

    public Weather(double t, double h, String w, String c, double f, double p1, double pT, double v, String wD, double wS) {
        setTemperature(t);
        setHumidity(h);
        setWeather(w);
        setConditions(c);
        setFeelsLike(f);
        setPrecip1hr(p1);
        setPrecipToday(pT);
        setVisibility(v);
        setWindDir(wD);
        setWindSpeed(wS);
    }

    public String toString() {
        String msg = getConditions();
        if (getWindSpeed() > 0) {
            msg += String.format(" - %dMPH %s winds", getWindSpeed(), getWindDir());
        }
        return msg;
    }

    public void setId(long id) { this.id = id; }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
}

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public void setPrecip1hr(double precip1hr) {
        this.precip1hr = precip1hr;
    }

    public void setPrecipToday(double precipToday) {
        this.precipToday = precipToday;
    }

    public void setWindDir(String windDir) {
        this.windDir = windDir;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public long getId() { return this.id; }

    public long getTimestamp() {
        return timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getTemperatureString() {
        return String.format("%s\u00b0F", getTemperature());
    }

    public double getHumidity() {
        return humidity;
    }

    public String getWeather() {
        return weather;
    }

    public String getConditions() {
        return conditions;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public double getPrecip1hr() {
        return precip1hr;
    }

    public double getPrecipToday() {
        return precipToday;
    }

    public double getVisibility() {
        return visibility;
    }

    public String getWindDir() {
        return windDir;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}
