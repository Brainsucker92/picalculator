package calculator.listeners.data;

import lombok.Getter;

@Getter
class CalculationResult<T> {
    protected T result;

    public CalculationResult(T result) {
        this.result = result;
    }
}
