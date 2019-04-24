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
        ExecutorService service;
        // service = Executors.newFixedThreadPool(4); // 1.
        //service = Executors.newCachedThreadPool(); // 2.
        service = Executors.newWorkStealingPool(); // 3.
        ChudnovskyCalculator calculator = new ChudnovskyCalculator();
        calculator.setExecutorService(service);

        MathContext context = new MathContext(50000, RoundingMode.DOWN);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();

        JButton button = new JButton();
        button.setText("Calculate");

        JTextField field = new JTextField();
        field.setEditable(true);

        JLabel label = new JLabel();
        label.setText("Text");
        button.addActionListener(e -> {
            label.setText("Calculating");
            Runnable r = () -> {
                String text = field.getText();
                int iterations = Integer.parseInt(text);
                BigDecimal chudnovskyPi = calculator.calculate(iterations, context);
                label.setText("Result: " + chudnovskyPi);
                System.out.println(chudnovskyPi);
            };
            Thread t = new Thread(r);

            //t.start(); // 1.
            service.submit(r); // 2.
            //r.run(); // 3.
        });

        panel.add(button);
        panel.add(field);
        panel.add(label);
        frame.add(panel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                service.shutdown();
                try {
                    service.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}
