package calculator.listeners;

import calculator.listeners.data.IterationCompletedResult;

public interface PiCalculatorListener {

    void notifyIterationCompleted(IterationCompletedResult calculationResult);
}