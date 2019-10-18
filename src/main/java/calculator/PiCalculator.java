package calculator;

/**
 * @author Stefan
 * @version 1.0
 */
public interface PiCalculator extends AsyncPiCalculator, SyncPiCalculator {

    void addListener(PiCalculatorListener listener);

    void removeListener(PiCalculatorListener listener);

    boolean hasListener(PiCalculatorListener listener);

    AsyncPiCalculator parallel();

    SyncPiCalculator sequential();
}
