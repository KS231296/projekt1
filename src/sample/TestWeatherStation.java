package sample;

import java.util.Scanner;

public class TestWeatherStation {
    public static void main(String[] args) {
        boolean koniec = false;
        Scanner text = new Scanner(System.in);


        do {

            System.out.println("Podaj miasto");

            String miasto = text.nextLine();
            WeatherStation wMiescie = new WeatherStation(miasto);
            System.out.println(wMiescie.createWeatherObject().toString());
            WeatherConnectionDelayController controller = new WeatherConnectionDelayController(wMiescie, 5000);
            controller.run();

            System.out.println("Wyjsc? T/n");
            String wyjsc = text.nextLine();
            if (wyjsc.equals("T")) {
                koniec = true;
            }

        } while (!koniec);
    }

}
