package sample;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;


public class Controller implements Observer {

    WeatherStation station = new WeatherStation();
    boolean started = false;
    boolean paused = false;
    LocalDateTime startTime;

    private Weather weather;
    private Observable weatherController;

    private Thread weatherUpdates = new Thread();

    WeatherConnectionDelayController connectionDelayController;

    private String miasto;
    private String units = "metric";
    private String info;

    Gson gson = new Gson();
    File dataFile = new File("data.json");

    private ArrayList<int[]> nowTimeData;
    private ArrayList<Weather> weatherData;

    @FXML
    private TextField txtMiasto;

    @FXML
    private Button btnStartPause;

    @FXML
    private Button btnInterrupt;

    @FXML
    private Button btnLoad;

    @FXML
    private TextField txtOdswierzanie;

    @FXML
    private ButtonBar unitsBar;

    @FXML
    private RadioButton unitsMetric;

    @FXML
    private ToggleGroup unitsGroup;

    @FXML
    private RadioButton unitsImperial;

    @FXML
    private RadioButton unitsDefault;

    @FXML
    private TextArea areaStatistics;

    @FXML
    private Button btnSave;

    @FXML
    private AreaChart<Number, Number> chartTemperature;

    @FXML
    private LineChart<Number, Number> chartPressure;

    @FXML
    private LineChart<Number, Number> chartHumidity;

    private XYChart.Series<Number, Number> temperature = new XYChart.Series<>();
    private XYChart.Series<Number, Number> temp_min = new XYChart.Series<>();
    private XYChart.Series<Number, Number> temp_max = new XYChart.Series<>();
    private XYChart.Series<Number, Number> humidity = new XYChart.Series<>();
    private XYChart.Series<Number, Number> pressure = new XYChart.Series<>();


    @FXML
    void celsius(ActionEvent event) {
        units = "metric";
        System.out.println("metric");

    }

    @FXML
    void fahrenheit(ActionEvent event) {
        units = "imperial";
        System.out.println("imperial");
    }

    @FXML
    void interrupt(ActionEvent event) {
        System.out.println("stopping");
        connectionDelayController.stop();
        weatherUpdates.interrupt();
        started = false;
    }

    @FXML
    void kelvin(ActionEvent event) {
        units = "";
        System.out.println("default");
    }

