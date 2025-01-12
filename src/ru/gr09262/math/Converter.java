package ru.gr09262.math;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.lang.Math.abs;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Converter {

    private double xMin;

    private double xMax;

    private double yMin;

    private double yMax;
    @JsonIgnore
    private int width;
    @JsonIgnore
    private int height;

    public Converter(
            @JsonProperty("xmin") double xMin,
            @JsonProperty("xmax") double xMax,
            @JsonProperty("ymin") double yMin,
            @JsonProperty("ymax") double yMax,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height
    ){
        setXShape(xMin, xMax);
        setYShape(yMin, yMax);
        setWidth(width);
        setHeight(height);
    }

    public double getXMin() {
        return xMin;
    }

    public double getXMax() {
        return xMax;
    }

    public void setXShape(double xMin, double xMax) {
        this.xMin = Math.min(xMin, xMax);
        this.xMax = Math.max(xMin, xMax);
    }

    public double getYMin() {
        return yMin;
    }

    public double getYMax() {
        return yMax;
    }

    public void setYShape(double yMin, double yMax) {
        this.yMin = Math.min(yMin, yMax);
        this.yMax = Math.max(yMin, yMax);
    }

    public int getWidth() {
        return width - 1;
    }

    public void setWidth(int width) {
        this.width = abs(width);
    }

    public int getHeight() {
        return height - 1;
    }

    public void setHeight(int height) {
        this.height = abs(height);
    }
    @JsonIgnore
    public double getXDen(){
        return width / (xMax - xMin);
    }
    @JsonIgnore
    public double getYDen(){
        return height / (yMax - yMin);
    }

    /**
     * Метод преобразования координаты из декартовой системы в экранную
     * @param x декартовая система координат
     * @return экранная система координат, соответствующая указанной декартовой координате
     */
    public int xCrt2Scr(double x){
        var v = ((x - xMin) * getXDen());
        if (v < -width) v = -width;
        if (v > 2 * width) v = 2 * width;
        return (int)v;
    }

    public int yCrt2Scr(double y){
        var v = ((yMax - y) * getYDen());
        if (v < -height) v = -height;
        if (v > 2 * height) v = 2 * height;
        return (int)v;
    }

    public double xScr2Crt(int x){
        return (double)x / getXDen() + xMin;
    }

    public double yScr2Crt(int y){
        return yMax - (double)y / getYDen();
    }
}
