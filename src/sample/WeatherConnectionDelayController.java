package sample;


import java.io.IOException;
import java.util.Observable;

public class WeatherConnectionDelayController extends Observable implements Runnable {


    protected Thread worker;
    protected volatile boolean isRunning = false;
    protected int interval;
    private WeatherStation station;
    private Weather weather;
    private int connectionResult;

    public WeatherConnectionDelayController(WeatherStation station, int interval) {
        this.interval = interval;
        this.station = station;
    }

    public WeatherConnectionDelayController(WeatherStation station) {
        this.interval = 60000;
        this.station = station;
    }

    public WeatherStation getStation() {
        return station;
    }

    public Weather getWeather() {
        return weather;
    }

    public int getConnectionResult() {
        return connectionResult;
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


    @Override
    public void run()  {
        isRunning = true;
        while (isRunning) {

            try {
                weather = station.createWeatherObject();
                connectionResult = station.getConnectionResult();
                setChanged();
                notifyObservers(weather);
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

    }




}
