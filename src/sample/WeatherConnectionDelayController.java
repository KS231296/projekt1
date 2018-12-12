package sample;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.time.LocalTime;

public class WeatherConnectionDelayController implements Runnable, Observable {


    protected Thread worker;
    protected volatile boolean isRunning = false;
    protected int interval;
    private WeatherStation station;
    private Weather weather;

    public WeatherConnectionDelayController(WeatherStation station, int interval) {
        this.interval = interval;
        this.station = station;
    }

    public WeatherConnectionDelayController(WeatherStation station) {
        this.interval = 60000;
        this.station = station;
    }


    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void stop() {
        isRunning = false;
    }

    public void interrupt() {

        isRunning = false;
        worker.interrupt();
    }


    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {

            try {
                weather = station.createWeatherObject();
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Failed to complete operation");
            }

        }

    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {

    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {

    }
}
