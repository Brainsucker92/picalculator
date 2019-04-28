package calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class BaileyBorweinPlouffeCalculator implements AsyncPiCalculator, PiCalculator {

    private ExecutorService service;

    public BaileyBorweinPlouffeCalculator(ExecutorService service) {
        this.service = service;
    }

    @Override
    public CompletableFuture<BigDecimal> calculateAsync(int iterations) {
        MathContext context = new MathContext(20, RoundingMode.DOWN);
        return calculateAsync(iterations, context);
    }

    @Override
    public CompletableFuture<BigDecimal> calculateAsync(int iterations, MathContext precision) {
        return sumBBP(iterations, precision);

    }

    @Override
    public void setExecutorService(ExecutorService service) {
        this.service = service;
    }

    @Override
    public BigDecimal calculate(int iterations) {
        CompletableFuture<BigDecimal> future = calculateAsync(iterations);
        return future.join();

    }

    @Override
    public BigDecimal calculate(int iterations, MathContext precision) {
        CompletableFuture<BigDecimal> future = calculateAsync(iterations, precision);
        return future.join();
    }

    private CompletableFuture<BigDecimal> sumBBP(int k, MathContext context) {
        Optional<CompletableFuture<BigDecimal>> reduce = IntStream.rangeClosed(0, k)
                .mapToObj(i -> calculateBBP(i, context)).reduce((a, b) -> a.thenCombine(b, BigDecimal::add));
        return reduce.get();
    }

    private CompletableFuture<BigDecimal> calculateBBP(int k, MathContext context) {

        CompletableFuture<BigDecimal> t1Future = CompletableFuture.supplyAsync(() -> BigDecimal.valueOf(16).pow(k), service)
                .thenApply(i -> BigDecimal.ONE.divide(i, context));
        CompletableFuture<BigDecimal> t2Future = CompletableFuture.supplyAsync(() -> BigDecimal.valueOf(4)
                .divide(BigDecimal.valueOf(8 * k + 1), context), service);
        CompletableFuture<BigDecimal> t3Future = CompletableFuture.supplyAsync(() -> BigDecimal.valueOf(2)
                .divide(BigDecimal.valueOf(8 * k + 4), context), service);
        CompletableFuture<BigDecimal> t4Future = CompletableFuture.supplyAsync(() -> BigDecimal.valueOf(1)
                .divide(BigDecimal.valueOf(8 * k + 5), context), service);
        CompletableFuture<BigDecimal> t5Future = CompletableFuture.supplyAsync(() -> BigDecimal.valueOf(1)
                .divide(BigDecimal.valueOf(8 * k + 6), context), service);

        CompletableFuture<BigDecimal> diffFuture = t2Future.thenCombine(t3Future, BigDecimal::subtract)
                .thenCombine(t4Future, BigDecimal::subtract)
                .thenCombine(t5Future, BigDecimal::subtract);
        return t1Future.thenCombine(diffFuture, BigDecimal::multiply).thenApply(BigDecimal::stripTrailingZeros);
    }
}
