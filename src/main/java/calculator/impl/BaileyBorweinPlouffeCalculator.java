package calculator.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class BaileyBorweinPlouffeCalculator extends PiCalculatorImpl {

    public BaileyBorweinPlouffeCalculator(ExecutorService service) {
        super(service);
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

    private CompletableFuture<BigDecimal> sumBBP(int k, MathContext context) {
        return IntStream.rangeClosed(0, k).mapToObj(i -> calculateBBP(i, context))
                .reduce((a, b) -> a.thenCombine(b, BigDecimal::add))
                .orElse(CompletableFuture.completedFuture(BigDecimal.ZERO));
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
