package calculator.listeners;

import calculator.listeners.data.ConstantCalculationResult;
import calculator.listeners.data.DenominatorCalculationResult;
import calculator.listeners.data.IterationCompletedResult;
import calculator.listeners.data.NominatorCalculationResult;

public class ChudnovskyCalculatorAdapter implements ChudnovskyCalculatorListener {
    @Override
    public void notifyIterationCompleted(IterationCompletedResult calculationResult) {
    }

    @Override
    public void notifyDenominatorCalculationCompleted(DenominatorCalculationResult result) {
    }

    @Override
    public void notifyNominatorCalculationCompleted(NominatorCalculationResult result) {
    }

    @Override
    public void notifyConstantCalculationCompleted(ConstantCalculationResult result) {
    }
}
