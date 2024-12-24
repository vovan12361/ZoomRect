package ru.gr09262.math;

public class Complex {
    private double re;
    private double im;
    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }
    public Complex() {
        re = 0;
        im = 0;
    }
    public Complex plus(Complex other){
        return new Complex(re+other.re,im+other.im);
    }

    public Complex times(Complex other){
        return new Complex(
                re* other.re-im* other.im,
                re*other.im+im* other.re
                );
    }
    public double abs2(){
        return re*re+im*im;
    }

}
