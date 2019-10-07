package start;

import calculator.PiCalculator;
import calculator.impl.ChudnovskyCalculator;

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

    private PiCalculator calculator;
    ExecutorService service;

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

    // TODO needs some clean up. Quick and dirty testing method.
    private void start() {
        // service = Executors.newFixedThreadPool(4);
        // service = Executors.newCachedThreadPool();
        initExecutorService();
        calculator = new ChudnovskyCalculator(service);
        //calculator = new BaileyBorweinPlouffeCalculator(service);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("PI Calculator");

        JPanel panel = new JPanel();

        JButton button = new JButton();
        button.setText("Calculate");

        JButton abortButton = new JButton();
        abortButton.setText("Abort");
        abortButton.setEnabled(false);

        JTextField iterationsField = new JTextField(10);
        iterationsField.setEditable(true);
        iterationsField.setToolTipText("Number of iterations");

        JTextField requiredIterationsField = new JTextField(10);
        requiredIterationsField.setEditable(false);
        requiredIterationsField.setToolTipText("Required iterations");

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
                    button.setEnabled(false);
                    int iterations = Integer.parseInt(text);
                    int digits = Integer.parseInt(digitsText);

                    if (calculator instanceof ChudnovskyCalculator) {
                        ChudnovskyCalculator chudCalc = (ChudnovskyCalculator) calculator;
                        int numIterations = chudCalc.getNumIterations(digits);
                        requiredIterationsField.setText(String.valueOf(numIterations));
                    } else {
                        requiredIterationsField.setText("UNKNOWN");
                    }

                    MathContext context = new MathContext(digits, RoundingMode.HALF_UP);
                    CompletableFuture<BigDecimal> chudnovskyPi = calculator.calculateAsync(iterations, context);
                    abortButton.setEnabled(true);
                    chudnovskyPi.thenAccept(bigDecimal -> {
                        String decString = bigDecimal.toString();
                        label.setText("Result: " + decString);
                        button.setEnabled(true);
                        abortButton.setEnabled(false);
                        System.out.println(decString);
                    });
                }
            };
            r.run();
        });

        abortButton.addActionListener(e -> {
            System.out.println("Aborting...");
            service.shutdownNow();
            abortButton.setEnabled(false);
            initExecutorService();
            calculator.setExecutorService(service);
            label.setText("Aborted");
            button.setEnabled(true);
        });

        panel.add(button);
        panel.add(iterationsField);
        panel.add(digitsField);
        panel.add(requiredIterationsField);
        panel.add(abortButton);
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

    private void initExecutorService() {
        service = Executors.newWorkStealingPool();
    }
}
