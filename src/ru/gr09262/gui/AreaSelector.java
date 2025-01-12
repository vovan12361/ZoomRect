package ru.gr09262.gui;

import java.awt.*;

/**
 * Класс для отображения прямоугольной области, очерчивающей границы
 * выбираемой при помощи мыши области
 */
public class AreaSelector {
    private final Rect rect = new Rect();
    private Graphics mainGraphics;
    private Color color;

    /**
     * Получение объекта графики, с помощью которого осуществляется вывод изображения
     * @return объект класса {@link Graphics}
     */
    public Graphics getGraphics() {
        return mainGraphics;
    }

    /**
     * Задание основного объекта графики для отображения
     * @param graphics объект класс {@link Graphics}, используемый для
     * отрисовки изображения
     */
    public void setGraphics(Graphics graphics) {
        if (mainGraphics == null){
            // Если mainGraphics устанавливается впервые,
            // будет выполнена инициализация режима XOR
            // для устранения возможности появления артефактов.
            mainGraphics = graphics;
            mainGraphics.setXORMode(Color.WHITE);
            mainGraphics.fillRect(-100, -100, 2, 2);
            mainGraphics.setPaintMode();
        }
        this.mainGraphics = graphics;
    }

    /**
     * Получение цвета рамки
     * @return цвет рамки
     */
    public Color getColor() {
        return color;
    }

    /**
     * Задание цвета рамки
     * @param color цвет рамки
     */
    public void setColor(Color color) {
        this.color = color;
    }


    /**
     * Отрисовка прямоугольной области, сформированной двумя заданными точками
     */
    public void paint () {
        var startPoint =rect.getStartPoint();
        if (startPoint!= null) {
            mainGraphics.setXORMode(Color.WHITE);
            mainGraphics.setColor(color);
            mainGraphics.drawRect(
                    startPoint.x,
                    startPoint.y,
                    rect.getWidth(),
                    rect.getHeigth()
            );
            mainGraphics.setPaintMode();
        }
    }

    /**
     * Добавление точки в прямоугольную область, вокруг которой следует
     * отрисовать рамку
     * @param point добавляемая точка
     */
    public void addPoint(Point point){
        rect.addPoint(point);
    }

    /**
     * Очистка выбранной прямоугольной области путём удаления сформировавших ее точек
     */
    public void clearSelection(){
        rect.clearPoints();
    }

    public Rect getRect() {
        return rect;
    }
}