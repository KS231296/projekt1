package sample;


import java.util.Observable;

public class WeatherConnectionDelayController extends Observable implements Runnable {


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


    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {

            try {
                weather = station.createWeatherObject();
                System.out.println("controller changed: "+ weather.toString());
                setChanged();
                notifyObservers(weather);
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Failed to complete operation");
            }

        }

    }




}
