package calculator.tools;

/**
 * Indicator interface, that the implementation is capable of providing information about the calculation precision.
 */
public interface PrecisionProvider {

    /**
     * Returns the number of digits that can be calculated precisely with a given amount of iterations.
     *
     * @param iterations The number of iterations you want to calculate
     * @return The number of digits that will be calculated correctly
     */
    int getPrecision(int iterations);

    /**
     * Calculates the number of iterations required for the specified level of precision. More information:
     * https://mathoverflow.net/q/261162/146822
     *
     * @param precision The level of precision you want to calculate.
     * @return The number of iterations required
     */
    int getNumIterations(int precision);
}
