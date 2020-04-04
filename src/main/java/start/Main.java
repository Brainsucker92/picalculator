package start;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import calculator.PiCalculator;
import calculator.impl.ChudnovskyCalculator;
import calculator.listeners.PiCalculatorEventProvider;
import calculator.listeners.PiCalculatorListener;
import calculator.tools.PrecisionProvider;

public class Main {

    private PiCalculator calculator;
    private ExecutorService service;

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

    // TODO needs some clean up. Quick and dirty testing method.
    private void start() {
        // service = Executors.newFixedThreadPool(4);
        // service = Executors.newCachedThreadPool();
        this.service = createExecutorService();
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

        JTextField correctDigitsField = new JTextField(10);
        correctDigitsField.setEditable(false);
        correctDigitsField.setToolTipText("Correct digits");

        JTextField digitsField = new JTextField(10);
        digitsField.setToolTipText("Number of digits");
        digitsField.setEditable(true);

        PiCalculatorListener listener = calculationResult -> {
            int iterationIndex = calculationResult.getIterationIndex();
            System.out.println("Completed Iteration: " + iterationIndex);
        };

        if (calculator instanceof PiCalculatorEventProvider) {
            PiCalculatorEventProvider eventProvider = (PiCalculatorEventProvider) calculator;
            eventProvider.addListener(listener);
        }

        JLabel label = new JLabel();
        label.setText("Text");
        button.addActionListener(e -> {
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

                if (calculator instanceof PrecisionProvider) {
                    PrecisionProvider provider = (PrecisionProvider) calculator;
                    int numIterations = provider.getPrecision(iterations);
                    correctDigitsField.setText(String.valueOf(numIterations));
                } else {
                    correctDigitsField.setText("UNKNOWN");
                }

                MathContext context = new MathContext(digits, RoundingMode.HALF_EVEN);
                CompletableFuture<BigDecimal> future = calculator.calculateAsync(iterations, context);
                abortButton.setEnabled(true);
                future.thenAccept(bigDecimal -> {
                    String decString = bigDecimal.toString();
                    label.setText("Result: " + decString);
                    button.setEnabled(true);
                    abortButton.setEnabled(false);
                    System.out.println(decString);
                });
            }
        });

        abortButton.addActionListener(e -> {
            // TODO use CompletableFuture::cancel instead
            System.out.println("Aborting...");
            service.shutdownNow();
            abortButton.setEnabled(false);
            ExecutorService executorService = createExecutorService();
            calculator.setExecutorService(executorService);
            label.setText("Aborted");
            button.setEnabled(true);
        });

        panel.add(button);
        panel.add(iterationsField);
        panel.add(digitsField);
        panel.add(correctDigitsField);
        panel.add(abortButton);
        panel.add(label);
        frame.add(panel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                service.shutdown();
                try {
                    label.setText("Terminating application");
                    boolean terminated = service.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

    private ExecutorService createExecutorService() {
        ExecutorService service;
        service = Executors.newWorkStealingPool();
        // service = Executors.newSingleThreadExecutor();
        // service = Executors.newFixedThreadPool(8);
        // service = Executors.newCachedThreadPool();
        return service;
    }
}
