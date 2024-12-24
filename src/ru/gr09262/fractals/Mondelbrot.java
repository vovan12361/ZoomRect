package ru.gr09262.fractals;

import ru.gr09262.math.Complex;

public class Mondelbrot {
    private final double r2 = 4.0;
    private final int maxIter = 200;
    public boolean isInSet(Complex c) {
        Complex z = new Complex();
        int i = 0;
        while(z.abs2() < r2 && i < maxIter) {
            z = z.times(z).plus(c);
            i++;
        }
        return i == maxIter;
    }

}
