package calculator;

import java.math.BigDecimal;

public interface PiCalculatorListener {

    void notifyIterationCompleted(int index, BigDecimal result);
}
