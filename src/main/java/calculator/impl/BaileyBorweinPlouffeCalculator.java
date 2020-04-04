package calculator.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

/**
 * https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
 *
 * @author Stefan
 * @version 1.0
 */
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
        return IntStream.rangeClosed(0, k)
                        .mapToObj(i -> calculateBBP(i, context))
                        .reduce((f1, f2) -> f1.thenCombine(f2, BigDecimal::add)).orElse(CompletableFuture.completedFuture(BigDecimal.ZERO));
    }

    private CompletableFuture<BigDecimal> calculateBBP(int k, MathContext context) {

        CompletableFuture<BigDecimal> t0Future =
                CompletableFuture.supplyAsync(() -> BigDecimal.valueOf(16).pow(k), service)
                                 .thenApply(i -> BigDecimal.ONE.divide(i, context));
        CompletableFuture<BigInteger> t1Future =
                CompletableFuture.supplyAsync(() -> BigInteger.valueOf(8)
                                                              .multiply(BigInteger.valueOf(k)));
        CompletableFuture<BigDecimal> fraction1 =
                t1Future.thenApply(res -> calculateFraction(res, 4, 1, context));
        CompletableFuture<BigDecimal> fraction2 =
                t1Future.thenApply(res -> calculateFraction(res, 2, 4, context));
        CompletableFuture<BigDecimal> fraction3 =
                t1Future.thenApply(res -> calculateFraction(res, 1, 5, context));
        CompletableFuture<BigDecimal> fraction4 =
                t1Future.thenApply(res -> calculateFraction(res, 1, 6, context));

        CompletableFuture<BigDecimal> diffFuture = fraction1.thenCombine(fraction2, BigDecimal::subtract)
                                                            .thenCombine(fraction3, BigDecimal::subtract)
                                                            .thenCombine(fraction4, BigDecimal::subtract);

        CompletableFuture<BigDecimal> future = t0Future.thenCombine(diffFuture, BigDecimal::multiply)
                                                       .thenApply(BigDecimal::stripTrailingZeros);
        future.thenAccept(result -> this.iterationCompleted(k, result));
        return future;
    }

    private BigDecimal calculateFraction(BigInteger k, int nominator, int denominator, MathContext context) {
        return BigDecimal.valueOf(nominator).divide(new BigDecimal(k.add(BigInteger.valueOf(denominator))), context);
    }
}
