package sample;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Observable;

/**
 * klasa umozliwiajaca polaczenie z serwerem
 */
public class WeatherStation extends Observable implements Runnable{
    private String miasto;
    private String url;
    private String units;
    private StringBuffer response;
    private int connectionResult = 0;
    protected volatile boolean isRunning = false;
    protected int interval;
    private Weather weather;




    protected boolean connected = false;


    public WeatherStation() {
        this.miasto = "Wroclaw";
        this.units = "metric";
        this.response = new StringBuffer();
        this.connected = false;
        this.interval = 60000;
    }

    public WeatherStation(String miasto) {
        this.miasto = miasto;
        this.units = "metric";
        this.response = new StringBuffer();
        this.connected = false;
    }

    public WeatherStation(String miasto, String units, int interval) {
        this.miasto = miasto;
        this.units = units;
        this.response = new StringBuffer();
        this.connected = false;
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setMiasto(String miasto) {
        this.miasto = miasto;
        this.connected = false;
    }

    public void setUnits(String units) {
        this.units = units;
        this.connected = false;
    }

    public int getConnectionResult() {
        return connectionResult;
    }

    public String getMiasto() {
        return miasto;
    }

    public String getUrl() {
        return url;
    }

    public String getUnits() {
        return units;
    }

    public StringBuffer getResponse() {
        return response;
    }

    /**
     * tworzenie url bez podania parametrow wstepnych
     * @return String url utworzony z pol miasto i units
     */
    public String createURL() {
        return "http://api.openweathermap.org/data/2.5/weather?q=" + miasto + "&units=" + units + "&APPID=84f311e61a853d7c6881876cd312d669";
    }

    /**
     * tworzenie url z podaniem miasta
     * @param miasto miasto dla ktorego beda pobierane dane
     * @return String url z wybranym miastem i w jednostkach metric - stopnie Celsjusza
     */
    public String createURL(String miasto) {
        return "http://api.openweathermap.org/data/2.5/weather?q=" + miasto + "&units=metric&APPID=84f311e61a853d7c6881876cd312d669";
    }

    /**
     * tworzenie url z podaniem miasta i jednostek
     * @param miasto wybrane miasto
     * @param units wybrane jednostki("metric", "imperial" lub "")
     * @return String url z wybranym miastem i w wybranych jednostkach
     */
    public String createURL(String miasto, String units) {
        return "http://api.openweathermap.org/data/2.5/weather?q=" + miasto + "&units=" + units + "&APPID=84f311e61a853d7c6881876cd312d669";
    }

    /**
     * metoda sluzaca do polaczenie z serwerem. ustawia pole response na uzyskana odpowiedz
     */
    public void connect() {
        try {
            URL obj = new URL(this.createURL());
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connected = true;
            connectionResult = 1;
        }catch (MalformedURLException ex) {
            System.out.println("bad url");
            connectionResult = -1;

        }
        catch (IOException ex) {
            System.out.println("Connection failed");
            connectionResult = -2;

        }
    }

    /**
     * metoda tworzaca obiekt Weather
     * wykorzystuje metode connect()
     * @return obiekt Weather zawierajacy informacje o pogodzie utworzonej z odpowiedzi serwera
     */
    public Weather createWeatherObject() {
        Gson gson = new Gson();
        Weather pogoda = new Weather();
        if (!connected) {
            this.connect();
        }

        if (connectionResult == 1) {

            Map m = gson.fromJson(response.toString(), Map.class);

            pogoda = gson.fromJson(m.get("main").toString(), Weather.class);
            this.clearResponse();
        } else {
            return pogoda;
        }


        return pogoda;
    }

    /**
     * czysci pole response
     */
    public void clearResponse() {
        response = new StringBuffer();
        connected = false;
    }

    public void stop() {
        isRunning = false;
    }


    @Override
    public void run()  {
        isRunning = true;
        while (isRunning) {

            try {
                weather = createWeatherObject();
                setChanged();
                notifyObservers(weather);
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

    }


}
