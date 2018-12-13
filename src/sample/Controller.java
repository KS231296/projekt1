package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Observable;
import java.util.Observer;

import static java.util.Objects.isNull;

public class Controller implements Observer {

    double now;
    boolean started = false;
    boolean paused = false;

    private Weather weather;
    private Observable weatherController;

    private Thread weatherUpdates = new Thread();

    WeatherConnectionDelayController connectionDelayController;

    private String miasto;
    private String units = "metric";


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
    //private LineChart<String, Number> chartTemperature;
    private LineChart<Number, Number> chartTemperature;

    @FXML
    private AreaChart<Number, Number> chartPressure;

    @FXML
    private AreaChart<Number, Number> chartHumidity;

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

    }

    @FXML
    void saveData(ActionEvent event) {

    }

    @FXML
    void startPause(ActionEvent event) {

        if (!started) {
            miasto = txtMiasto.getText();
            if (miasto.equals("")) {
                txtMiasto.setText("Wroclaw");
                miasto = "Wroclaw";
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
        System.out.println("starting");
        started = true;
        now = 0;

        chartTemperature.getData().removeAll(chartTemperature.getData());
        chartHumidity.getData().removeAll(chartHumidity.getData());
        chartPressure.getData().removeAll(chartPressure.getData());

        temperature.setName("temperatura");
        temp_max.setName("tmp max");
        temp_min.setName("tmp min");

        WeatherStation station = new WeatherStation(miasto, units);
        int odswierzanie = 5000;
        if (txtOdswierzanie.getText().equals("")) {
            new Alert(Alert.AlertType.ERROR, "Nie podano wartości odświerzania, ustawiono wartość domyślną. (60s)").showAndWait();
        } else {
            odswierzanie = 1000 * Integer.parseInt(txtOdswierzanie.getText());
        }
        if (odswierzanie < 15000) {
            odswierzanie = 5000;
            new Alert(Alert.AlertType.ERROR, "Zbyt mała wartość odświerzania, ustawiono wartość domyślną. (60s)").showAndWait();
        }

        connectionDelayController = new WeatherConnectionDelayController(station, odswierzanie);
        weatherUpdates = new Thread(connectionDelayController);
        connectionDelayController.addObserver(this);

        weatherUpdates.start();
    }

    private void resume() {
        System.out.println("resuming");

        weatherUpdates.start();
        paused = false;
    }

    private void pause() {
        System.out.println("pausing");
        connectionDelayController.stop();
        paused = true;
    }

    public void actualizeCharts() {
        // String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        // now = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM")) + " " + now;


        temperature.getData().add(new XYChart.Data<>(now, weather.getTemperatura()));
        System.out.println(temperature.getData().toString());
        temp_min.getData().add(new XYChart.Data<>(now, weather.getTemp_min()));
        temp_max.getData().add(new XYChart.Data<>(now, weather.getTemp_max()));
        humidity.getData().add(new XYChart.Data<>(now, weather.getWilgotnosc()));
        pressure.getData().add(new XYChart.Data<>(now, weather.getCisnienie()));

        Platform.runLater(() -> {
            chartTemperature.getData().addAll(temperature);
            chartTemperature.getData().addAll(temp_min);
            chartTemperature.getData().addAll(temp_max);
            System.out.println("added:");
            System.out.println(chartTemperature.getData().toString());
            chartHumidity.getData().add(humidity);
            chartPressure.getData().add(pressure);
        });

        now = now + 0.5;

    }

    @Override
    public void update(Observable weatherController, Object weather) {

        this.weather = (Weather) weather;
        Platform.runLater(() -> actualizeCharts());
        System.out.println("Controller Weather updated: " + weather.toString());
    }
}