    @FXML
    void loadData(ActionEvent event) {

        FileChooser choose = new FileChooser();
        choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("json", "*.json", "*.txt"));
        final File[] ddd = {new File("")};
        new Alert(Alert.AlertType.CONFIRMATION, "Czy chcesz załądować własny plik?", new ButtonType("tak", ButtonBar.ButtonData.YES), new ButtonType("załaduj z pamięci", ButtonBar.ButtonData.NO), new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE)).showAndWait().ifPresent(response -> {
            switch (response.getButtonData()) {
                case CANCEL_CLOSE: {
                    System.out.println("cancel");
                    ddd[0] = null;
                    break;
                }
                case NO: {
                    System.out.println("no");
                    ddd[0] = dataFile;
                    break;
                }
                case YES: {
                    System.out.println("yes");
                    ddd[0] = choose.showOpenDialog(new Stage());
                    break;
                }
            }
        });

        if (ddd[0] != null) {
            String path = ddd[0].toURI().toASCIIString();
            System.out.println(path);

            Data data = Data.readJSON(ddd[0]);
            System.out.println("loading");
            chartTemperature.getData().clear();
            chartHumidity.getData().clear();
            chartPressure.getData().clear();

            temperature.getData().clear();
            temp_min.getData().clear();
            temp_max.getData().clear();
            humidity.getData().clear();
            pressure.getData().clear();
            humidity.getData().clear();
            weatherData = new ArrayList<Weather>();
            nowTimeData = new ArrayList<int[]>();

            units = data.getUnits();
            startTime = LocalDateTime.of(data.getStartTime()[0], data.getStartTime()[1], data.getStartTime()[2], data.getStartTime()[3], data.getStartTime()[4], data.getStartTime()[5]);
            miasto = data.getMiasto();
            nowTimeData = data.getNowTime();
            weatherData = data.getWeather();

            info = String.format("Czas rozpoczęcia pomiarów: %s%nMiasto: %s", startTime.toString(), miasto);

            for (int i = 0; i < weatherData.size(); i++) {
                LocalDateTime nowTime = LocalDateTime.of(nowTimeData.get(i)[0], nowTimeData.get(i)[1], nowTimeData.get(i)[2], nowTimeData.get(i)[3], nowTimeData.get(i)[4], nowTimeData.get(i)[5]);
                actualizeDataSeries(nowTime, weatherData.get(i));
            }
            actualizeCharts();
            new Alert(Alert.AlertType.INFORMATION, "loaded").showAndWait();
        }


    }

    @FXML
    void saveData(ActionEvent event) {
        FileChooser choose = new FileChooser();
        choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("json", "*.json", "*.txt"));
        File dataFile = choose.showOpenDialog(new Stage());
        if (dataFile != null) {
            String path = dataFile.toURI().toASCIIString();
            System.out.println(path);
            int[] start = {startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond()};

            Data data = new Data(start, station.getMiasto(), nowTimeData, weatherData, station.getUnits());
            Data.saveJSON(data, dataFile);
            new Alert(Alert.AlertType.INFORMATION, "saved").showAndWait();
        }
    }

    @FXML
    void startPause(ActionEvent event) {

        if (!started) {
            miasto = txtMiasto.getText();
            if (miasto.equals("")) {
                miasto = "London";
                txtMiasto.setText(miasto);
            }
            start();
        } else {
            if (paused) {
                resume();
            } else {
                pause();
            }
        }


    }


    private void start() {
        areaStatistics.setText("");
        chartTemperature.getData().clear();
        chartHumidity.getData().clear();
        chartPressure.getData().clear();

        temperature.getData().clear();
        temp_min.getData().clear();
        temp_max.getData().clear();
        humidity.getData().clear();
        pressure.getData().clear();
        humidity.getData().clear();
        weatherData = new ArrayList<Weather>();
        nowTimeData = new ArrayList<int[]>();

        station.setMiasto(miasto);
        station.setUnits(units);
        int odswierzanie = 60000;
        if (txtOdswierzanie.getText().equals("")) {
            new Alert(Alert.AlertType.WARNING, "Nie podano wartości odświerzania, ustawiono wartość domyślną. (60s)").showAndWait();
        } else {
            try {
                odswierzanie = 1000 * Integer.parseInt(txtOdswierzanie.getText());
            } catch (IllegalArgumentException e) {
                new Alert(Alert.AlertType.ERROR, "Podana wartość odświezrzania nie jest liczbą/ liczba całkowitą.").showAndWait();
                started = false;
                return;
            }
        }
        if (odswierzanie < 15000) {
            odswierzanie = 15000;
            new Alert(Alert.AlertType.WARNING, "Zbyt mała wartość odświerzania, ustawiono wartość minimalną. (15s)").showAndWait();
        }

        connectionDelayController = new WeatherConnectionDelayController(station, odswierzanie);
        weatherUpdates = new Thread(connectionDelayController);
        connectionDelayController.addObserver(this);

        weatherUpdates.start();
        started = true;
        startTime = LocalDateTime.now();
        info = String.format("Czas rozpoczęcia pomiarów: %s%nMiasto: %s", startTime.toString(), miasto);
        areaStatistics.setText(info);
    }

    private void resume() {
        weatherUpdates.resume();
        paused = false;
    }

    private void pause() {
        weatherUpdates.suspend();
        paused = true;
    }

    private void actualizeDataSeries(LocalDateTime nowTime, Weather weather) {
        Long now = Duration.between(startTime, nowTime).toSeconds();

        temperature.getData().add(new XYChart.Data<>(now, weather.getTemperatura()));
        temp_min.getData().add(new XYChart.Data<>(now, weather.getTemp_min()));
        temp_max.getData().add(new XYChart.Data<>(now, weather.getTemp_max()));
        humidity.getData().add(new XYChart.Data<>(now, weather.getWilgotnosc()));
        pressure.getData().add(new XYChart.Data<>(now, weather.getCisnienie()));
    }

    private void actualizeCharts() {

        Platform.runLater(() -> {
            chartPressure.getData().removeAll(chartPressure.getData());
            chartTemperature.getData().removeAll(chartTemperature.getData());
            chartHumidity.getData().removeAll(chartHumidity.getData());

            chartTemperature.getData().add(temp_min);
            chartTemperature.getData().add(temp_max);
            chartTemperature.getData().add(temperature);

            chartHumidity.getData().add(humidity);
            chartPressure.getData().add(pressure);


            double[] stat = calculateStatistics();
            String output = String.format("%n%nIlość pomiarów: %d%n%nTemperatura - %nmin: %.2f%nmax: %.2f%nstd: %.2f%n%nCiśnienie -%nmin: %.2f%nmax: %.2f%nstd: %.2f%n%nWilgotność - %nmin: %.2f%nmax: %.2f%nstd: %.2f", (int) stat[0], stat[1], stat[2], stat[3], stat[4], stat[5], stat[6], stat[7], stat[8], stat[9]);

            areaStatistics.setText(info + output); // {liczbaPomiar, minT, maxT, stdT, minP, maxP, stdP, minH, maxH, stdH};
        });

    }

    public void updateData(LocalDateTime nowTime) {
        int[] start = {startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond()};
        weatherData.add(weather);
        int[] nowSeries = {nowTime.getYear(), nowTime.getMonthValue(), nowTime.getDayOfMonth(), nowTime.getHour(), nowTime.getMinute(), nowTime.getSecond()};
        nowTimeData.add(nowSeries);
        Data data = new Data(start, station.getMiasto(), nowTimeData, weatherData, station.getUnits());
        Data.saveJSON(data, dataFile);
    }

    @Override
    public void update(Observable weatherController, Object weather) {
        WeatherConnectionDelayController con = (WeatherConnectionDelayController) weatherController;
        switch (con.getConnectionResult()) {
            case 0: {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "??").showAndWait());
                break;
            }
            case 1: {
                this.weather = (Weather) weather;
                LocalDateTime nowTime = LocalDateTime.now();
                Platform.runLater(() -> {
                    actualizeDataSeries(nowTime, (Weather) weather);
                    actualizeCharts();
                });
                updateData(nowTime);
                break;
            }
            case -1: {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "bad url").showAndWait());
                break;
            }
            case -2: {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Błąd połączenia").showAndWait());
                break;
            }


        }

    }

    private double[] calculateStatistics() {
        double liczbaPomiar, minT, maxT, stdT, minP, maxP, stdP, minH, maxH, stdH;
        liczbaPomiar = temperature.getData().size();
        Comparator<XYChart.Data<Number, Number>> valuesY = Comparator.comparingDouble(o -> (double) o.getYValue());
        temperature.getData().sort(valuesY);
        pressure.getData().sort(valuesY);
        humidity.getData().sort(valuesY);
        minT = (double) temperature.getData().get(0).getYValue();
        maxT = (double) temperature.getData().get((int) liczbaPomiar - 1).getYValue();
        stdT = calcSTD(temperature);
        minP = (double) pressure.getData().get(0).getYValue();
        maxP = (double) pressure.getData().get((int) liczbaPomiar - 1).getYValue();
        stdP = calcSTD(pressure);
        minH = (double) humidity.getData().get(0).getYValue();
        maxH = (double) humidity.getData().get((int) liczbaPomiar - 1).getYValue();
        stdH = calcSTD(humidity);
        double[] statistics = {liczbaPomiar, minT, maxT, stdT, minP, maxP, stdP, minH, maxH, stdH};
        return statistics;
    }

    private double calcSTD(javafx.scene.chart.XYChart.Series<Number, Number> series) {
        double sum = 0;
        for (int i = 0; i < series.getData().size(); i++) {
            sum = sum + (double) series.getData().get(i).getYValue();
        }
        double mean = sum / series.getData().size();
        double varSum = 0;
        for (int i = 0; i < series.getData().size(); i++) {
            varSum = varSum + Math.pow((double) series.getData().get(i).getYValue() - mean, 2);
        }

        return Math.sqrt(varSum / series.getData().size());
    }


}