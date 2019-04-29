package calculator.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class ChudnovskyCalculator extends PiCalculatorImpl {

    private static final BigInteger number0 = BigInteger.valueOf(545140134);
    private static final BigInteger number1 = BigInteger.valueOf(-262537412640768000L);
    private static final BigInteger number2 = BigInteger.valueOf(13591409);
    private static final BigInteger number3 = BigInteger.valueOf(10005);
    private static final BigInteger number4 = BigInteger.valueOf(426880);

    public ChudnovskyCalculator(ExecutorService service) {
        super(service);
    }

    public CompletableFuture<BigDecimal> calculateAsync(int iterations) {
        MathContext context = new MathContext(20, RoundingMode.DOWN);
        return calculateAsync(iterations, context);
    }

    @Override
    public CompletableFuture<BigDecimal> calculateAsync(int iterations, MathContext precision) {
        if (iterations < 0) {
            throw new IllegalArgumentException();
        }
        return chudnovsky(iterations, precision);
    }

    private BigInteger factorial(int n) {
        if (n <= 1) {
            return BigInteger.valueOf(1);
        }
        return IntStream.rangeClosed(2, n).mapToObj(BigInteger::valueOf)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    private CompletableFuture<BigDecimal> chudnovsky(int n, MathContext context) {

        CompletableFuture<BigDecimal> constant = chudnovskyConstant(context);
        CompletableFuture<BigDecimal> sum = chudnovskySum(n, context);
        return constant.thenCombine(sum, (bigDecimal, bigDecimal2) -> bigDecimal.divide(bigDecimal2, context));
    }

    private CompletableFuture<BigDecimal> chudnovskyNumber(int k, MathContext context) {

        BigInteger kBigInt = BigInteger.valueOf(k);

        CompletableFuture<BigInteger> future0 = CompletableFuture.supplyAsync(() -> 6 * k, service)
                .thenApply(this::factorial);
        CompletableFuture<BigInteger> future1 = CompletableFuture.supplyAsync(() -> number0.multiply(kBigInt), service)
                .thenApply(i -> i.add(number2));
        CompletableFuture<BigInteger> future2 = CompletableFuture.supplyAsync(() -> 3 * k, service)
                .thenApply(this::factorial);
        CompletableFuture<BigInteger> future3 = CompletableFuture.supplyAsync(() -> factorial(k), service)
                .thenApply(i -> i.pow(3));
        CompletableFuture<BigInteger> future4 = CompletableFuture.supplyAsync(() -> number1.pow(k), service);

        CompletableFuture<BigInteger> nom = future0.thenCombine(future1, BigInteger::multiply);
        CompletableFuture<BigInteger> denom = future2.thenCombine(future3, BigInteger::multiply)
                .thenCombine(future4, BigInteger::multiply);

        CompletableFuture<BigDecimal> future = nom.thenCombine(denom, (bigInteger, bigInteger2) -> new BigDecimal(bigInteger)
                .divide(new BigDecimal(bigInteger2), context)
                .stripTrailingZeros());
        return future;
    }

    private CompletableFuture<BigDecimal> chudnovskySum(int n, MathContext context) {
        return IntStream.rangeClosed(0, n).mapToObj(value -> chudnovskyNumber(value, context))
                .reduce((a, b) -> a.thenCombine(b, BigDecimal::add))
                .orElse(CompletableFuture.completedFuture(BigDecimal.ZERO));
    }

    private CompletableFuture<BigDecimal> chudnovskyConstant(MathContext context) {

        return CompletableFuture.supplyAsync(() -> new BigDecimal(number3).sqrt(context), service)
                .thenApply(i -> new BigDecimal(number4).multiply(i))
                .thenApply(BigDecimal::stripTrailingZeros);
    }
}