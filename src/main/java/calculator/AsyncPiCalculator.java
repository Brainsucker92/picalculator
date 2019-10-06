package calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Indicator interface for an asynchronous PI calculator.
 * Calculations done by this interface will run concurrently.
 *
 * @author Stefan
 * @version 1.0
 */
public interface AsyncPiCalculator {

    CompletableFuture<BigDecimal> calculateAsync(int iterations);

    CompletableFuture<BigDecimal> calculateAsync(int iterations, MathContext precision);

    void setExecutorService(ExecutorService service);
}
