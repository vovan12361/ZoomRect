package ru.gr09262.fractals;

import ru.gr09262.math.Complex;


/**
 * Класс для вычисления принадлежности комплексного числа к множеству Мандельброта.
 */
public class Mondelbrot {
    private final double r2 = 4.0;
    private int Iter = 200;
    /**
     * Определяет, принадлежит ли данное комплексное число множеству Мандельброта.
     *
     * @param c Комплексное число, для которого вычисляется принадлежность множеству.
     * @return Количество итераций до выхода за пределы области, либо максимальное значение итераций.
     */
    public int isInSet(Complex c) {
        Complex z = new Complex();
        int i = 0;
        while(z.abs2() < r2 && i < Iter) {
            z = z.times(z).plus(c);
            i++;
        }
        return i;
    }

    /**
     * Получает текущее максимальное количество итераций.
     *
     * @return Максимальное количество итераций.
     */
    public int getIter() {
        return Iter;
    }

    /**
     * Устанавливает максимальное количество итераций.
     * Значение должно быть в диапазоне от 200 до 5000.
     *
     * @param iter Новое значение максимального количества итераций.
     */
    public void setIter(int iter) {
        if (iter < 200) iter = 200;
        if (iter > 5000) iter = 5000;
        Iter = iter;
    }
}
