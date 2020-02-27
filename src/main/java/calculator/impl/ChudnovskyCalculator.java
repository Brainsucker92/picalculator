package calculator.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import calculator.PiCalculatorListener;
import factorial.FactorialCalculator;
import factorial.impl.GuavaFactorialCalculator;
import factorial.impl.MemoizeFactorialCalculator;

/**
 * Implements the Chudnovsky algorithm which calculates PI as an infinite sum. This is a multi-threaded high performance
 * iterative implementation shown at Wikipedia: https://en.wikipedia.org/wiki/Chudnovsky_algorithm
 *
 * @author Stefan
 * @version 1.0
 */
public class ChudnovskyCalculator extends PiCalculatorImpl {

    private static final BigInteger number0 = BigInteger.valueOf(545140134);
    private static final BigInteger number1 = BigInteger.valueOf(-262537412640768000L);
    private static final BigInteger number2 = BigInteger.valueOf(13591409);
    private static final BigInteger number3 = BigInteger.valueOf(10005);
    private static final BigInteger number4 = BigInteger.valueOf(426880);

    private final FactorialCalculator<BigInteger> factorialCalculator;

    public ChudnovskyCalculator(ExecutorService service) {
        super(service);
        FactorialCalculator<BigInteger> calculator = new GuavaFactorialCalculator();
        this.factorialCalculator = new MemoizeFactorialCalculator<>(calculator);
    }

    public ChudnovskyCalculator(ExecutorService service, FactorialCalculator<BigInteger> calculator) {
        super(service);
        this.factorialCalculator = calculator;
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
     * Calculates the number of iterations required for the specified level of precision. More information:
     * https://mathoverflow.net/q/261162/146822
     *
     * @param precision The level of precision you want to calculate.
     * @return The number of iterations required
     */
    public int getNumIterations(int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("precision argument must be >= 0");
        }
        long l = 151931373056000L;
        double precisionPerIteration = Math.log10(l);
        return (int) (precision / precisionPerIteration);
    }

    /**
     * Returns the number of digits that can be calculated precisely with a given amount of iterations.
     *
     * @param iterations The number of iterations you want to calculate
     * @return The number of digits that will be correct
     */
    public int getPrecision(int iterations) {
        if (iterations < 0) {
            throw new IllegalArgumentException("iterations argument must be >= 0");
        }
        long l = 151931373056000L;
        double precisionPerIteration = Math.log10(l);
        return (int) ((iterations + 1) * precisionPerIteration);
    }

    /**
     * The Chudnovsky algorithm to calculate PI. The desired precision of the number can be set via the MathContext
     * parameter.
     *
     * @param n       The number of iterations for the Chudnovsky sum.
     * @param context The mathematical context that will be applied to the result
     * @return A CompletableFuture, containing the result of the algorithm.
     */
    private CompletableFuture<BigDecimal> chudnovsky(int n, MathContext context) {

        CompletableFuture<BigDecimal> constant = chudnovskyConstantAsync(context);
        CompletableFuture<BigDecimal> sum = chudnovskySumAsync(n, context);
        return constant.thenCombine(sum, (bigDecimal, bigDecimal2) -> bigDecimal.divide(bigDecimal2, context));
    }

    /**
     * Calculates the k-th Chudnovsky number This number is the k-th part of the infinite sum in the algorithm The
     * precision of the number can be set via the MathContext parameter.
     *
     * @param k       The index of the number you want to calculate. (>=0)
     * @param context The mathematical context that will be applied to the result
     * @return A CompletableFuture, containing the result as BigDecimal
     */
    private CompletableFuture<BigDecimal> chudnovskyNumberAsync(int k, MathContext context) {

        CompletableFuture<BigInteger> nominator = calculateNominatorAsync(k);
        CompletableFuture<BigInteger> denominator = calculateDenominatorAsync(k);

        @SuppressWarnings("unused")
        CompletableFuture<BigDecimal> future = nominator.thenCombine(denominator,
                (bigInteger, bigInteger2) -> new BigDecimal(bigInteger)
                        // Unfortunately this operation cannot be made more concurrently.
                        .divide(new BigDecimal(bigInteger2), context)
                        .stripTrailingZeros());
        future.thenAccept(result -> iterationCompleted(k, result));
        return future;
    }

