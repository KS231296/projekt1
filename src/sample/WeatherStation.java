package sample;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


public class WeatherStation {
    private String miasto;
    private String url;
    private String units;
    private StringBuffer response;
    private boolean connected = false;

    public WeatherStation() {
        this.miasto = "Wroclaw";
        this.units = "metric";
        this.response = new StringBuffer();
        this.connected = false;
    }

    public WeatherStation(String miasto) {
        this.miasto = miasto;
        this.units = "metric";
        this.response = new StringBuffer();
        this.connected = false;
    }

    public WeatherStation(String miasto, String units) {
        this.miasto = miasto;
        this.units = units;
        this.response = new StringBuffer();
        this.connected = false;
    }

    public void setMiasto(String miasto) {
        this.miasto = miasto;
        this.connected = false;
    }

    public void setUnits(String units) {
        this.units = units;
        this.connected = false;
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

    public String createURL() {
        return "http://api.openweathermap.org/data/2.5/weather?q=" + miasto + "&units=" + units + "&APPID=84f311e61a853d7c6881876cd312d669";
    }

    public String createURL(String miasto) {
        return "http://api.openweathermap.org/data/2.5/weather?q=" + miasto + "&units=metric&APPID=84f311e61a853d7c6881876cd312d669";
    }

    public String createURL(String miasto, String units) {
        return "http://api.openweathermap.org/data/2.5/weather?q=" + miasto + "&units=" + units + "&APPID=84f311e61a853d7c6881876cd312d669";
    }

    public void connect() {
        System.out.println("Connecting...");
        try {
            URL obj = new URL(this.createURL());
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connected = true;
        } catch (MalformedURLException ex) {
            System.out.println("bad url");
        } catch (IOException ex) {
            System.out.println("Connection failed");
        }

    }

    public Weather createWeatherObject() {
        Gson gson = new Gson();
        if(!connected) {
            this.connect();
        }
        Map m = gson.fromJson(response.toString(), Map.class);

        Weather pogoda = gson.fromJson(m.get("main").toString(), Weather.class);
        this.clearResponse();
        return pogoda;
    }

    public void clearResponse(){
        response = new StringBuffer();
        connected = false;
    }
}