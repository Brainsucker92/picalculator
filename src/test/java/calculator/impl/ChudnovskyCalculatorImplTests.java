package calculator.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Adds test cases for the ChudnovskyCalculator implementation
 *
 * @author Stefan
 * @version 1.0
 */
public class ChudnovskyCalculatorImplTests {

    private ChudnovskyCalculator calculator;
    private ExecutorService service = Executors.newSingleThreadExecutor();

    @BeforeEach
    public void initTests() {
        calculator = new ChudnovskyCalculator(service);
    }

    @Test
    public void getNumIterationsTest() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> calculator.getNumIterations(-1));

        Assertions.assertEquals(0, calculator.getNumIterations(0));
        Assertions.assertEquals(0, calculator.getNumIterations(12));
        Assertions.assertEquals(0, calculator.getNumIterations(13));
        Assertions.assertEquals(0, calculator.getNumIterations(14));
        Assertions.assertEquals(1, calculator.getNumIterations(15));
        Assertions.assertEquals(1, calculator.getNumIterations(28));
        Assertions.assertEquals(2, calculator.getNumIterations(29));
        Assertions.assertEquals(2, calculator.getNumIterations(42));
        Assertions.assertEquals(3, calculator.getNumIterations(43));
        Assertions.assertEquals(705, calculator.getNumIterations(10000));
        Assertions.assertEquals(2115, calculator.getNumIterations(30000));
        Assertions.assertEquals(3525, calculator.getNumIterations(50000));
        Assertions.assertEquals(8814, calculator.getNumIterations(125000));
        Assertions.assertEquals(70513, calculator.getNumIterations(1000000));
    }

    @Test
    public void testFactorial() {
        try {
            Method factorial = ChudnovskyCalculator.class.getDeclaredMethod("factorial", int.class);
            factorial.setAccessible(true);

            Assertions.assertEquals(factorial.invoke(calculator, 0), BigInteger.valueOf(1));
            Assertions.assertEquals(factorial.invoke(calculator, -1), BigInteger.valueOf(1));
            Assertions.assertEquals(factorial.invoke(calculator, 2), BigInteger.valueOf(2));
            Assertions.assertEquals(factorial.invoke(calculator, 3), BigInteger.valueOf(6));
            Assertions.assertEquals(factorial.invoke(calculator, 4), BigInteger.valueOf(24));
            Assertions.assertEquals(factorial.invoke(calculator, 5), BigInteger.valueOf(120));
            Assertions.assertEquals(factorial.invoke(calculator, 6), BigInteger.valueOf(720));
            Assertions.assertEquals(factorial.invoke(calculator, 10), BigInteger.valueOf(3628800));
            Assertions.assertEquals(factorial.invoke(calculator, 15), BigInteger.valueOf(1307674368000L));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Disabled
    public void getPrecisionTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> calculator.getPrecision(-1));
        // TODO
    }
}