    /**
     * Calculates the nominator of the Chudnovsky infinite sum
     *
     * @param k The index of the number you want to calculate. (>=0)
     * @return A CompletableFuture, containing the result as BigInteger
     */
    private CompletableFuture<BigInteger> calculateNominatorAsync(int k) {
        BigInteger kBigInt = BigInteger.valueOf(k);

        CompletableFuture<BigInteger> future0 =
                CompletableFuture.supplyAsync(() -> 6 * k, service)
                                 .thenApply(factorialCalculator::factorial);
        CompletableFuture<BigInteger> future1 =
                CompletableFuture.supplyAsync(() -> number0.multiply(kBigInt), service)
                                 .thenApply(i -> i.add(number2));
        @SuppressWarnings("unused")
        CompletableFuture<BigInteger> nominator = future0.thenCombine(future1, BigInteger::multiply);
        nominator.thenAccept(result -> listeners.stream()
                                                .filter(listener -> listener instanceof ChudnovskyCalculatorListener)
                                                .forEach(listener ->
                                                        ((ChudnovskyCalculatorListener) listener).notifyNominatorCalculationCompleted(k, result)));
        return nominator;
    }

    /**
     * Calculates the denominator of the Chudnovsky infinite sum
     *
     * @param k The index of the number you want to calculate. (>=0)
     * @return A CompletableFuture, containing the result as BigInteger
     */
    private CompletableFuture<BigInteger> calculateDenominatorAsync(int k) {
        CompletableFuture<BigInteger> future2 = CompletableFuture.supplyAsync(() -> 3 * k, service)
                                                                 .thenApply(factorialCalculator::factorial);
        CompletableFuture<BigInteger> future3 = CompletableFuture.supplyAsync(() -> factorialCalculator.factorial(k), service)
                                                                 .thenApply(i -> i.pow(3));
        CompletableFuture<BigInteger> future4 = CompletableFuture.supplyAsync(() -> number1.pow(k), service);

        @SuppressWarnings("unused")
        CompletableFuture<BigInteger> denominator = future2.thenCombine(future3, BigInteger::multiply)
                                                           .thenCombine(future4, BigInteger::multiply);
        denominator.thenAccept(result -> listeners.stream()
                                                  .filter(listener -> listener instanceof ChudnovskyCalculatorListener)
                                                  .forEach(listener ->
                                                          ((ChudnovskyCalculatorListener) listener).notifyDenominatorCalculationCompleted(k, result)));
        return denominator;
    }

    /**
     * Calculates the sum of n Chudnovsky numbers. The precision of the number can be set via the MathContext
     * parameter.
     *
     * @param n       The index of the last number to sum
     * @param context The mathematical context that will be applied to the result
     * @return A CompletableFuture, containing the result of the sum
     */
    private CompletableFuture<BigDecimal> chudnovskySumAsync(int n, MathContext context) {
        Stream<CompletableFuture<BigDecimal>> futureStream = IntStream.rangeClosed(0, n)
                                                                      .parallel()
                                                                      .mapToObj(value -> chudnovskyNumberAsync(value, context));
        CompletableFuture<BigDecimal>[] futures = futureStream.toArray(CompletableFuture[]::new);
        CompletableFuture<Void> streamFuture = CompletableFuture.allOf(futures);

        return streamFuture.thenCompose(v -> Stream.of(futures)
                                                   .parallel()
                                                   .reduce(CompletableFuture.completedFuture(BigDecimal.ZERO),
                                                           (a, b) -> a.thenCombine(b, BigDecimal::add)));
    }

    /**
     * Calculates the constant part of the Chudnovsky algorithm to a given precision. The precision of the number can be
     * set via the MathContext parameter.
     *
     * @param context The mathematical context that will be applied to the result
     * @return The constant part of the Chudnovsky algorithm as CompletableFuture
     */
    private CompletableFuture<BigDecimal> chudnovskyConstantAsync(MathContext context) {
        CompletableFuture<BigDecimal> constant = CompletableFuture.supplyAsync(() -> new BigDecimal(number3).sqrt(context), service)
                                                                  .thenApply(i -> new BigDecimal(number4).multiply(i))
                                                                  .thenApply(BigDecimal::stripTrailingZeros);

        constant.thenAccept(result -> listeners.stream().filter(listener -> listener instanceof ChudnovskyCalculatorListener)
                                               .forEach(listener -> ((ChudnovskyCalculatorListener) listener).notifyConstantCalculationCompleted(result)));
        return constant;
    }

    public interface ChudnovskyCalculatorListener extends PiCalculatorListener {

        void notifyDenominatorCalculationCompleted(int k, BigInteger result);

        void notifyNominatorCalculationCompleted(int k, BigInteger result);

        void notifyConstantCalculationCompleted(BigDecimal result);
    }

    public class ChudnovskyCalculatorAdapter implements ChudnovskyCalculatorListener {

        @Override
        public void notifyIterationCompleted(int index, BigDecimal result) {
        }

        @Override
        public void notifyDenominatorCalculationCompleted(int k, BigInteger result) {
        }

        @Override
        public void notifyNominatorCalculationCompleted(int k, BigInteger result) {
        }

        @Override
        public void notifyConstantCalculationCompleted(BigDecimal result) {
        }
    }
}