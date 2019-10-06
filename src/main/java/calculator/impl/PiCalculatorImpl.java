package calculator.impl;

import calculator.AsyncPiCalculator;
import calculator.PiCalculator;
import calculator.SyncPiCalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Basic implementation for any sort of PI calculator
 *
 * @author Stefan
 * @see 1.0
 */
public abstract class PiCalculatorImpl implements PiCalculator {

    ExecutorService service;

    PiCalculatorImpl(ExecutorService service) {
        this.service = service;
    }

    @Override
    public void setExecutorService(ExecutorService service) {
        this.service = service;
    }

    @Override
    public BigDecimal calculate(int iterations) {
        try {
            return calculateAsync(iterations).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    @Override
    public BigDecimal calculate(int iterations, MathContext precision) {
        try {
            return calculateAsync(iterations, precision).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    @Override
    public AsyncPiCalculator parallel() {
        return this;
    }

    @Override
    public SyncPiCalculator sequential() {
        return this;
    }
}
