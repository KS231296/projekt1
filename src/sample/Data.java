package sample;


import com.google.gson.Gson;
import javafx.scene.control.Alert;

import java.io.*;
import java.util.ArrayList;

public class Data {
    private String units;
    private int[] startTime;
    private String miasto;
    private ArrayList<int[]> nowTime;
    private ArrayList<Weather> weather;
   static Gson gson = new Gson();


    public Data(int[] startTime, String miasto, ArrayList<int[]> nowTime, ArrayList<Weather> weather, String units) {
        this.startTime = startTime;
        this.miasto = miasto;
        this.nowTime = nowTime;
        this.weather = weather;
        this.units = units;
    }

    public static void saveJSON(Data data, File dataFile) {

        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to save data");
        }
    }

    public static Data readJSON(File dataFile) {
        Data data = null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile))) {
            data = gson.fromJson(bufferedReader, Data.class);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load data");
        }
        return data;
    }






    public String getUnits() {
        return units;
    }

    public int[] getStartTime() {
        return startTime;
    }

    public String getMiasto() {
        return miasto;
    }

    public ArrayList<int[]> getNowTime() {
        return nowTime;
    }

    public ArrayList<Weather> getWeather() {
        return weather;
    }
}
