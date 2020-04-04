package calculator.listeners.data;

import java.math.BigDecimal;

public class ConstantCalculationResult extends CalculationResult<BigDecimal> {
    public ConstantCalculationResult(BigDecimal result) {
        super(result);
    }
}
