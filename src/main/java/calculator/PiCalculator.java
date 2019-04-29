package calculator;

public interface PiCalculator extends AsyncPiCalculator, SyncPiCalculator {

    AsyncPiCalculator parallel();

    SyncPiCalculator sequential();
}
