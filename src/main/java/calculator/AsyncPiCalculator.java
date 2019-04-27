package calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface AsyncPiCalculator {

    CompletableFuture<BigDecimal> calculateAsync(int iterations);

    CompletableFuture<BigDecimal> calculateAsync(int iterations, MathContext precision);

    void setExecutorService(ExecutorService service);
}
