package calculator.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        Assertions.assertEquals(1, calculator.getNumIterations(13));
        Assertions.assertEquals(1, calculator.getNumIterations(14));
        Assertions.assertEquals(1, calculator.getNumIterations(15));
        Assertions.assertEquals(2, calculator.getNumIterations(27));
        Assertions.assertEquals(3, calculator.getNumIterations(41));
        Assertions.assertEquals(705, calculator.getNumIterations(10000));
        Assertions.assertEquals(2115, calculator.getNumIterations(30000));
        Assertions.assertEquals(3525, calculator.getNumIterations(50000));
        Assertions.assertEquals(8814, calculator.getNumIterations(125000));
    }
}
