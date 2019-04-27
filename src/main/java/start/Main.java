package start;

import calculator.ChudnovskyCalculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
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
        //service = Executors.newFixedThreadPool(4); // 1.
        // service = Executors.newCachedThreadPool(); // 2.
        service = Executors.newWorkStealingPool(); // 3.
        ChudnovskyCalculator calculator = new ChudnovskyCalculator(service);


        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("PI Calculator");

        JPanel panel = new JPanel();

        JButton button = new JButton();
        button.setText("Calculate");

        JTextField iterationsField = new JTextField(10);
        iterationsField.setEditable(true);
        iterationsField.setToolTipText("Number of iterations");

        JTextField digitsField = new JTextField(10);
        digitsField.setToolTipText("Number of digits");
        digitsField.setEditable(true);

        JLabel label = new JLabel();
        label.setText("Text");
        button.addActionListener(e -> {
            Runnable r = () -> {

                // Better option: disable button if input is invalid
                boolean error = false;
                String text = iterationsField.getText();
                if (text.length() == 0) {
                    iterationsField.setBackground(Color.RED);
                    error = true;
                } else {
                    iterationsField.setBackground(Color.WHITE);
                }
                String digitsText = digitsField.getText();
                if (digitsText.length() == 0) {
                    digitsField.setBackground(Color.RED);
                    error = true;
                } else {
                    digitsField.setBackground(Color.WHITE);
                }

                if (error) {
                    label.setText("Please enter valid values.");
                } else {
                    label.setText("Calculating");
                    int iterations = Integer.parseInt(text);
                    int digits = Integer.valueOf(digitsText);

                    MathContext context = new MathContext(digits, RoundingMode.DOWN);
                    CompletableFuture<BigDecimal> chudnovskyPi = calculator.calculateAsync(iterations, context);
                    chudnovskyPi.thenAccept(bigDecimal -> {
                        String decString = bigDecimal.toString();
                        label.setText("Result: " + decString);
                        System.out.println(decString);
                    });
                }
            };

            //Thread t = new Thread(r);
            //t.start(); // 1.
            // service.submit(r); // 2.
            r.run(); // 3.
        });

        panel.add(button);
        panel.add(iterationsField);
        panel.add(digitsField);
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
