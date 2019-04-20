package start;

import calculator.ChudnovskyCalculator;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

    private void start() {
        ExecutorService service = Executors.newCachedThreadPool();
        ChudnovskyCalculator calculator = new ChudnovskyCalculator();
        calculator.setExecutorService(service);

        BigDecimal chudnovskyPi = calculator.chudnovsky(3000);
        System.out.println(chudnovskyPi);
        // System.out.println(Math.PI);

        service.shutdown();
        try {
            service.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
