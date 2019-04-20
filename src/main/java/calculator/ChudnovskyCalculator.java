package calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ChudnovskyCalculator implements PiCalculator {

    private static HashMap<Integer, BigDecimal> chudnovskyList = new HashMap<>();
    private static HashMap<Integer, BigDecimal> factorials = new HashMap<>();

    private ExecutorService executorService;

    public BigDecimal calculate(int n) {
        if (n < 0) {
            throw new RuntimeException();
        }
        return chudnovsky(n);
    }

    private BigDecimal factorial(int n) {

        if (factorials.containsKey(n)) return factorials.get(n);

        BigDecimal result = BigDecimal.valueOf(1);
        if (n <= 1) {
            return BigDecimal.valueOf(1);
        }
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigDecimal.valueOf(i));
        }
        factorials.put(n, result);
        return result;
    }

    public BigDecimal chudnovsky(int n) {

        MathContext mathContext = new MathContext(50000, RoundingMode.DOWN);
        IntStream intStream = IntStream.range(0, n + 1);
        Stream<Callable<BigDecimal>> lotOfWork = intStream.mapToObj(value -> () -> {

            BigDecimal nom = factorial(6 * value).multiply(BigDecimal.valueOf(545140134).multiply(BigDecimal.valueOf(value)).add(BigDecimal.valueOf(13591409)));
            BigDecimal denom = factorial(3 * value).multiply(factorial(value).pow(3)).multiply(BigDecimal.valueOf(-262537412640768000L).pow(value));
            return nom.divide(denom, mathContext).stripTrailingZeros();
        });
        List<Callable<BigDecimal>> workList = lotOfWork.collect(Collectors.toList());
        List<BigDecimal> resultList = List.of();
        if (executorService != null) {
            try {
                List<Future<BigDecimal>> futureList = executorService.invokeAll(workList);
                resultList = futureList.stream().map((f) -> {
                    try {
                        return f.get().stripTrailingZeros();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    throw new RuntimeException();
                }).collect(Collectors.toList());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            resultList = workList.stream().map(f -> {
                try {
                    return f.call().stripTrailingZeros();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                throw new RuntimeException();
            }).collect(Collectors.toList());
        }
        BigDecimal result = BigDecimal.valueOf(0.0);
        for (BigDecimal r : resultList) {
            result = result.add(r);
        }
        BigDecimal cons = BigDecimal.valueOf(426880).multiply(BigDecimal.valueOf(10005).sqrt(mathContext));
        BigDecimal pi = cons.divide(result, result.scale(), RoundingMode.DOWN);
        return pi;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
