package calculator.listeners.data;

import java.math.BigInteger;

import lombok.Getter;

@Getter
public class DenominatorCalculationResult extends CalculationResult<BigInteger> {
    protected int iterationIndex;

    public DenominatorCalculationResult(BigInteger result, int iterationIndex) {
        super(result);
        this.iterationIndex = iterationIndex;
    }
}
