package calculator.listeners.data;

import java.math.BigInteger;

import lombok.Getter;

@Getter
public class NominatorCalculationResult extends CalculationResult<BigInteger> {
    private int iterationIndex;

    public NominatorCalculationResult(BigInteger result, int iterationIndex) {
        super(result);
        this.iterationIndex = iterationIndex;
    }
}
