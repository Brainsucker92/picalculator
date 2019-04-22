package calculator;

import java.math.BigDecimal;
import java.math.MathContext;

public interface PiCalculator {

    BigDecimal calculate(int iterations);

    BigDecimal calculate(int iterations, MathContext precision);
}
