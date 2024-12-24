package ru.gr09262.gui;

import ru.gr09262.fractals.Mondelbrot;
import ru.gr09262.math.Complex;
import ru.gr09262.math.Converter;

import java.awt.*;

public class FractalPainter implements Painter{

    private final Mondelbrot mondelbrot = new Mondelbrot();
    private final Converter converter;

    public FractalPainter(double xMin, double xMax, double yMin, double yMax){
        converter = new Converter(xMin,xMax,yMin,yMax,0,0);
    }

    public void updateCoordinates(double xMin, double xMax,
                                  double yMin, double yMax){
        converter.setXShape(xMin,xMax);
        converter.setYShape(yMin,yMax);
    }

    @Override
    public void paint(Graphics g) {
        for(int i = 0; i < getWidth(); i++) {
            for(int j = 0; j < getHeight(); j++) {
                var x = converter.xScr2Crt(i);
                var y = converter.yScr2Crt(j);
                var color = mondelbrot.isInSet(new Complex(x, y)) ?
                        Color.BLACK : Color.WHITE;
                g.setColor(color);
                g.fillRect(i,j,1,1);
            }
        }
    }

    @Override
    public int getWidth() {
        return converter.getWidth();
    }

    @Override
    public void setWidth(int width) {
        converter.setWidth(width);
    }

    @Override
    public int getHeight() {
        return converter.getHeight();
    }

    @Override
    public void setHeight(int height) {
        converter.setHeight(height);
    }
}
