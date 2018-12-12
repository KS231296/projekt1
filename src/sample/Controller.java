package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class Controller {

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
    private ToggleGroup units;

    @FXML
    private RadioButton unitsImperial;

    @FXML
    private RadioButton unitsDefault;

    @FXML
    private TextArea areaStatistics;

    @FXML
    private Button btnSave;

    @FXML
    private LineChart<Number, Number> chartTemperature;

    @FXML
    private AreaChart<Number, Number> chartPressure;

    @FXML
    private AreaChart<Number, Number> chartHumidity;

    @FXML
    void celsius(ActionEvent event) {

    }

    @FXML
    void farenheit(ActionEvent event) {

    }

    @FXML
    void interrupt(ActionEvent event) {

    }

    @FXML
    void kelvin(ActionEvent event) {

    }

    @FXML
    void loadData(ActionEvent event) {

    }

    @FXML
    void saveData(ActionEvent event) {

    }

    @FXML
    void startPause(ActionEvent event) {

    }

}
