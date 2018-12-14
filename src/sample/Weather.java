package sample;

public class Weather {
    private double temp;
    private double pressure;
    private double humidity;
    private double temp_min;
    private double temp_max;

    public Weather(double temperatura, double cisnienie, double wilgotnosc, double temp_min, double temp_max) {
        this.temp = temperatura;
        this.pressure = cisnienie;
        this.humidity = wilgotnosc;
        this.temp_min = temp_min;
        this.temp_max = temp_max;
    }

    public Weather() {

    }

    public Weather(int connection) {

    }

    public double getTemperatura() {
        return temp;
    }

    public double getCisnienie() {
        return pressure;
    }

    public double getWilgotnosc() {
        return humidity;
    }

    public double getTemp_min() {
        return temp_min;
    }

    public double getTemp_max() {
        return temp_max;
    }

    @Override
    public String toString() {
        return "Temperatura: " + temp + ", Maksymalna: " + temp_max + ", Minimalna: " + temp_min + ", Ciśnienie: " + pressure + ", Wilgotność: " + humidity;
    }

}