package start;

import calculator.ChudnovskyCalculator;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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

        MathContext context = new MathContext(50000, RoundingMode.DOWN);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();

        JButton button = new JButton();
        button.setText("Calculate");

        JLabel label = new JLabel();
        label.setText("Text");
        button.addActionListener(e -> {
            label.setText("Calculating");
            Thread t = new Thread(() -> {
                BigDecimal chudnovskyPi = calculator.chudnovsky(100, context);
                label.setText("Result: " + chudnovskyPi);
                System.out.println(chudnovskyPi);
            });
            t.setName("Verrueckter-Mongo");
            t.start();
        });

        panel.add(button);
        panel.add(label);
        frame.add(panel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                service.shutdown();
                try {
                    service.awaitTermination(15, TimeUnit.MINUTES);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}
