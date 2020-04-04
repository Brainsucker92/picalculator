package calculator;

/**
 * @author Stefan
 * @version 1.0
 */
public interface PiCalculator extends AsyncPiCalculator, SyncPiCalculator {

    AsyncPiCalculator parallel();

    SyncPiCalculator sequential();
}
