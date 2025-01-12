package ru.gr09262.gui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.gr09262.fractals.Mondelbrot;
import ru.gr09262.math.Complex;
import ru.gr09262.math.Converter;

import java.awt.*;
@JsonIgnoreProperties(ignoreUnknown = true)
public class FractalPainter implements Painter{

    private final Mondelbrot mondelbrot = new Mondelbrot();
    private final Converter converter;
    @JsonCreator
    public FractalPainter(@JsonProperty("xmin") double xMin,
                          @JsonProperty("xmax") double xMax,
                          @JsonProperty("ymin") double yMin,
                          @JsonProperty("ymax") double yMax){
        converter = new Converter(xMin,xMax,yMin,yMax,0,0);
    }

    public Converter getConverter() {
        return converter;
    }

    public void updateCoordinates(double xMin, double xMax,
                                  double yMin, double yMax, int panelWidth, int panelHeight){
        converter.setXShape(xMin,xMax);
        converter.setYShape(yMin,yMax);
        adjustCoordinatesToAspectRatio(panelWidth, panelHeight);
    }

    public void adjustCoordinatesToAspectRatio(int panelWidth, int panelHeight) {
        double panelAspect = (double) panelWidth / panelHeight;
        double fractalWidth = converter.getXMax() - converter.getXMin();
        double fractalHeight = converter.getYMax() - converter.getYMin();
        double fractalAspect = fractalWidth / fractalHeight;

        if (panelAspect > fractalAspect) {
            // Панель шире, чем фрактал — корректируем ширину
            double newWidth = fractalHeight * panelAspect;
            double centerX = (converter.getXMin() + converter.getXMax()) / 2;
            converter.setXShape(centerX - newWidth / 2, centerX + newWidth / 2);
        } else {
            // Панель выше, чем фрактал — корректируем высоту
            double newHeight = fractalWidth / panelAspect;
            double centerY = (converter.getYMin() + converter.getYMax()) / 2;
            converter.setYShape(centerY - newHeight / 2, centerY + newHeight / 2);
        }
    }

    @Override
    public void paint(Graphics g) {
        for(int i = 0; i < getWidth(); i++) {
            for(int j = 0; j < getHeight(); j++) {
                var x = converter.xScr2Crt(i);
                var y = converter.yScr2Crt(j);
                var iterations = mondelbrot.isInSet(new Complex(x, y));
                Color color;
                if(iterations == 200) {
                    color = Color.BLACK;
                } else {
                    float hue = (float) iterations / 400;
                    color = Color.getHSBColor(hue, 1.0f, 1.0f);
                }
                g.setColor(color);
                g.fillRect(i,j,1,1);
            }
        }
    }

    @Override
    @JsonIgnore
    public int getWidth() {
        return converter.getWidth();
    }

    @Override
    public void setWidth(int width) {
        converter.setWidth(width);
    }

    @Override
    @JsonIgnore
    public int getHeight() {
        return converter.getHeight();
    }

    @Override
    public void setHeight(int height) {
        converter.setHeight(height);
    }

    public void resetCoordinates() {
        updateCoordinates(-2.0, 1.0, -1.0, 1.0, getWidth(), getHeight());
    }
}
