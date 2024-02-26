package gauss.utils;

import gauss.domain.Fraction;

/**
 * Библиотека методов для работы с дробями.
 */
public class FractionUtils {

    public static void toCommonDenominator(Fraction a, Fraction b) {
        if (a.getDenominator() != b.getDenominator()) {
            int temp = a.getDenominator();
            a.multiple(b.getDenominator());
            b.multiple(temp);
        }
    }

    public static Fraction amount(Fraction a, Fraction b) {
        toCommonDenominator(a, b);
        return new Fraction(a.getNumerator() + b.getNumerator(), a.getDenominator());
    }

    public static Fraction difference(Fraction a, Fraction b) {
        toCommonDenominator(a, b);
        return new Fraction(a.getNumerator() - b.getNumerator(), a.getDenominator());
    }

    public static Fraction multiplication(Fraction a, Fraction b) {
        return new Fraction(a.getNumerator() * b.getNumerator(),
                a.getDenominator() * b.getDenominator());
    }

    public static Fraction division(Fraction a, Fraction b) {
        return new Fraction(a.getNumerator() * b.getDenominator(),
                a.getDenominator() * b.getNumerator());
    }
}
