package calculator.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

/**
 * Implements the Chudnovsky algorithm which calculates PI as an infinite sum.
 * This is a multi-threaded high performance iterative implementation shown at Wikipedia:
 * https://en.wikipedia.org/wiki/Chudnovsky_algorithm
 */
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
        MathContext context = new MathContext(20);
        return calculateAsync(iterations, context);
    }

    @Override
    public CompletableFuture<BigDecimal> calculateAsync(int iterations, MathContext precision) {
        if (iterations < 0) {
            throw new IllegalArgumentException();
        }
        return chudnovsky(iterations, precision);
    }

    /**
     * Calculates the factorial of a given number {@code n}
     *
     * @param n The number to calculate the factorial of
     * @return The factorial of the number as BigInteger
     */
    private BigInteger factorial(int n) {
        if (n <= 1) {
            return BigInteger.valueOf(1);
        }
        return IntStream.rangeClosed(2, n).mapToObj(BigInteger::valueOf)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    /**
     * The Chudnovsky algorithm to calculate PI.
     * The precision of the number can be set via the MathContext parameter.
     *
     * @param n       The number of iterations for the Chudnovsky sum.
     * @param context The mathematical context that will be applied to the result
     * @return A CompletableFuture, containing the result of the algorithm.
     */
    private CompletableFuture<BigDecimal> chudnovsky(int n, MathContext context) {

        CompletableFuture<BigDecimal> constant = chudnovskyConstant(context);
        CompletableFuture<BigDecimal> sum = chudnovskySum(n, context);
        return constant.thenCombine(sum, (bigDecimal, bigDecimal2) -> bigDecimal.divide(bigDecimal2, context));
    }

    /**
     * Calculates the k-th Chudnovsky number
     * This number is the k-th part of the infinite sum in the algorithm
     * The precision of the number can be set via the MathContext parameter.
     *
     * @param k       The index of the number you want to calculate. (>=0)
     * @param context The mathematical context that will be applied to the result
     * @return A CompletableFuture, containing the result as BidDecimal
     */
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

    /**
     * Calculates the sum of n Chudnovsky numbers.
     * The precision of the number cn be set via the MathContext parameter.
     *
     * @param n       The index of the last number to sum
     * @param context The mathematical context that will be applied to the result
     * @return A CompletableFuture, containing the result of the sum
     */
    private CompletableFuture<BigDecimal> chudnovskySum(int n, MathContext context) {
        return IntStream.rangeClosed(0, n).mapToObj(value -> chudnovskyNumber(value, context))
                .reduce((a, b) -> a.thenCombine(b, BigDecimal::add))
                .orElse(CompletableFuture.completedFuture(BigDecimal.ZERO));
    }

    /**
     * Calculates the constant part of the Chudnovsky algorithm to a given precision
     * The precision of the number cn be set via the MathContext parameter.
     *
     * @param context The mathematical context that will be applied to the result
     * @return The constant part of the Chudnovsky algorithm as CompletableFuture
     */
    private CompletableFuture<BigDecimal> chudnovskyConstant(MathContext context) {

        return CompletableFuture.supplyAsync(() -> new BigDecimal(number3).sqrt(context), service)
                .thenApply(i -> new BigDecimal(number4).multiply(i))
                .thenApply(BigDecimal::stripTrailingZeros);
    }
}
