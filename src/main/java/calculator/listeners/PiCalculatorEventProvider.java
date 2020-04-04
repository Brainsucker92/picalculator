package calculator.listeners;

public interface PiCalculatorEventProvider {

    boolean hasListener(PiCalculatorListener listener);

    void addListener(PiCalculatorListener listener);

    void removeListener(PiCalculatorListener listener);
}
