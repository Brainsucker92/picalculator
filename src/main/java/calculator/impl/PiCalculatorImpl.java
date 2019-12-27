package calculator.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import calculator.AsyncPiCalculator;
import calculator.PiCalculator;
import calculator.PiCalculatorListener;
import calculator.SyncPiCalculator;

/**
 * Basic implementation for any sort of PI calculator
 *
 * @author Stefan
 * @see 1.0
 */
public abstract class PiCalculatorImpl implements PiCalculator {

    ExecutorService service;
    protected Set<PiCalculatorListener> listeners;

    PiCalculatorImpl(ExecutorService service) {
        this.service = service;
        listeners = new HashSet<>();
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

    @Override
    public void addListener(PiCalculatorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(PiCalculatorListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean hasListener(PiCalculatorListener listener) {
        return listeners.contains(listener);
    }

    /**
     * Call this method as soon as one iteration of the calculation has been completed. This method will invoke the
     * notifyIterationCompleted method on each listener.
     *
     * @param index  The index of the iteration that has been completed.
     * @param result The result of the iteration that has been completed.
     */
    void iterationCompleted(int index, BigDecimal result) {
        listeners.forEach(listener -> listener.notifyIterationCompleted(index, result));
    }
}
