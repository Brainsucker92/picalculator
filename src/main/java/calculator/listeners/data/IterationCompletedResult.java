package calculator.listeners.data;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class IterationCompletedResult {
    private BigDecimal result;
    private int iterationIndex;

    public IterationCompletedResult(BigDecimal result, int iterationIndex) {
        this.result = result;
        this.iterationIndex = iterationIndex;
    }
}