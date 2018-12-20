package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class Controller implements Observer {

    private WeatherStation station = new WeatherStation();
    private boolean started = false;
    private boolean paused = false;
   private LocalDateTime startTime;

    private Weather weather;

    private Thread weatherUpdates = new Thread();



    private String miasto;
    private String units = "metric";
    private String info;


    private File dataFile = new File("data.json");

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
    private ContextMenu menuMiasta;


    @FXML
    private MenuItem itemsMIasta;

    @FXML
    private ChoiceBox<String> choiceTimeUnits;

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

    /**
     * ustawia rodzaj jednostek temperatury
     * @param event po kliknieciu radio button unitsMetric
     */
    @FXML
    void celsius(ActionEvent event) {
        units = "metric";

    }

    /**
     * ustawia rodzaj jednostek temperatury
     * @param event po kliknieciu radio button unitsImperial
     */
    @FXML
    void fahrenheit(ActionEvent event) {
        units = "imperial";
    }

    /**
     * przerywa watek pobierania danych pogodowych
     * @param event po kliknieciu btnInterrupt
     */
    @FXML
    void interrupt(ActionEvent event) {
        station.stop();
        weatherUpdates.interrupt();
        started = false;
        btnInterrupt.setDisable(true);
        btnStartPause.setText("Start");

    }

    /**
     * ustawia rodzaj jednostek temperatury
     * @param event po kliknieciu radio button unitsDefault
     */
    @FXML
    void kelvin(ActionEvent event) {
        units = "";
    }

    /**
     * Ladowanie danych z pliku
     * @param event po kliknieciu btnLoad
     */
    @FXML
    void loadData(ActionEvent event) {
        FileChooser choose = new FileChooser();
        choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("json", "*.json", "*.txt"));
        final File[] ddd = {new File("")};
        new Alert(Alert.AlertType.CONFIRMATION, "Czy chcesz załadować własny plik?", new ButtonType("tak", ButtonBar.ButtonData.YES), new ButtonType("załaduj z pamięci", ButtonBar.ButtonData.NO), new ButtonType("CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE)).showAndWait().ifPresent(response -> {
            switch (response.getButtonData()) {
                case CANCEL_CLOSE: {
                    ddd[0] = null;
                    break;
                }
                case NO: {
                    ddd[0] = dataFile;
                    interrupt(event); // przerwanie aktywnego wątku pobierania danych (jeśli wcześniej był uruchomiony)
                    break;
                }
                case YES: {
                    ddd[0] = choose.showOpenDialog(new Stage());
                    interrupt(event); // przerwanie aktywnego wątku pobierania danych (jeśli wcześniej był uruchomiony)
                    break;
                }
            }
        });  // wyświetlenie zapytania przy wybraniu własnego pliku otwiera się okno wyboru pliku lub załadowanie domyślnego pliku danych(ostatnio wykonywane pomiary), można również anulować akcje

        if (ddd[0] != null) {
            String path = ddd[0].toURI().toASCIIString();

            Data data = Data.readJSON(ddd[0]); //odczytanie danych z pliku JSON
            chartTemperature.getData().clear();
            chartHumidity.getData().clear();
            chartPressure.getData().clear();

            temperature.getData().clear();
            temp_min.getData().clear();
            temp_max.getData().clear();
            humidity.getData().clear();
            pressure.getData().clear();
            humidity.getData().clear();
            weatherData = new ArrayList<>();
            nowTimeData = new ArrayList<>();
// wyczyszczenie wykresów i serii danych

            units = data.getUnits();
            startTime = LocalDateTime.of(data.getStartTime()[0], data.getStartTime()[1], data.getStartTime()[2], data.getStartTime()[3], data.getStartTime()[4], data.getStartTime()[5]);
            miasto = data.getMiasto();
            nowTimeData = data.getNowTime();
            weatherData = data.getWeather();
            LocalDateTime endTime = LocalDateTime.of(nowTimeData.get(nowTimeData.size() - 1)[0], nowTimeData.get(nowTimeData.size() - 1)[1], nowTimeData.get(nowTimeData.size() - 1)[2], nowTimeData.get(nowTimeData.size() - 1)[3], nowTimeData.get(nowTimeData.size() - 1)[4], nowTimeData.get(nowTimeData.size() - 1)[5]);
// aktualizacja zmiennych
            info = String.format("Czas rozpoczęcia pomiarów: %s%nKoniec pomiarów: %s%nMiasto: %s%nJednostki: %s", startTime.toString(), endTime.toString(), miasto, units);

            for (int i = 0; i < weatherData.size(); i++) {
                LocalDateTime nowTime = LocalDateTime.of(nowTimeData.get(i)[0], nowTimeData.get(i)[1], nowTimeData.get(i)[2], nowTimeData.get(i)[3], nowTimeData.get(i)[4], nowTimeData.get(i)[5]);
                actualizeDataSeries(nowTime, weatherData.get(i));
            }
            actualizeCharts();
            new Alert(Alert.AlertType.INFORMATION, "loaded").showAndWait();
        }


    }

    /**
     * zapis danych do pliku z wyborem pliku zapisu. Dane są na bieżąco zapisywane do pliku domyślnego  - data.json
     * @param event
     */
    @FXML
    void saveData(ActionEvent event) {
        FileChooser choose = new FileChooser();
        choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("json", "*.json", "*.txt"));
        File dataFile = choose.showSaveDialog(new Stage()); // tworzenie okna do zapisu pliku
        if (dataFile != null) {
            String path = dataFile.toURI().toASCIIString();
            int[] start = {startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond()};

            Data data = new Data(start, station.getMiasto(), nowTimeData, weatherData, station.getUnits());
            Data.saveJSON(data, dataFile);
            new Alert(Alert.AlertType.INFORMATION, "saved").show(); // po wyświetleniu informacji o zapisie danych wątek połączenia jest kontynuowany
        }
    }


    @FXML
    void startPause(ActionEvent event) {
// do określenia którą metodę wybrać przy kliknięciu przycisku start/pause
        if (!started) {
            miasto = txtMiasto.getText();
            if (miasto.equals("")) {
                miasto = "London";
                new Alert(Alert.AlertType.WARNING, "Nie podano miast, ustawiono wartość domyślną. (London)").showAndWait();
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

    /**
     * rozpoczyna watek pobierania i zapisywania danych
     */
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
        weatherData = new ArrayList<>();
        nowTimeData = new ArrayList<>();
// zresetowanie danych wyświetlanych w aplikacji
        station.setMiasto(miasto);
        station.setUnits(units);
        int mnoznik = 0;
        switch (choiceTimeUnits.getValue()) {
            case "seconds": {
                mnoznik = 1000;
                break;
            }
            case "minutes": {
                mnoznik = 60 * 1000;
                break;
            }
            case "hours": {
                mnoznik = 60 * 60 * 1000;
                break;
            }
            case "days": {
                mnoznik = 24 * 60 * 60 * 1000;
                break;
            }

        }
// aktualizacja zmiennych
        int odswierzanie = 60000;
        if (txtOdswierzanie.getText().equals("")) {
            new Alert(Alert.AlertType.WARNING, "Nie podano wartości odświerzania, ustawiono wartość domyślną. (60s)").showAndWait();
        } else {
            try {
                odswierzanie = mnoznik * Integer.parseInt(txtOdswierzanie.getText());
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

        station.setInterval(odswierzanie);
        weatherUpdates = new Thread(station); // utworzenie nowego wątku
        station.addObserver(this);

        weatherUpdates.start(); // rozpoczęcie wątku pobierania informacji o pogodzie
        started = true;
        btnInterrupt.setDisable(false); // aktywacja przycisku przerwania
        btnStartPause.setText("wstrzymaj");
        startTime = LocalDateTime.now();
        info = String.format("Czas rozpoczęcia pomiarów: %s%nMiasto: %s%nJednostki: %s", startTime.toString(), miasto, units);
        areaStatistics.setText(info);
        txtMiasto.setText("");
        txtOdswierzanie.setText("");
    }

    private void resume() {
        weatherUpdates.resume();
        paused = false;
        btnInterrupt.setDisable(false); // aktywacja przycisku przerwania
        btnStartPause.setText("wstrzymaj");
    }

    private void pause() {
        weatherUpdates.suspend();
        paused = true;
        btnInterrupt.setDisable(true); // dezaktywacja przycisku przerwania
        btnStartPause.setText("wznów");
    }


    /**
     *
     * Metoda do aktualizacji serii danych
     * @param nowTime aktualny czas
     * @param weather aktualna pogoda
     */
    private void actualizeDataSeries(LocalDateTime nowTime, Weather weather) {
        Long now = Duration.between(startTime, nowTime).toSeconds();

        temperature.getData().add(new XYChart.Data<>(now, weather.getTemperatura()));
        temp_min.getData().add(new XYChart.Data<>(now, weather.getTemp_min()));
        temp_max.getData().add(new XYChart.Data<>(now, weather.getTemp_max()));
        humidity.getData().add(new XYChart.Data<>(now, weather.getWilgotnosc()));
        pressure.getData().add(new XYChart.Data<>(now, weather.getCisnienie()));
    }

    /**
     * Metoda aktualizowania wykresów
     */
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

    /**
     * Aktualizacja danych zapisanych w pliku domyślnym
     * @param nowTime
     */
    public void updateData(LocalDateTime nowTime) {
        int[] start = {startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond()};
        weatherData.add(weather);
        int[] nowSeries = {nowTime.getYear(), nowTime.getMonthValue(), nowTime.getDayOfMonth(), nowTime.getHour(), nowTime.getMinute(), nowTime.getSecond()};
        nowTimeData.add(nowSeries);
        Data data = new Data(start, station.getMiasto(), nowTimeData, weatherData, station.getUnits()); // utworzenie obiektu klasy przechowującej dane
        Data.saveJSON(data, dataFile); // zapis do pliku
    }

    /**
     * nadpisana metoda z interfejsu Observer
     * @param weatherStation
     * @param weather
     */
    @Override
    public void update(Observable weatherStation, Object weather) {
        WeatherStation station = (WeatherStation) weatherStation;
        switch (station.getConnectionResult()) {
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
                started = false;
                break;
            }
            case -2: {
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Błąd połączenia").showAndWait());
                break;
            }


        }

    }

    /**
     * metoda do obliczenia danych statystycznych
     * @return tablica wartości danych statystycznych
     */
    private double[] calculateStatistics() {
        double liczbaPomiar, minT, maxT, stdT, minP, maxP, stdP, minH, maxH, stdH;
        liczbaPomiar = temperature.getData().size();
        Comparator<XYChart.Data<Number, Number>> valuesY = Comparator.comparingDouble(o -> (double) o.getYValue());
        temperature.getData().sort(valuesY); // statystyka temperatury obliczana bez uwzględnienia wartości t_min i t_max pobieranych z serweru
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
        return new double[]{liczbaPomiar, minT, maxT, stdT, minP, maxP, stdP, minH, maxH, stdH};
    }

    /**
     * Metoda obliczająca odchylenie standardowe
     * @param series seria danych dla której obliczane jest odchylenie
     * @return
     */
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