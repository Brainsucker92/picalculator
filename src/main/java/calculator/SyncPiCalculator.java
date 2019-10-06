package calculator;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Indicator interface for an synchronous PI calculator.
 * Calculations done by this interface will run sequentially.
 *
 * @author Stefan
 * @version 1.0
 */
public interface SyncPiCalculator {

    BigDecimal calculate(int iterations);

    BigDecimal calculate(int iterations, MathContext precision);
}
