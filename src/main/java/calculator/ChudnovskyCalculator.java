package calculator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChudnovskyCalculator implements PiCalculator {

    private ExecutorService executorService;

    public BigDecimal calculate(int iterations) {
        MathContext context = new MathContext(20, RoundingMode.DOWN);
        return calculate(iterations, context);
    }

    @Override
    public BigDecimal calculate(int iterations, MathContext precision) {
        if (iterations < 0) {
            throw new IllegalArgumentException();
        }
        return chudnovsky(iterations, precision);
    }


    private BigInteger factorial(int n) {

        BigInteger result = BigInteger.valueOf(1);
        if (n <= 1) {
            return BigInteger.valueOf(1);
        }
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    public BigDecimal chudnovsky(int n, MathContext context) {

        Supplier<BigDecimal> cons = executeCalculation(() -> chudnovskyConstant(context));
        Supplier<BigDecimal> result = executeCalculation(() -> chudnovskySum(n, context));


        Supplier<BigDecimal> pi = executeCalculation(() -> cons.get().divide(result.get(), context));
        return pi.get();
    }

    private BigDecimal chudnovskyNumber(int k, MathContext context) {

        Callable<BigInteger> f1 = () -> factorial(6 * k);
        Callable<BigInteger> f2 = () -> factorial(3 * k);
        Callable<BigInteger> f3 = () -> factorial(k);

        Callable<BigDecimal> mul1 = () -> BigDecimal.valueOf(545140134).multiply(BigDecimal.valueOf(k));

        Callable<BigDecimal> pow1 = () -> BigDecimal.valueOf(-262537412640768000L).pow(k);

        Supplier<BigInteger> f1Supplier = executeCalculation(f1);
        Supplier<BigInteger> f2Supplier = () -> executeCalculation(f2).get();
        Supplier<BigInteger> f3Supplier = () -> executeCalculation(f3).get();
        Supplier<BigDecimal> m1Supplier = () -> executeCalculation(mul1).get();
        Supplier<BigDecimal> pow1Supplier = () -> executeCalculation(pow1).get();

        Callable<BigDecimal> m2 = () -> new BigDecimal(f1Supplier.get()).multiply(m1Supplier.get());
        Callable<BigDecimal> pow2 = () -> new BigDecimal(f3Supplier.get()).pow(3);

        Supplier<BigDecimal> m2Supplier = executeCalculation(m2);
        Supplier<BigDecimal> pow2Supplier = executeCalculation(pow2);

        Callable<BigDecimal> addition1 = () -> m2Supplier.get().add(BigDecimal.valueOf(13591409));
        Callable<BigDecimal> m3 = () -> new BigDecimal(f2Supplier.get()).multiply(pow2Supplier.get());

        Supplier<BigDecimal> addition1Supplier = executeCalculation(addition1);
        Supplier<BigDecimal> m3Supplier = executeCalculation(m3);

        Callable<BigDecimal> m4 = () -> m3Supplier.get().multiply(pow1Supplier.get());
        Supplier<BigDecimal> m4Supplier = executeCalculation(m4);
        Supplier<BigDecimal> resultSupplier = () -> addition1Supplier.get().divide(m4Supplier.get(), context).stripTrailingZeros();

        BigDecimal result = resultSupplier.get();
        System.out.println("Finished chudnovsky number: " + k);
        return result;
    }

    private BigDecimal chudnovskySum(int n, MathContext context) {
        IntStream intStream = IntStream.range(0, n + 1).parallel();
        Stream<Callable<BigDecimal>> lotOfWork = intStream.mapToObj(value -> () -> chudnovskyNumber(value, context));
        BigDecimal result = lotOfWork.map(this::executeCalculation).map(Supplier::get).reduce(BigDecimal.ZERO, BigDecimal::add);
        return result;
    }

    private BigDecimal chudnovskyConstant(MathContext context) {

        Callable<BigDecimal> sqrt1 = () -> BigDecimal.valueOf(10005).sqrt(context);
        Supplier<BigDecimal> sqrtSupplier = executeCalculation(sqrt1);

        Callable<BigDecimal> m1 = () -> BigDecimal.valueOf(426880).multiply(sqrtSupplier.get());
        Supplier<BigDecimal> m1Supplier = executeCalculation(m1);

        return m1Supplier.get();
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    private <T> Supplier<T> executeCalculation(Callable<T> callable) {
        if (executorService != null) {
            Future<T> future = executorService.submit(callable);
            return () -> {
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                throw new RuntimeException();
            };
        } else {
            return () -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                throw new RuntimeException();
            };
        }
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
