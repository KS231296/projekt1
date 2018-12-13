package sample;


import java.util.Observable;
import java.util.Observer;

public class WeatherObserwer implements Observer {
    private Weather weather;
    private Observable weatherController;

    public Weather getWeather() {
        return weather;
    }

    @Override
    public void update(Observable weatherController, Object weather) {
        this.weather = (Weather) weather;
        System.out.println("Weather updated: " + weather.toString());
    }

}
