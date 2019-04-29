package start;

import calculator.impl.BaileyBorweinPlouffeCalculator;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BBPMain {

    public static void main(String[] args) {
        BBPMain main = new BBPMain();
        main.start();
    }

    private void start() {
        ExecutorService service = Executors.newWorkStealingPool();
        BaileyBorweinPlouffeCalculator calc = new BaileyBorweinPlouffeCalculator(service);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MathContext context = new MathContext(100000, RoundingMode.DOWN);
        BigDecimal calculate = calc.calculate(100000, context);
        System.out.println(calculate);


        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                service.shutdown();
                try {
                    service.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        frame.setVisible(true);
    }
}
