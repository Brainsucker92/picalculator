package calculator.listeners;

import calculator.listeners.data.ConstantCalculationResult;
import calculator.listeners.data.DenominatorCalculationResult;
import calculator.listeners.data.NominatorCalculationResult;

public interface ChudnovskyCalculatorListener extends PiCalculatorListener {
    void notifyDenominatorCalculationCompleted(DenominatorCalculationResult result);

    void notifyNominatorCalculationCompleted(NominatorCalculationResult result);

    void notifyConstantCalculationCompleted(ConstantCalculationResult result);
}
